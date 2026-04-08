package com.nzhk.wxg.business.focus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nzhk.wxg.business.focus.bean.*;
import com.nzhk.wxg.business.focus.entity.FocusRemindLog;
import com.nzhk.wxg.business.focus.entity.FocusSession;
import com.nzhk.wxg.business.focus.service.IFocusService;
import com.nzhk.wxg.business.habit.entity.Habit;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.exception.BizException;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.mapper.FocusRemindLogMapper;
import com.nzhk.wxg.mapper.FocusSessionMapper;
import com.nzhk.wxg.mapper.HabitMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class FocusServiceImpl implements IFocusService {
    /** 与 PostgreSQL TIMESTAMPTZ / JDBC 一致，避免 LocalDateTime 映射报错 */
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_PAUSED = 2;
    private static final int STATUS_FINISHED = 3;
    private static final int STATUS_CANCELLED = 4;

    @Resource
    private FocusSessionMapper focusSessionMapper;
    @Resource
    private HabitMapper habitMapper;
    @Resource
    private FocusRemindLogMapper focusRemindLogMapper;

    @Override
    public FocusSessionResData start(FocusStartReqData data) {
        if (data == null || StringUtils.isBlank(data.getHabitId())) {
            throw new BizException(40000, "habitId不能为空");
        }
        int plannedMinutes = data.getPlannedMinutes() == null ? 25 : data.getPlannedMinutes();
        if (plannedMinutes <= 0 || plannedMinutes > 720) {
            throw new BizException(40000, "plannedMinutes范围不合法");
        }
        String userId = ContextCache.getUserId();
        Habit habit = habitMapper.selectById(data.getHabitId());
        if (habit == null || !userId.equals(habit.getUserId())) {
            throw new BizException(40000, "习惯不存在");
        }

        LambdaQueryWrapper<FocusSession> activeQ = new LambdaQueryWrapper<>();
        activeQ.eq(FocusSession::getUserId, userId)
                .eq(FocusSession::getHabitId, data.getHabitId())
                .in(FocusSession::getStatus, STATUS_RUNNING, STATUS_PAUSED);
        if (focusSessionMapper.selectCount(activeQ) > 0) {
            throw new BizException(40000, "当前习惯已有进行中的专注");
        }

        OffsetDateTime now = OffsetDateTime.now(ZONE);
        FocusSession s = new FocusSession();
        s.setId(IdUtil.getId());
        s.setUserId(userId);
        s.setHabitId(data.getHabitId());
        s.setPlannedMinutes(plannedMinutes);
        s.setActualSeconds(0);
        s.setStatus(STATUS_RUNNING);
        s.setStartTime(now);
        s.setExpectedEndTime(now.plusMinutes(plannedMinutes));
        s.setPauseTotalSeconds(0);
        s.setRemindSound(data.getRemindSound() == null || data.getRemindSound());
        s.setRemindVibrate(data.getRemindVibrate() == null || data.getRemindVibrate());
        s.setRemindSubscribe(data.getRemindSubscribe() == null || data.getRemindSubscribe());
        s.setRemindSentFlag(false);
        focusSessionMapper.insert(s);
        return toRes(s);
    }

    @Override
    public FocusSessionResData pause(FocusActionReqData data) {
        FocusSession s = getMineSession(data == null ? null : data.getSessionId());
        if (s.getStatus() != STATUS_RUNNING) throw new BizException(40000, "仅进行中专注可暂停");
        s.setStatus(STATUS_PAUSED);
        s.setPauseStartTime(OffsetDateTime.now(ZONE));
        focusSessionMapper.updateById(s);
        return toRes(s);
    }

    @Override
    public FocusSessionResData resume(FocusActionReqData data) {
        FocusSession s = getMineSession(data == null ? null : data.getSessionId());
        if (s.getStatus() != STATUS_PAUSED) throw new BizException(40000, "仅暂停中的专注可继续");
        OffsetDateTime now = OffsetDateTime.now(ZONE);
        int pauseSeconds = 0;
        if (s.getPauseStartTime() != null) {
            pauseSeconds = (int) Math.max(0, ChronoUnit.SECONDS.between(s.getPauseStartTime(), now));
        }
        s.setPauseTotalSeconds((s.getPauseTotalSeconds() == null ? 0 : s.getPauseTotalSeconds()) + pauseSeconds);
        s.setPauseStartTime(null);
        s.setStatus(STATUS_RUNNING);
        s.setExpectedEndTime(s.getExpectedEndTime().plusSeconds(pauseSeconds));
        focusSessionMapper.updateById(s);
        return toRes(s);
    }

    @Override
    public FocusSessionResData adjust(FocusAdjustReqData data) {
        FocusSession s = getMineSession(data == null ? null : data.getSessionId());
        if (s.getStatus() != STATUS_RUNNING && s.getStatus() != STATUS_PAUSED) {
            throw new BizException(40000, "当前状态不支持调时长");
        }
        int delta = data == null || data.getDeltaMinutes() == null ? 0 : data.getDeltaMinutes();
        if (delta == 0) return toRes(s);
        int next = Math.max(1, Math.min(720, (s.getPlannedMinutes() == null ? 25 : s.getPlannedMinutes()) + delta));
        int realDelta = next - (s.getPlannedMinutes() == null ? 25 : s.getPlannedMinutes());
        s.setPlannedMinutes(next);
        s.setExpectedEndTime(s.getExpectedEndTime().plusMinutes(realDelta));
        focusSessionMapper.updateById(s);
        return toRes(s);
    }

    @Override
    public FocusSessionResData finish(FocusFinishReqData data) {
        FocusSession s = getMineSession(data == null ? null : data.getSessionId());
        if (s.getStatus() != STATUS_RUNNING && s.getStatus() != STATUS_PAUSED) {
            throw new BizException(40000, "当前状态无法结束");
        }
        OffsetDateTime now = OffsetDateTime.now(ZONE);
        int pauseTotalSeconds = s.getPauseTotalSeconds() == null ? 0 : s.getPauseTotalSeconds();
        if (s.getStatus() == STATUS_PAUSED && s.getPauseStartTime() != null) {
            pauseTotalSeconds += (int) Math.max(0, ChronoUnit.SECONDS.between(s.getPauseStartTime(), now));
        }
        int actualSeconds = (int) Math.max(0, ChronoUnit.SECONDS.between(s.getStartTime(), now) - pauseTotalSeconds);
        String finishType = data != null && StringUtils.isNotBlank(data.getFinishType()) ? data.getFinishType() : "manual";
        String ft = finishType.toLowerCase(Locale.ROOT);
        int status = "cancel".equals(ft) ? STATUS_CANCELLED : STATUS_FINISHED;

        s.setPauseTotalSeconds(pauseTotalSeconds);
        s.setActualSeconds(actualSeconds);
        s.setEndTime(now);
        s.setStatus(status);
        s.setFinishType(ft);
        s.setPauseStartTime(null);
        focusSessionMapper.updateById(s);
        return toRes(s);
    }

    @Override
    public FocusSessionResData current(FocusCurrentReqData data) {
        if (data == null || StringUtils.isBlank(data.getHabitId())) return null;
        String userId = ContextCache.getUserId();
        LambdaQueryWrapper<FocusSession> q = new LambdaQueryWrapper<>();
        q.eq(FocusSession::getUserId, userId)
                .eq(FocusSession::getHabitId, data.getHabitId())
                .in(FocusSession::getStatus, STATUS_RUNNING, STATUS_PAUSED)
                .orderByDesc(FocusSession::getCreateTime)
                .last("limit 1");
        FocusSession s = focusSessionMapper.selectOne(q);
        return s == null ? null : toRes(s);
    }

    @Override
    public FocusListResData list(FocusListReqData data) {
        String userId = ContextCache.getUserId();
        LambdaQueryWrapper<FocusSession> q = new LambdaQueryWrapper<>();
        q.eq(FocusSession::getUserId, userId);
        if (data != null && StringUtils.isNotBlank(data.getHabitId())) {
            q.eq(FocusSession::getHabitId, data.getHabitId());
        }
        if (data != null && data.getDate() != null) {
            OffsetDateTime s = data.getDate().atStartOfDay(ZONE).toOffsetDateTime();
            OffsetDateTime e = data.getDate().plusDays(1).atStartOfDay(ZONE).toOffsetDateTime();
            q.ge(FocusSession::getStartTime, s).lt(FocusSession::getStartTime, e);
        }
        q.orderByDesc(FocusSession::getStartTime);
        List<FocusSession> all = focusSessionMapper.selectList(q);
        int pageNo = data != null && data.getPageNo() != null && data.getPageNo() > 0 ? data.getPageNo() : 1;
        int pageSize = data != null && data.getPageSize() != null && data.getPageSize() > 0 ? data.getPageSize() : 20;
        int from = Math.min((pageNo - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        FocusListResData res = new FocusListResData();
        res.setTotal((long) all.size());
        res.setList(all.subList(from, to).stream().map(this::toRes).collect(Collectors.toList()));
        return res;
    }

    @Override
    public FocusStatsResData stats(FocusStatsReqData data) {
        String userId = ContextCache.getUserId();
        LambdaQueryWrapper<FocusSession> q = new LambdaQueryWrapper<>();
        q.eq(FocusSession::getUserId, userId)
                .eq(FocusSession::getStatus, STATUS_FINISHED);
        if (data != null && StringUtils.isNotBlank(data.getHabitId())) {
            q.eq(FocusSession::getHabitId, data.getHabitId());
        }
        LocalDate now = LocalDate.now();
        String period = data != null && StringUtils.isNotBlank(data.getPeriod()) ? data.getPeriod() : "all";
        if ("day".equals(period)) {
            OffsetDateTime dayStart = now.atStartOfDay(ZONE).toOffsetDateTime();
            OffsetDateTime dayEnd = now.plusDays(1).atStartOfDay(ZONE).toOffsetDateTime();
            q.ge(FocusSession::getStartTime, dayStart).lt(FocusSession::getStartTime, dayEnd);
        } else if ("week".equals(period)) {
            LocalDate weekStart = now.with(DayOfWeek.MONDAY);
            OffsetDateTime ws = weekStart.atStartOfDay(ZONE).toOffsetDateTime();
            OffsetDateTime we = weekStart.plusDays(7).atStartOfDay(ZONE).toOffsetDateTime();
            q.ge(FocusSession::getStartTime, ws).lt(FocusSession::getStartTime, we);
        } else if ("month".equals(period)) {
            LocalDate monthStart = now.withDayOfMonth(1);
            OffsetDateTime ms = monthStart.atStartOfDay(ZONE).toOffsetDateTime();
            OffsetDateTime me = monthStart.plusMonths(1).atStartOfDay(ZONE).toOffsetDateTime();
            q.ge(FocusSession::getStartTime, ms).lt(FocusSession::getStartTime, me);
        }
        List<FocusSession> list = focusSessionMapper.selectList(q);
        long sessions = list.size();
        long seconds = list.stream().map(FocusSession::getActualSeconds).filter(v -> v != null && v > 0).mapToLong(Integer::longValue).sum();
        FocusStatsResData res = new FocusStatsResData();
        res.setTotalSessions(sessions);
        res.setTotalSeconds(seconds);
        res.setTotalMinutes(seconds / 60);
        res.setAvgMinutes(sessions == 0 ? 0 : (seconds / sessions) / 60);
        return res;
    }

    @Override
    public void delete(FocusActionReqData data) {
        FocusSession s = getMineSession(data == null ? null : data.getSessionId());
        focusSessionMapper.deleteById(s.getId());
    }

    @Override
    public List<FocusSession> listNeedRemind() {
        LambdaQueryWrapper<FocusSession> q = new LambdaQueryWrapper<>();
        q.eq(FocusSession::getStatus, STATUS_RUNNING)
                .eq(FocusSession::getRemindSubscribe, true)
                .eq(FocusSession::getRemindSentFlag, false)
                .le(FocusSession::getExpectedEndTime, OffsetDateTime.now(ZONE))
                .orderByAsc(FocusSession::getExpectedEndTime);
        return focusSessionMapper.selectList(q);
    }

    @Override
    public void markRemindSent(String sessionId) {
        FocusSession s = focusSessionMapper.selectById(sessionId);
        if (s == null) return;
        s.setRemindSentFlag(true);
        s.setRemindSentTime(OffsetDateTime.now(ZONE));
        focusSessionMapper.updateById(s);
    }

    private FocusSession getMineSession(String sessionId) {
        if (StringUtils.isBlank(sessionId)) throw new BizException(40000, "sessionId不能为空");
        FocusSession s = focusSessionMapper.selectById(sessionId);
        if (s == null) throw new BizException(40000, "专注记录不存在");
        if (!ContextCache.getUserId().equals(s.getUserId())) throw new BizException(40000, "无权限操作该专注记录");
        return s;
    }

    private FocusSessionResData toRes(FocusSession s) {
        FocusSessionResData r = new FocusSessionResData();
        r.setSessionId(s.getId());
        r.setHabitId(s.getHabitId());
        r.setPlannedMinutes(s.getPlannedMinutes());
        r.setActualSeconds(s.getActualSeconds());
        r.setStatus(s.getStatus());
        r.setStartTime(s.getStartTime());
        r.setEndTime(s.getEndTime());
        r.setExpectedEndTime(s.getExpectedEndTime());
        r.setPauseTotalSeconds(s.getPauseTotalSeconds());
        r.setFinishType(s.getFinishType());
        return r;
    }

    public void saveRemindLog(String userId, String habitId, String sessionId, boolean success, String errCode, String errMsg) {
        FocusRemindLog log = new FocusRemindLog();
        log.setId(IdUtil.getId());
        log.setUserId(userId);
        log.setHabitId(habitId);
        log.setSessionId(sessionId);
        log.setRemindChannel("subscribe");
        log.setSendStatus(success ? 1 : 0);
        log.setErrCode(errCode);
        log.setErrMsg(errMsg);
        focusRemindLogMapper.insert(log);
    }
}

