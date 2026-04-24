package com.nzhk.wxg.business.habitcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.habit.entity.Habit;
import com.nzhk.wxg.business.habit.service.IHabitService;
import com.nzhk.wxg.business.file.entity.UploadedFile;
import com.nzhk.wxg.business.file.service.IFileService;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionGetReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionItemResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionListReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionListResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReflectionSaveReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReqData;
import com.nzhk.wxg.business.habitcheckin.bean.HabitRankItem;
import com.nzhk.wxg.business.habitcheckin.bean.StatisticsInfoResData;
import com.nzhk.wxg.business.habitcheckin.bean.UserCheckInAggregateItem;
import com.nzhk.wxg.business.habitcheckin.bean.UserCheckInDateItem;
import com.nzhk.wxg.business.habitcheckin.bean.UserCheckInRankItemResData;
import com.nzhk.wxg.business.habitcheckin.entity.HabitCheckIn;
import com.nzhk.wxg.business.habitcheckin.entity.UserRankSnapshot;
import com.nzhk.wxg.business.habitcheckin.service.IHabitCheckInService;
import com.nzhk.wxg.business.habitcheckin.service.UserRankSnapshotAsyncService;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.exception.BizException;
import com.nzhk.wxg.common.utils.FileSignUtil;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.common.utils.NzhkDateUtil;
import com.nzhk.wxg.mapper.HabitCheckInMapper;
import com.nzhk.wxg.mapper.HabitMapper;
import com.nzhk.wxg.mapper.UserRankSnapshotMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 习惯打卡记录表 服务实现类
 * </p>
 *
 * @author lxy
 * @since 2026-02-06
 */
@Slf4j
@Service
public class HabitCheckInServiceImpl extends ServiceImpl<HabitCheckInMapper, HabitCheckIn> implements IHabitCheckInService {
    private static final int USER_RANK_LIMIT = 100;

    @Resource
    private HabitMapper habitMapper;

    @Resource
    private IHabitService habitService;

    @Resource
    private IFileService fileService;

    @Resource
    private FileSignUtil fileSignUtil;

    @Resource
    private UserRankSnapshotMapper userRankSnapshotMapper;

    @Resource
    private UserRankSnapshotAsyncService userRankSnapshotAsyncService;

    @Override
    public void checkIn(CheckInReqData data) {
        log.info("checkIn userId:{}, habitId:{}", ContextCache.getUserId(), data != null ? data.getHabitId() : null);
        HabitCheckIn habitCheckIn = new HabitCheckIn();
        habitCheckIn.setId(IdUtil.getId());
        habitCheckIn.setHabitId(data.getHabitId());
        habitCheckIn.setUserId(ContextCache.getUserId());
        habitCheckIn.setCheckInDate(LocalDate.now());
        habitCheckIn.setCheckInTime(LocalDateTime.now());
        habitCheckIn.setCheckInType(1);
        baseMapper.insert(habitCheckIn);
        userRankSnapshotAsyncService.refreshSingleUserRankSnapshotAsync(ContextCache.getUserId());
    }

