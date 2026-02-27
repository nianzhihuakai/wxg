package com.nzhk.wxg.business.habitcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.habit.entity.Habit;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailReqData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInDetailResData;
import com.nzhk.wxg.business.habitcheckin.bean.CheckInReqData;
import com.nzhk.wxg.business.habitcheckin.bean.StatisticsInfoResData;
import com.nzhk.wxg.business.habitcheckin.entity.HabitCheckIn;
import com.nzhk.wxg.business.habitcheckin.service.IHabitCheckInService;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.common.utils.NzhkDateUtil;
import com.nzhk.wxg.mapper.HabitCheckInMapper;
import com.nzhk.wxg.mapper.HabitMapper;
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

    @Resource
    private HabitMapper habitMapper;

    @Override
    public void checkIn(CheckInReqData data) {
        log.info("checkIn userId:{}, habitId:{}", ContextCache.getUserId(), data != null ? data.getHabitId() : null);
        HabitCheckIn habitCheckIn = new HabitCheckIn();
        habitCheckIn.setId(IdUtil.getId());
        habitCheckIn.setHabitId(data.getHabitId());
        habitCheckIn.setUserId(ContextCache.getUserId());
        habitCheckIn.setCheckInDate(LocalDate.now());
        habitCheckIn.setCheckInTime(LocalDateTime.now());
        baseMapper.insert(habitCheckIn);
    }

    @Override
    public void fillCheckIn(CheckInReqData data) {
        log.info("fillCheckIn userId:{}, data:{}", ContextCache.getUserId(), data);
        Integer fillCheckInStatus = data.getFillCheckInStatus();
        if (null == fillCheckInStatus || 1 == fillCheckInStatus) {
            HabitCheckIn habitCheckIn = new HabitCheckIn();
            habitCheckIn.setId(IdUtil.getId());
            habitCheckIn.setHabitId(data.getHabitId());
            habitCheckIn.setUserId(ContextCache.getUserId());
            habitCheckIn.setCheckInDate(data.getCheckInDate());
            habitCheckIn.setCheckInTime(LocalDateTime.of(data.getCheckInDate(), LocalTime.MIN));
            baseMapper.insert(habitCheckIn);
        } else {
            LambdaUpdateWrapper<HabitCheckIn> habitCheckInLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            habitCheckInLambdaUpdateWrapper.eq(HabitCheckIn::getHabitId, data.getHabitId())
                    .eq(HabitCheckIn::getCheckInDate, data.getCheckInDate())
                    .eq(HabitCheckIn::getUserId, ContextCache.getUserId());
            baseMapper.delete(habitCheckInLambdaUpdateWrapper);
        }

    }

    @Override
    public CheckInDetailResData getWeekCheckInInfo(CheckInDetailReqData data) {
        log.info("getWeekCheckInInfo userId:{}, habitId:{}, weekStart:{}, weekEnd:{}", ContextCache.getUserId(), data != null ? data.getHabitId() : null, data != null ? data.getWeekStart() : null, data != null ? data.getWeekEnd() : null);
        LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitCheckInLambdaQueryWrapper
                .eq(HabitCheckIn::getUserId, ContextCache.getUserId())
                .ge(HabitCheckIn::getCheckInDate, data.getWeekStart())
                .le(HabitCheckIn::getCheckInDate, data.getWeekEnd());
        if (StringUtils.isNotEmpty(data.getHabitId())) {
            habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getHabitId, data.getHabitId());
        }
        List<HabitCheckIn> habitCheckIns = baseMapper.selectList(habitCheckInLambdaQueryWrapper);
        CheckInDetailResData checkInDetailResData = new CheckInDetailResData();
        if (!CollectionUtils.isEmpty(habitCheckIns)) {
            int checkInNum = habitCheckIns.stream().filter(Objects::nonNull).map(HabitCheckIn::getCheckInDate).distinct().toList().size();
            checkInDetailResData.setCheckInNum(checkInNum);
            int totalDays = 7;
            checkInDetailResData.setTotalCheckInNum(totalDays);
            checkInDetailResData.setCheckInRate(new BigDecimal(checkInNum).divide(new BigDecimal(totalDays),2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            checkInDetailResData.setCheckInDate(habitCheckIns.stream().map(HabitCheckIn::getCheckInDate).toList());
        }
        return checkInDetailResData;
    }

    @Override
    public CheckInDetailResData getMonthCheckInInfo(CheckInDetailReqData data) {
        log.info("getMonthCheckInInfo userId:{}, habitId:{}, monthDate:{}", ContextCache.getUserId(), data != null ? data.getHabitId() : null, data != null ? data.getMonthDate() : null);
        LocalDate monthStartDate = data.getMonthDate();
        LocalDate monthEndDate = monthStartDate.plusMonths(1).minusDays(1);
        int dayOfMonth = monthEndDate.getDayOfMonth();
        LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitCheckInLambdaQueryWrapper
                .eq(HabitCheckIn::getUserId, ContextCache.getUserId())
                .ge(HabitCheckIn::getCheckInDate, monthStartDate)
                .le(HabitCheckIn::getCheckInDate, monthEndDate);
        if (StringUtils.isNotEmpty(data.getHabitId())) {
            habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getHabitId, data.getHabitId());
        }
        List<HabitCheckIn> habitCheckIns = baseMapper.selectList(habitCheckInLambdaQueryWrapper);
        CheckInDetailResData checkInDetailResData = new CheckInDetailResData();
        if (!CollectionUtils.isEmpty(habitCheckIns)) {
            int checkInNum = habitCheckIns.stream().filter(Objects::nonNull).map(HabitCheckIn::getCheckInDate).distinct().toList().size();
            checkInDetailResData.setCheckInNum(checkInNum);
            checkInDetailResData.setTotalCheckInNum(dayOfMonth);
            checkInDetailResData.setCheckInRate(new BigDecimal(checkInNum).divide(new BigDecimal(dayOfMonth),2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            checkInDetailResData.setCheckInDate(habitCheckIns.stream().map(HabitCheckIn::getCheckInDate).toList());
        }
        return checkInDetailResData;
    }

    @Override
    public CheckInDetailResData getYearCheckInInfo(CheckInDetailReqData data) {
        log.info("getYearCheckInInfo userId:{}, habitId:{}, yearDate:{}", ContextCache.getUserId(), data != null ? data.getHabitId() : null, data != null ? data.getYearDate() : null);
        LocalDate yearStartDate = data.getYearDate();
        LocalDate yearEndDate = yearStartDate.plusYears(1).minusDays(1);
        int dayOfYear = yearEndDate.getDayOfYear();
        LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitCheckInLambdaQueryWrapper
                .eq(HabitCheckIn::getUserId, ContextCache.getUserId())
                .ge(HabitCheckIn::getCheckInDate, yearStartDate)
                .le(HabitCheckIn::getCheckInDate, yearEndDate);
        if (StringUtils.isNotEmpty(data.getHabitId())) {
            habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getHabitId, data.getHabitId());
        }
        List<HabitCheckIn> habitCheckIns = baseMapper.selectList(habitCheckInLambdaQueryWrapper);
        CheckInDetailResData checkInDetailResData = new CheckInDetailResData();
        if (!CollectionUtils.isEmpty(habitCheckIns)) {
            int checkInNum = habitCheckIns.stream().filter(Objects::nonNull).map(HabitCheckIn::getCheckInDate).distinct().toList().size();
            checkInDetailResData.setCheckInNum(checkInNum);
            checkInDetailResData.setTotalCheckInNum(dayOfYear);
            checkInDetailResData.setCheckInRate(new BigDecimal(checkInNum).divide(new BigDecimal(dayOfYear),2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            checkInDetailResData.setCheckInDate(habitCheckIns.stream().map(HabitCheckIn::getCheckInDate).toList());
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
                    .max(Map.Entry.comparingByValue()) // 找到值（出现次数）最大的entry
                    .map(Map.Entry::getKey) // 获取这个entry的key
                    .orElse(null);
            String lessKey = habitIdCountMap.entrySet().stream()
                    .min(Map.Entry.comparingByValue()) // 找到值（出现次数）最大的entry
                    .map(Map.Entry::getKey) // 获取这个entry的key
                    .orElse(null);

            int totalDays = (int) ChronoUnit.DAYS.between(minCheckInDateTime.getCheckInTime().toLocalDate(), LocalDate.now()) + 1;
            int checkInDays = habitCheckIns.stream().map(HabitCheckIn::getCheckInDate).distinct().toList().size();
            BigDecimal checkInRate = new BigDecimal(checkInDays).divide(new BigDecimal(totalDays),2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

            statisticsInfoResData.setFirstCheckInDateTime(NzhkDateUtil.getDateTimeStrByLocalDateTime(minCheckInDateTime.getCheckInTime()));
            statisticsInfoResData.setLastCheckInDateTime(NzhkDateUtil.getDateTimeStrByLocalDateTime(maxCheckInDateTime.getCheckInTime()));
            statisticsInfoResData.setMostHabitName(habitIdNameMap.get(mostKey));
            statisticsInfoResData.setLessHabitName(habitIdNameMap.get(lessKey));
            statisticsInfoResData.setTotalCheckInDays(totalDays);
            statisticsInfoResData.setCheckInNumDays(checkInDays);
            statisticsInfoResData.setCheckInRate(checkInRate);
        }
        return statisticsInfoResData;
    }
}
