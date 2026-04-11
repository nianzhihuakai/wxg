package com.nzhk.wxg.business.periodgoal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nzhk.wxg.business.habit.entity.Habit;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalDeleteReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalGetReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalItemResData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalListReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalListResData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalSaveReqData;
import com.nzhk.wxg.business.periodgoal.bean.PeriodGoalSaveResData;
import com.nzhk.wxg.business.periodgoal.entity.PeriodGoal;
import com.nzhk.wxg.business.periodgoal.service.IPeriodGoalService;
import com.nzhk.wxg.common.exception.BizException;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.mapper.HabitMapper;
import com.nzhk.wxg.mapper.PeriodGoalMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PeriodGoalServiceImpl implements IPeriodGoalService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int HARVEST_MAX = 20000;
    private static final Map<String, Integer> HABIT_LIMIT = new HashMap<>();
    /** 同一用户、同一自然周期内最多创建的目标条数（与习惯数上限数值相同，语义不同） */
    private static final Map<String, Integer> GOAL_COUNT_LIMIT = new HashMap<>();

    static {
        HABIT_LIMIT.put("week", 3);
        HABIT_LIMIT.put("month", 5);
        HABIT_LIMIT.put("year", 10);
        GOAL_COUNT_LIMIT.put("week", 3);
        GOAL_COUNT_LIMIT.put("month", 5);
        GOAL_COUNT_LIMIT.put("year", 10);
    }

    @Resource
    private PeriodGoalMapper periodGoalMapper;

    @Resource
    private HabitMapper habitMapper;

    @Override
    public PeriodGoalListResData list(String userId, PeriodGoalListReqData data) {
        validateListRequest(data);
        LocalDate start = LocalDate.parse(data.getPeriodStart(), DATE_FMT);
        LocalDate end = LocalDate.parse(data.getPeriodEnd(), DATE_FMT);
        LambdaQueryWrapper<PeriodGoal> w = new LambdaQueryWrapper<>();
        w.eq(PeriodGoal::getUserId, userId)
                .eq(PeriodGoal::getPeriodType, data.getPeriodType())
                .eq(PeriodGoal::getPeriodStart, start)
                .eq(PeriodGoal::getPeriodEnd, end)
                .eq(PeriodGoal::getStatus, 1)
                .orderByAsc(PeriodGoal::getCreatedAt);
        List<PeriodGoal> rows = periodGoalMapper.selectList(w);
        PeriodGoalListResData res = new PeriodGoalListResData();
        res.setList(rows.stream().map(this::toItem).collect(Collectors.toList()));
        return res;
    }

    @Override
    public PeriodGoalItemResData get(String userId, PeriodGoalGetReqData data) {
        if (data == null || StringUtils.isBlank(data.getId())) {
            throw new BizException(40000, "缺少目标ID");
        }
        PeriodGoal row = loadOwned(userId, data.getId());
        return toItem(row);
    }

    @Override
    public PeriodGoalSaveResData save(String userId, PeriodGoalSaveReqData data) {
        validateSaveRequest(data);
        List<String> habitIds = normalizeHabitIds(data.getHabitIds());
        int maxHabits = maxHabitsFor(data.getPeriodType());
        if (habitIds.size() > maxHabits) {
            throw new BizException(40000, "该周期最多关联 " + maxHabits + " 个习惯");
        }
        verifyHabitsOwned(userId, habitIds);
        LocalDate start = LocalDate.parse(data.getPeriodStart(), DATE_FMT);
        LocalDate end = LocalDate.parse(data.getPeriodEnd(), DATE_FMT);
        if (end.isBefore(start)) {
            throw new BizException(40000, "周期结束不能早于开始");
        }
        String harvest = StringUtils.defaultString(data.getHarvestHtml());
        if (harvest.length() > HARVEST_MAX) {
            throw new BizException(40000, "收获内容过长");
        }
        String habitJson;
        try {
            habitJson = JSON.writeValueAsString(habitIds);
        } catch (Exception e) {
            throw new BizException(50000, "习惯数据序列化失败");
        }
        LocalDateTime now = LocalDateTime.now();
        PeriodGoalSaveResData res = new PeriodGoalSaveResData();
        if (StringUtils.isBlank(data.getId())) {
            assertGoalCountUnderLimit(userId, data.getPeriodType(), start, end);
            PeriodGoal row = new PeriodGoal();
            row.setId(IdUtil.getId());
            row.setUserId(userId);
            row.setTitle(StringUtils.defaultIfBlank(StringUtils.trimToEmpty(data.getTitle()), "未命名目标"));
            row.setPeriodType(data.getPeriodType());
            row.setPeriodStart(start);
            row.setPeriodEnd(end);
            row.setMetricType(data.getMetricType());
            row.setTargetValue(data.getTargetValue());
            row.setHabitIds(habitJson);
            row.setHarvestHtml(harvest);
            row.setStatus(1);
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            periodGoalMapper.insert(row);
            res.setId(row.getId());
        } else {
            PeriodGoal row = loadOwned(userId, data.getId());
            boolean samePeriod = data.getPeriodType().equals(row.getPeriodType())
                    && start.equals(row.getPeriodStart())
                    && end.equals(row.getPeriodEnd());
            if (!samePeriod) {
                assertGoalCountUnderLimit(userId, data.getPeriodType(), start, end);
            }
            row.setTitle(StringUtils.defaultIfBlank(StringUtils.trimToEmpty(data.getTitle()), "未命名目标"));
            row.setPeriodType(data.getPeriodType());
            row.setPeriodStart(start);
            row.setPeriodEnd(end);
            row.setMetricType(data.getMetricType());
            row.setTargetValue(data.getTargetValue());
            row.setHabitIds(habitJson);
            row.setHarvestHtml(harvest);
            row.setUpdatedAt(now);
            LambdaUpdateWrapper<PeriodGoal> uw = new LambdaUpdateWrapper<>();
            uw.eq(PeriodGoal::getId, row.getId()).eq(PeriodGoal::getUserId, userId).eq(PeriodGoal::getStatus, 1);
            periodGoalMapper.update(row, uw);
            res.setId(row.getId());
        }
        return res;
    }

    @Override
    public void delete(String userId, PeriodGoalDeleteReqData data) {
        if (data == null || StringUtils.isBlank(data.getId())) {
            throw new BizException(40000, "缺少目标ID");
        }
        loadOwned(userId, data.getId());
        LambdaQueryWrapper<PeriodGoal> uw = new LambdaQueryWrapper<>();
        uw.eq(PeriodGoal::getId, data.getId()).eq(PeriodGoal::getUserId, userId).eq(PeriodGoal::getStatus, 1);
        int n = periodGoalMapper.delete(uw);
        if (n == 0) {
            throw new BizException(40400, "目标不存在");
        }
    }

    private void validateListRequest(PeriodGoalListReqData data) {
        if (data == null) {
            throw new BizException(40000, "参数为空");
        }
        if (!isValidPeriodType(data.getPeriodType())) {
            throw new BizException(40000, "周期类型无效");
        }
        parseDateRequired(data.getPeriodStart(), "periodStart");
        parseDateRequired(data.getPeriodEnd(), "periodEnd");
    }

    private void validateSaveRequest(PeriodGoalSaveReqData data) {
        if (data == null) {
            throw new BizException(40000, "参数为空");
        }
        if (!isValidPeriodType(data.getPeriodType())) {
            throw new BizException(40000, "周期类型无效");
        }
        if (!isValidMetricType(data.getMetricType())) {
            throw new BizException(40000, "统计方式无效");
        }
        parseDateRequired(data.getPeriodStart(), "periodStart");
        parseDateRequired(data.getPeriodEnd(), "periodEnd");
        if (data.getTargetValue() == null || data.getTargetValue() < 1) {
            throw new BizException(40000, "目标值须为大于等于1的整数");
        }
        if (CollectionUtils.isEmpty(data.getHabitIds())) {
            throw new BizException(40000, "请至少选择一个习惯");
        }
    }

    private LocalDate parseDateRequired(String s, String field) {
        if (StringUtils.isBlank(s)) {
            throw new BizException(40000, "缺少日期: " + field);
        }
        try {
            return LocalDate.parse(s.trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new BizException(40000, "日期格式错误: " + field);
        }
    }

    private boolean isValidPeriodType(String t) {
        return "week".equals(t) || "month".equals(t) || "year".equals(t);
    }

    private boolean isValidMetricType(String t) {
        return "checkin_days".equals(t) || "checkin_count".equals(t);
    }

    private int maxHabitsFor(String periodType) {
        return HABIT_LIMIT.getOrDefault(periodType, 3);
    }

    private int maxGoalsForPeriod(String periodType) {
        return GOAL_COUNT_LIMIT.getOrDefault(periodType, 3);
    }

    /**
     * 新建：目标周期内已有有效目标数须小于上限。
     * 更新且改周期：当前记录在库中仍属旧周期，故直接统计新周期即可。
     */
    private void assertGoalCountUnderLimit(String userId, String periodType, LocalDate periodStart, LocalDate periodEnd) {
        LambdaQueryWrapper<PeriodGoal> w = new LambdaQueryWrapper<>();
        w.eq(PeriodGoal::getUserId, userId)
                .eq(PeriodGoal::getPeriodType, periodType)
                .eq(PeriodGoal::getPeriodStart, periodStart)
                .eq(PeriodGoal::getPeriodEnd, periodEnd)
                .eq(PeriodGoal::getStatus, 1);
        Long cnt = periodGoalMapper.selectCount(w);
        int max = maxGoalsForPeriod(periodType);
        if (cnt != null && cnt >= max) {
            throw new BizException(40000, periodGoalLimitMessage(periodType, max));
        }
    }

    private String periodGoalLimitMessage(String periodType, int max) {
        String label = "week".equals(periodType) ? "周" : "month".equals(periodType) ? "月" : "年";
        return "本" + label + "最多创建" + max + "个目标";
    }

    private List<String> normalizeHabitIds(List<String> raw) {
        if (CollectionUtils.isEmpty(raw)) {
            return Collections.emptyList();
        }
        return raw.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private void verifyHabitsOwned(String userId, List<String> habitIds) {
        LambdaQueryWrapper<Habit> w = new LambdaQueryWrapper<>();
        w.eq(Habit::getUserId, userId)
                .in(Habit::getId, habitIds)
                .ne(Habit::getStatus, 0);
        Long count = habitMapper.selectCount(w);
        if (count == null || count.intValue() != habitIds.size()) {
            throw new BizException(40000, "存在无效或已删除的习惯");
        }
    }

    private PeriodGoal loadOwned(String userId, String id) {
        LambdaQueryWrapper<PeriodGoal> w = new LambdaQueryWrapper<>();
        w.eq(PeriodGoal::getUserId, userId).eq(PeriodGoal::getId, id).eq(PeriodGoal::getStatus, 1).last("limit 1");
        PeriodGoal row = periodGoalMapper.selectOne(w);
        if (row == null) {
            throw new BizException(40400, "目标不存在");
        }
        return row;
    }

    private PeriodGoalItemResData toItem(PeriodGoal row) {
        PeriodGoalItemResData item = new PeriodGoalItemResData();
        item.setId(row.getId());
        item.setTitle(row.getTitle());
        item.setPeriodType(row.getPeriodType());
        item.setPeriodStart(row.getPeriodStart() != null ? row.getPeriodStart().format(DATE_FMT) : null);
        item.setPeriodEnd(row.getPeriodEnd() != null ? row.getPeriodEnd().format(DATE_FMT) : null);
        item.setMetricType(row.getMetricType());
        item.setTargetValue(row.getTargetValue());
        item.setHarvestHtml(StringUtils.defaultString(row.getHarvestHtml()));
        item.setHabitIds(parseHabitIdsJson(row.getHabitIds()));
        return item;
    }

    private List<String> parseHabitIdsJson(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return JSON.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("parse habit ids failed: {}", json, e);
            return new ArrayList<>();
        }
    }
}