    @Override
    public void fillCheckIn(CheckInReqData data) {
        log.info("fillCheckIn userId:{}, data:{}", ContextCache.getUserId(), data);
        Integer fillCheckInStatus = data.getFillCheckInStatus();
        if (null == fillCheckInStatus || 1 == fillCheckInStatus) {
            LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
            habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getUserId, ContextCache.getUserId())
                    .eq(HabitCheckIn::getHabitId, data.getHabitId())
                    .eq(HabitCheckIn::getCheckInDate, data.getCheckInDate());
            List<HabitCheckIn> habitCheckIns = baseMapper.selectList(habitCheckInLambdaQueryWrapper);
            if (habitCheckIns.isEmpty()) {
                HabitCheckIn habitCheckIn = new HabitCheckIn();
                habitCheckIn.setId(IdUtil.getId());
                habitCheckIn.setHabitId(data.getHabitId());
                habitCheckIn.setUserId(ContextCache.getUserId());
                habitCheckIn.setCheckInDate(data.getCheckInDate());
                habitCheckIn.setCheckInType(data.getCheckInType());
                if (1 == data.getCheckInType()) {
                    habitCheckIn.setCheckInTime(LocalDateTime.now());
                } else {
                    habitCheckIn.setCheckInTime(LocalDateTime.of(data.getCheckInDate(), LocalTime.MIN));
                }
                baseMapper.insert(habitCheckIn);
            }
        } else {
            LambdaUpdateWrapper<HabitCheckIn> habitCheckInLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            habitCheckInLambdaUpdateWrapper.eq(HabitCheckIn::getHabitId, data.getHabitId())
                    .eq(HabitCheckIn::getCheckInDate, data.getCheckInDate())
                    .eq(HabitCheckIn::getUserId, ContextCache.getUserId());
            baseMapper.delete(habitCheckInLambdaUpdateWrapper);
        }
        habitService.updateStreak(data.getHabitId());
        userRankSnapshotAsyncService.refreshSingleUserRankSnapshotAsync(ContextCache.getUserId());
    }

    @Override
    public CheckInDetailResData getWeekCheckInInfo(CheckInDetailReqData data) {
        log.info("getWeekCheckInInfo userId:{}, habitId:{}, weekStart:{}, weekEnd:{}", ContextCache.getUserId(), data != null ? data.getHabitId() : null, data != null ? data.getWeekStart() : null, data != null ? data.getWeekEnd() : null);
        LocalDate qStart = data.getWeekStart();
        LocalDate qEnd = data.getWeekEnd();
        if (data.getPeriodStartDate() != null && data.getPeriodEndDate() != null) {
            LocalDate effStart = qStart.isBefore(data.getPeriodStartDate()) ? data.getPeriodStartDate() : qStart;
            LocalDate effEnd = qEnd.isAfter(data.getPeriodEndDate()) ? data.getPeriodEndDate() : qEnd;
            if (effStart.isAfter(effEnd)) {
                return new CheckInDetailResData();
            }
            qStart = effStart;
            qEnd = effEnd;
        }
        LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitCheckInLambdaQueryWrapper
                .eq(HabitCheckIn::getUserId, ContextCache.getUserId())
                .ge(HabitCheckIn::getCheckInDate, qStart)
                .le(HabitCheckIn::getCheckInDate, qEnd);
        if (StringUtils.isNotEmpty(data.getHabitId())) {
            habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getHabitId, data.getHabitId());
        }
        List<HabitCheckIn> habitCheckIns = baseMapper.selectList(habitCheckInLambdaQueryWrapper);
        int totalDays = (int) ChronoUnit.DAYS.between(qStart, qEnd) + 1;
        CheckInDetailResData checkInDetailResData = new CheckInDetailResData();
        if (!CollectionUtils.isEmpty(habitCheckIns)) {
            int checkInNum = habitCheckIns.stream().filter(Objects::nonNull).map(HabitCheckIn::getCheckInDate).distinct().toList().size();
            checkInDetailResData.setCheckInNum(checkInNum);
            checkInDetailResData.setTotalCheckInNum(totalDays);
            checkInDetailResData.setCheckInRate(new BigDecimal(checkInNum).divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            checkInDetailResData.setCheckInDate(habitCheckIns.stream().map(HabitCheckIn::getCheckInDate).toList());
        } else {
            checkInDetailResData.setTotalCheckInNum(totalDays);
        }
        return checkInDetailResData;
    }

    @Override
    public CheckInDetailResData getMonthCheckInInfo(CheckInDetailReqData data) {
        log.info("getMonthCheckInInfo userId:{}, habitId:{}, monthDate:{}", ContextCache.getUserId(), data != null ? data.getHabitId() : null, data != null ? data.getMonthDate() : null);
        LocalDate monthStartDate = data.getMonthDate();
        LocalDate monthEndDate = monthStartDate.plusMonths(1).minusDays(1);
        LocalDate qStart = monthStartDate;
        LocalDate qEnd = monthEndDate;
        if (data.getPeriodStartDate() != null && data.getPeriodEndDate() != null) {
            LocalDate effStart = monthStartDate.isBefore(data.getPeriodStartDate()) ? data.getPeriodStartDate() : monthStartDate;
            LocalDate effEnd = monthEndDate.isAfter(data.getPeriodEndDate()) ? data.getPeriodEndDate() : monthEndDate;
            if (effStart.isAfter(effEnd)) {
                return new CheckInDetailResData();
            }
            qStart = effStart;
            qEnd = effEnd;
        }
        int totalDays = (int) ChronoUnit.DAYS.between(qStart, qEnd) + 1;
        LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitCheckInLambdaQueryWrapper
                .eq(HabitCheckIn::getUserId, ContextCache.getUserId())
                .ge(HabitCheckIn::getCheckInDate, qStart)
                .le(HabitCheckIn::getCheckInDate, qEnd);
        if (StringUtils.isNotEmpty(data.getHabitId())) {
            habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getHabitId, data.getHabitId());
        }
        List<HabitCheckIn> habitCheckIns = baseMapper.selectList(habitCheckInLambdaQueryWrapper);
        CheckInDetailResData checkInDetailResData = new CheckInDetailResData();
        if (!CollectionUtils.isEmpty(habitCheckIns)) {
            int checkInNum = habitCheckIns.stream().filter(Objects::nonNull).map(HabitCheckIn::getCheckInDate).distinct().toList().size();
            checkInDetailResData.setCheckInNum(checkInNum);
            checkInDetailResData.setTotalCheckInNum(totalDays);
            checkInDetailResData.setCheckInRate(new BigDecimal(checkInNum).divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            checkInDetailResData.setCheckInDate(habitCheckIns.stream().map(HabitCheckIn::getCheckInDate).toList());
        } else {
            checkInDetailResData.setTotalCheckInNum(totalDays);
        }
        return checkInDetailResData;
    }

    @Override
    public CheckInDetailResData getYearCheckInInfo(CheckInDetailReqData data) {
        log.info("getYearCheckInInfo userId:{}, habitId:{}, yearDate:{}", ContextCache.getUserId(), data != null ? data.getHabitId() : null, data != null ? data.getYearDate() : null);
        LocalDate yearStartDate = data.getYearDate();
        LocalDate yearEndDate = yearStartDate.plusYears(1).minusDays(1);
        LocalDate qStart = yearStartDate;
        LocalDate qEnd = yearEndDate;
        if (data.getPeriodStartDate() != null && data.getPeriodEndDate() != null) {
            LocalDate effStart = yearStartDate.isBefore(data.getPeriodStartDate()) ? data.getPeriodStartDate() : yearStartDate;
            LocalDate effEnd = yearEndDate.isAfter(data.getPeriodEndDate()) ? data.getPeriodEndDate() : yearEndDate;
            if (effStart.isAfter(effEnd)) {
                return new CheckInDetailResData();
            }
            qStart = effStart;
            qEnd = effEnd;
        }
        int totalDays = (int) ChronoUnit.DAYS.between(qStart, qEnd) + 1;
        LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitCheckInLambdaQueryWrapper
                .eq(HabitCheckIn::getUserId, ContextCache.getUserId())
                .ge(HabitCheckIn::getCheckInDate, qStart)
                .le(HabitCheckIn::getCheckInDate, qEnd);
        if (StringUtils.isNotEmpty(data.getHabitId())) {
            habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getHabitId, data.getHabitId());
        }
        List<HabitCheckIn> habitCheckIns = baseMapper.selectList(habitCheckInLambdaQueryWrapper);
        CheckInDetailResData checkInDetailResData = new CheckInDetailResData();
        if (!CollectionUtils.isEmpty(habitCheckIns)) {
            int checkInNum = habitCheckIns.stream().filter(Objects::nonNull).map(HabitCheckIn::getCheckInDate).distinct().toList().size();
            checkInDetailResData.setCheckInNum(checkInNum);
            checkInDetailResData.setTotalCheckInNum(totalDays);
            checkInDetailResData.setCheckInRate(new BigDecimal(checkInNum).divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            checkInDetailResData.setCheckInDate(habitCheckIns.stream().map(HabitCheckIn::getCheckInDate).toList());
        } else {
            checkInDetailResData.setTotalCheckInNum(totalDays);
        }
        return checkInDetailResData;
    }

    @Override
    public StatisticsInfoResData getStatisticsInfo() {
        log.info("getStatisticsInfo userId:{}", ContextCache.getUserId());
        LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getUserId, ContextCache.getUserId());
        List<HabitCheckIn> habitCheckIns = baseMapper.selectList(habitCheckInLambdaQueryWrapper);
        LambdaQueryWrapper<Habit> habitLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitLambdaQueryWrapper.eq(Habit::getUserId, ContextCache.getUserId());
        List<Habit> habits = habitMapper.selectList(habitLambdaQueryWrapper);
        Map<String, String> habitIdNameMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(habits)) {
            habitIdNameMap = habits.stream().collect(Collectors.toMap(Habit::getId, Habit::getName));
        }

        StatisticsInfoResData statisticsInfoResData = new StatisticsInfoResData();
        if (!CollectionUtils.isEmpty(habitCheckIns)) {
            HabitCheckIn minCheckInDateTime = habitCheckIns.stream().filter(f -> null != f.getCheckInTime()).min(Comparator.comparing(HabitCheckIn::getCheckInTime)).get();
            HabitCheckIn maxCheckInDateTime = habitCheckIns.stream().filter(f -> null != f.getCheckInTime()).max(Comparator.comparing(HabitCheckIn::getCheckInTime)).get();
            Map<String, Long> habitIdCountMap = habitCheckIns.stream().collect(Collectors.groupingBy(HabitCheckIn::getHabitId, Collectors.counting()));
            String mostKey = habitIdCountMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            String lessKey = habitIdCountMap.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            int totalDays = (int) ChronoUnit.DAYS.between(minCheckInDateTime.getCheckInTime().toLocalDate(), LocalDate.now()) + 1;
            int checkInDays = habitCheckIns.stream().map(HabitCheckIn::getCheckInDate).distinct().toList().size();
            BigDecimal checkInRate = new BigDecimal(checkInDays).divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

            statisticsInfoResData.setFirstCheckInDateTime(NzhkDateUtil.getDateTimeStrByLocalDateTime(minCheckInDateTime.getCheckInTime()));
            statisticsInfoResData.setLastCheckInDateTime(NzhkDateUtil.getDateTimeStrByLocalDateTime(maxCheckInDateTime.getCheckInTime()));
            statisticsInfoResData.setMostHabitName(habitIdNameMap.get(mostKey));
            statisticsInfoResData.setLessHabitName(habitIdNameMap.get(lessKey));
            statisticsInfoResData.setTotalCheckInDays(totalDays);
            statisticsInfoResData.setCheckInNumDays(checkInDays);
            statisticsInfoResData.setCheckInRate(checkInRate);

            // 连续打卡天数、最长连续、里程碑、习惯排行
            List<LocalDate> distinctDates = habitCheckIns.stream().map(HabitCheckIn::getCheckInDate).filter(Objects::nonNull).distinct().sorted().toList();
            int currentStreak = computeCurrentStreak(distinctDates);
            int maxStreak = computeMaxStreak(distinctDates);
            statisticsInfoResData.setCurrentStreakDays(currentStreak);
            statisticsInfoResData.setMaxStreakDays(maxStreak);

            int[] milestones = {7, 30, 100, 365, 1000};
            List<String> achieved = new ArrayList<>();
            String next = null;
            for (int m : milestones) {
                if (checkInDays >= m) {
                    achieved.add(m + "天");
                } else if (next == null) {
                    next = m + "天";
                    break;
                }
            }
            statisticsInfoResData.setAchievedMilestones(achieved);
            statisticsInfoResData.setNextMilestone(next);

            Map<String, String> finalHabitIdNameMap = habitIdNameMap;
            List<HabitRankItem> ranking = habitIdCountMap.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(e -> new HabitRankItem(finalHabitIdNameMap.getOrDefault(e.getKey(), "未知"), e.getValue()))
                    .collect(Collectors.toList());
            statisticsInfoResData.setHabitRanking(ranking);
        }
        statisticsInfoResData.setTotalCheckInCount(CollectionUtils.isEmpty(habitCheckIns) ? 0 : habitCheckIns.size());
        statisticsInfoResData.setHabitCount(CollectionUtils.isEmpty(habits) ? 0 : habits.size());
        return statisticsInfoResData;
    }

    /**
     * 计算当前连续打卡天数：从今天（若今日已打卡）或昨天开始向前，连续多少天都有打卡
     */
    private int computeCurrentStreak(List<LocalDate> distinctDates) {
        if (CollectionUtils.isEmpty(distinctDates)) return 0;
        Set<LocalDate> dateSet = new HashSet<>(distinctDates);
        LocalDate today = LocalDate.now();
        LocalDate cursor = dateSet.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (dateSet.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    /**
     * 计算最长连续打卡天数
     */
    private int computeMaxStreak(List<LocalDate> distinctDates) {
        if (CollectionUtils.isEmpty(distinctDates)) return 0;
        int maxStreak = 1;
        int current = 1;
        for (int i = 1; i < distinctDates.size(); i++) {
            if (ChronoUnit.DAYS.between(distinctDates.get(i - 1), distinctDates.get(i)) == 1) {
                current++;
            } else {
                maxStreak = Math.max(maxStreak, current);
                current = 1;
            }
        }
        return Math.max(maxStreak, current);
    }

    @Override
    public void saveReflection(CheckInReflectionSaveReqData data) {
        String userId = ContextCache.getUserId();
        if (data == null || StringUtils.isBlank(data.getHabitId()) || data.getCheckInDate() == null) {
            throw new BizException(40000, "参数错误");
        }
        Habit habit = habitMapper.selectById(data.getHabitId());
        if (habit == null || !StringUtils.equals(habit.getUserId(), userId)) {
            throw new BizException(40400, "习惯不存在");
        }
        LambdaQueryWrapper<HabitCheckIn> q = new LambdaQueryWrapper<>();
        q.eq(HabitCheckIn::getHabitId, data.getHabitId())
                .eq(HabitCheckIn::getCheckInDate, data.getCheckInDate())
                .eq(HabitCheckIn::getUserId, userId);
        HabitCheckIn row = baseMapper.selectOne(q);
        if (row == null) {
            throw new BizException(40400, "该日期尚未打卡");
        }
        boolean touchText = data.getReflection() != null;
        boolean touchImg = Boolean.TRUE.equals(data.getUpdateImage());
        if (!touchText && !touchImg) {
            return;
        }
        LambdaUpdateWrapper<HabitCheckIn> uw = new LambdaUpdateWrapper<>();
        uw.eq(HabitCheckIn::getId, row.getId());
        if (touchText) {
            String trimmed = data.getReflection().trim();
            if (trimmed.length() > 100) {
                throw new BizException(40000, "感悟不超过100字");
            }
            uw.set(HabitCheckIn::getReflection, trimmed.isEmpty() ? null : trimmed);
        }
        if (touchImg) {
            String fid = data.getImageFileId();
            if (StringUtils.isBlank(fid)) {
                uw.set(HabitCheckIn::getReflectionImageUrl, null);
            } else {
                UploadedFile uf = fileService.getByFileId(fid.trim());
                if (uf == null || !StringUtils.equals(uf.getUserId(), userId)
                        || !StringUtils.equals(uf.getBizType(), "habit_reflection")) {
                    throw new BizException(40000, "图片无效");
                }
                uw.set(HabitCheckIn::getReflectionImageUrl, uf.getUrl());
            }
        }
        baseMapper.update(null, uw);
    }

    @Override
    public CheckInReflectionItemResData getReflection(CheckInReflectionGetReqData data) {
        String userId = ContextCache.getUserId();
        if (data == null || StringUtils.isBlank(data.getHabitId()) || data.getCheckInDate() == null) {
            throw new BizException(40000, "参数错误");
        }
        Habit habit = habitMapper.selectById(data.getHabitId());
        if (habit == null || !StringUtils.equals(habit.getUserId(), userId)) {
            throw new BizException(40400, "习惯不存在");
        }
        LambdaQueryWrapper<HabitCheckIn> q = new LambdaQueryWrapper<>();
        q.eq(HabitCheckIn::getHabitId, data.getHabitId())
                .eq(HabitCheckIn::getCheckInDate, data.getCheckInDate())
                .eq(HabitCheckIn::getUserId, userId);
        HabitCheckIn row = baseMapper.selectOne(q);
        if (row == null) {
            throw new BizException(40400, "该日期尚未打卡");
        }
        CheckInReflectionItemResData res = new CheckInReflectionItemResData();
        res.setCheckInDate(row.getCheckInDate());
        res.setReflection(row.getReflection());
        String imgUrl = row.getReflectionImageUrl();
        res.setReflectionImageUrl(StringUtils.isNotBlank(imgUrl) ? fileSignUtil.signFileUrlIfNeeded(imgUrl) : imgUrl);
        return res;
    }

    @Override
    public CheckInReflectionListResData listReflections(CheckInReflectionListReqData data) {
        String userId = ContextCache.getUserId();
        if (data == null || StringUtils.isBlank(data.getHabitId())) {
            throw new BizException(40000, "参数错误");
        }
        Habit habit = habitMapper.selectById(data.getHabitId());
        if (habit == null || !StringUtils.equals(habit.getUserId(), userId)) {
            throw new BizException(40400, "习惯不存在");
        }
        int pageNo = data.getPageNo() == null || data.getPageNo() < 1 ? 1 : data.getPageNo();
        int pageSize = data.getPageSize() == null || data.getPageSize() < 1 ? 20 : Math.min(data.getPageSize(), 50);
        LambdaQueryWrapper<HabitCheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HabitCheckIn::getUserId, userId)
                .eq(HabitCheckIn::getHabitId, data.getHabitId())
                .and(w -> w.and(w1 -> w1.isNotNull(HabitCheckIn::getReflection).ne(HabitCheckIn::getReflection, ""))
                        .or(w2 -> w2.isNotNull(HabitCheckIn::getReflectionImageUrl).ne(HabitCheckIn::getReflectionImageUrl, "")))
                .orderByDesc(HabitCheckIn::getCheckInDate);
        Page<HabitCheckIn> page = baseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        CheckInReflectionListResData res = new CheckInReflectionListResData();
        res.setTotal(page.getTotal());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            res.setRecords(Collections.emptyList());
            return res;
        }
        List<CheckInReflectionItemResData> items = page.getRecords().stream().map(r -> {
            CheckInReflectionItemResData it = new CheckInReflectionItemResData();
            it.setCheckInDate(r.getCheckInDate());
            it.setReflection(r.getReflection());
            String url = r.getReflectionImageUrl();
            it.setReflectionImageUrl(StringUtils.isNotBlank(url) ? fileSignUtil.signFileUrlIfNeeded(url) : url);
            return it;
        }).toList();
        res.setRecords(items);
        return res;
    }

    @Override
    public List<UserCheckInRankItemResData> getUserCheckInRank(String rankType) {
        String rankTypeValue = StringUtils.trimToEmpty(rankType).toLowerCase(Locale.ROOT);
        List<UserCheckInRankItemResData> rankList;
        if ("count".equals(rankTypeValue)) {
            rankList = userRankSnapshotMapper.selectRankByCount(USER_RANK_LIMIT);
        } else if ("streak".equals(rankTypeValue)) {
            rankList = userRankSnapshotMapper.selectRankByMomentum(USER_RANK_LIMIT);
        } else {
            rankList = userRankSnapshotMapper.selectRankByDays(USER_RANK_LIMIT);
        }
        if (CollectionUtils.isEmpty(rankList)) {
            recalibrateUserRankSnapshots();
            if ("count".equals(rankTypeValue)) {
                rankList = userRankSnapshotMapper.selectRankByCount(USER_RANK_LIMIT);
            } else if ("streak".equals(rankTypeValue)) {
                rankList = userRankSnapshotMapper.selectRankByMomentum(USER_RANK_LIMIT);
            } else {
                rankList = userRankSnapshotMapper.selectRankByDays(USER_RANK_LIMIT);
            }
        }
        if (CollectionUtils.isEmpty(rankList)) {
            return Collections.emptyList();
        }
        for (int i = 0; i < rankList.size(); i++) {
            UserCheckInRankItemResData item = rankList.get(i);
            item.setRankNo(i + 1);
            if (StringUtils.isNotBlank(item.getAvatarUrl())) {
                item.setAvatarUrl(fileSignUtil.signFileUrlIfNeeded(item.getAvatarUrl()));
            }
            if (StringUtils.isBlank(item.getNickName())) {
                item.setNickName("微信用户");
            }
            if (item.getRankValue() == null) {
                item.setRankValue(0L);
            }
            if (item.getCurrentStreakDays() == null) {
                item.setCurrentStreakDays(0);
            }
            if (item.getMaxStreakDays() == null) {
                item.setMaxStreakDays(0);
            }
        }
        return rankList;
    }

    @Override
    public void recalibrateUserRankSnapshots() {
        userRankSnapshotMapper.deleteAllSnapshots();
        List<UserCheckInAggregateItem> aggregateItems = baseMapper.selectUserCheckInAggregateItems();
        if (CollectionUtils.isEmpty(aggregateItems)) {
            return;
        }
        List<UserCheckInDateItem> dateItems = baseMapper.selectUserCheckInDateItems();
        Map<String, List<UserCheckInDateItem>> userDateMap = dateItems.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(UserCheckInDateItem::getUserId));
        for (UserCheckInAggregateItem aggregateItem : aggregateItems) {
            String userId = aggregateItem.getUserId();
            if (StringUtils.isBlank(userId)) {
                continue;
            }
            List<UserCheckInDateItem> userDateItems = userDateMap.getOrDefault(userId, Collections.emptyList());
            upsertSnapshot(userId, aggregateItem, userDateItems);
        }
    }

    private void refreshSingleUserRankSnapshot(String userId) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        LambdaQueryWrapper<HabitCheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HabitCheckIn::getUserId, userId);
        List<HabitCheckIn> records = baseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(records)) {
            userRankSnapshotMapper.deleteById(userId);
            return;
        }
        Set<LocalDate> dateSet = records.stream()
                .map(HabitCheckIn::getCheckInDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
        UserCheckInAggregateItem aggregateItem = new UserCheckInAggregateItem();
        aggregateItem.setUserId(userId);
        aggregateItem.setCheckInCount((long) records.size());
        aggregateItem.setCheckInDays((long) dateSet.size());
        List<UserCheckInDateItem> userDateItems = dateSet.stream().map(d -> {
            UserCheckInDateItem item = new UserCheckInDateItem();
            item.setUserId(userId);
            item.setCheckInDate(d);
            return item;
        }).toList();
        upsertSnapshot(userId, aggregateItem, userDateItems);
    }

    private void upsertSnapshot(String userId, UserCheckInAggregateItem aggregateItem, List<UserCheckInDateItem> userDateItems) {
        List<LocalDate> distinctDates = userDateItems.stream()
                .map(UserCheckInDateItem::getCheckInDate)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        int currentStreak = computeCurrentStreak(distinctDates);
        int maxStreak = computeMaxStreak(distinctDates);
        BigDecimal momentumScore = new BigDecimal(currentStreak).multiply(new BigDecimal("0.7"))
                .add(new BigDecimal(maxStreak).multiply(new BigDecimal("0.3")));

        UserRankSnapshot snapshot = new UserRankSnapshot();
        snapshot.setUserId(userId);
        snapshot.setCheckInDays(aggregateItem.getCheckInDays() == null ? 0 : aggregateItem.getCheckInDays().intValue());
        snapshot.setCheckInCount(aggregateItem.getCheckInCount() == null ? 0 : aggregateItem.getCheckInCount().intValue());
        snapshot.setCurrentStreakDays(currentStreak);
        snapshot.setMaxStreakDays(maxStreak);
        snapshot.setMomentumScore(momentumScore);
        snapshot.setLastCheckInDate(distinctDates.isEmpty() ? null : distinctDates.get(distinctDates.size() - 1));
        userRankSnapshotMapper.upsertSnapshot(snapshot);
    }
}
