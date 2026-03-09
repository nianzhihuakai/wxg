package com.nzhk.wxg.business.habit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nzhk.wxg.business.habit.bean.*;
import com.nzhk.wxg.business.habit.entity.Habit;
import com.nzhk.wxg.business.habit.service.IHabitService;
import com.nzhk.wxg.business.habitcheckin.entity.HabitCheckIn;
import com.nzhk.wxg.common.cache.ContextCache;
import com.nzhk.wxg.common.utils.BeanConvertUtil;
import com.nzhk.wxg.common.utils.IdUtil;
import com.nzhk.wxg.mapper.HabitCheckInMapper;
import com.nzhk.wxg.mapper.HabitMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 习惯表 服务实现类
 * </p>
 *
 * @author lxy
 * @since 2026-01-28
 */
@Slf4j
@Service
public class HabitServiceImpl extends ServiceImpl<HabitMapper, Habit> implements IHabitService {

    @Resource
    private HabitCheckInMapper habitCheckInMapper;

    @Override
    public List<HabitListResData> getHabits(HabitListReqData data) {
        log.info("getHabits userId:{}", ContextCache.getUserId());
        String userId = ContextCache.getUserId();
        LocalDate now = LocalDate.now();
        List<HabitListResData> habits = baseMapper.selectHabitList(userId, now, data.getHabitTypeId());

        int dayOfWeek = now.getDayOfWeek().getValue();
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        LambdaQueryWrapper<HabitCheckIn> allWrapper = new LambdaQueryWrapper<>();
        allWrapper.eq(HabitCheckIn::getUserId, userId);
        List<HabitCheckIn> allCheckIns = habitCheckInMapper.selectList(allWrapper);
        Map<String, List<HabitCheckIn>> habitIdMap = allCheckIns.stream().collect(Collectors.groupingBy(HabitCheckIn::getHabitId));

        Map<String, Long> weekCountMap = allCheckIns.stream()
                .filter(c -> !c.getCheckInDate().isBefore(weekStart) && !c.getCheckInDate().isAfter(weekEnd))
                .collect(Collectors.groupingBy(HabitCheckIn::getHabitId, Collectors.counting()));
        Map<String, Long> monthCountMap = allCheckIns.stream()
                .filter(c -> !c.getCheckInDate().isBefore(monthStart) && !c.getCheckInDate().isAfter(monthEnd))
                .collect(Collectors.groupingBy(HabitCheckIn::getHabitId, Collectors.counting()));

        habits.forEach(f -> {
            ShowState state = computeShowState(f, dayOfWeek, weekCountMap, monthCountMap);
            f.setShowToday(state.showToday);
            f.setShowReason(state.showReason);
        });

        habits.forEach(f -> {
            if (StringUtils.isNotEmpty(f.getCheckInId())) {
                f.setAlreadyCheckedInToday(true);
            }
            List<HabitCheckIn> habitCheckInsById = habitIdMap.get(f.getId());
            if (!CollectionUtils.isEmpty(habitCheckInsById)) {
                f.setCheckInNum(habitCheckInsById.size());
                int totalDays = (int) ChronoUnit.DAYS.between(f.getCreateTime().toLocalDate(), now) + 1;
                f.setTotalCheckInNum(totalDays);
            }
            f.setWeekCheckInCount(weekCountMap.getOrDefault(f.getId(), 0L).intValue());
            f.setMonthCheckInCount(monthCountMap.getOrDefault(f.getId(), 0L).intValue());
        });
        // 今日休息(rest)或已完成(weekly_done/monthly_done)的习惯排到列表最后
        habits.sort((a, b) -> {
            boolean aTail = StringUtils.isNotBlank(a.getShowReason());
            boolean bTail = StringUtils.isNotBlank(b.getShowReason());
            if (aTail == bTail) return 0;
            return aTail ? 1 : -1;
        });
        return habits;
    }

    /**
     * 计算习惯今日展示状态：是否可打卡、不可打卡时的原因
     */
    private ShowState computeShowState(HabitListResData h, int dayOfWeek, Map<String, Long> weekCountMap, Map<String, Long> monthCountMap) {
        String type = StringUtils.isNotBlank(h.getCheckInFrequencyType()) ? h.getCheckInFrequencyType() : "fixed";
        String freq = h.getCheckInFrequency();
        if ("fixed".equals(type)) {
            if (StringUtils.isBlank(freq)) return new ShowState(true, null);
            Set<String> days = Set.of(freq.split(","));
            boolean showToday = days.contains(String.valueOf(dayOfWeek));
            return new ShowState(showToday, showToday ? null : "rest");
        }
        if ("weekly".equals(type)) {
            int target = parseIntSafe(freq, 7);
            if (target <= 0) return new ShowState(true, null);
            long count = weekCountMap.getOrDefault(h.getId(), 0L);
            boolean showToday = count < target;
            return new ShowState(showToday, showToday ? null : "weekly_done");
        }
        if ("monthly".equals(type)) {
            int target = parseIntSafe(freq, 31);
            if (target <= 0) return new ShowState(true, null);
            long count = monthCountMap.getOrDefault(h.getId(), 0L);
            boolean showToday = count < target;
            return new ShowState(showToday, showToday ? null : "monthly_done");
        }
        return new ShowState(true, null);
    }

    private static class ShowState {
        final boolean showToday;
        final String showReason;

        ShowState(boolean showToday, String showReason) {
            this.showToday = showToday;
            this.showReason = showReason;
        }
    }

    private String parseFrequencyForSave(String type, String freq) {
        if ("fixed".equals(type)) {
            return StringUtils.isNotBlank(freq) ? freq.trim() : "1,2,3,4,5,6,7";
        }
        if ("weekly".equals(type)) {
            int n = parseIntSafe(freq, 1);
            return String.valueOf(Math.min(7, Math.max(1, n)));
        }
        if ("monthly".equals(type)) {
            int n = parseIntSafe(freq, 1);
            return String.valueOf(Math.min(31, Math.max(1, n)));
        }
        return StringUtils.isNotBlank(freq) ? freq.trim() : "1,2,3,4,5,6,7";
    }

    private int parseIntSafe(String s, int defaultVal) {
        if (StringUtils.isBlank(s)) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    @Override
    public List<HabitListResData> getArchiveHabits(HabitListReqData data) {
        log.info("getArchiveHabits userId:{}", ContextCache.getUserId());
        List<HabitListResData> habits = baseMapper.selectArchiveHabitList(ContextCache.getUserId(), LocalDate.now(), data.getHabitTypeId());
        LambdaQueryWrapper<HabitCheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HabitCheckIn::getUserId, ContextCache.getUserId());
        List<HabitCheckIn> allCheckIns = habitCheckInMapper.selectList(wrapper);
        Map<String, List<HabitCheckIn>> habitIdMap = allCheckIns.stream().collect(Collectors.groupingBy(HabitCheckIn::getHabitId));
        habits.forEach(f -> {
            List<HabitCheckIn> checkIns = habitIdMap.get(f.getId());
            if (CollectionUtils.isEmpty(checkIns)) return;
            LocalDate calcStart = f.getStartDate() != null ? f.getStartDate() : (f.getCreateTime() != null ? f.getCreateTime().toLocalDate() : LocalDate.now());
            LocalDate rawEnd = f.getEndDate();
            boolean isLongTerm = rawEnd != null && (rawEnd.toString().startsWith("2099") || rawEnd.toString().startsWith("9999"));
            LocalDate calcEnd = (rawEnd == null || isLongTerm) && f.getArchiveDateTime() != null
                    ? f.getArchiveDateTime().toLocalDate()
                    : (rawEnd != null ? rawEnd : LocalDate.now());
            if (calcEnd.isBefore(calcStart)) calcEnd = calcStart;
            int totalDays = (int) ChronoUnit.DAYS.between(calcStart, calcEnd) + 1;
            LocalDate finalCalcEnd = calcEnd;
            long checkInNum = checkIns.stream()
                    .map(HabitCheckIn::getCheckInDate)
                    .filter(d -> d != null && !d.isBefore(calcStart) && !d.isAfter(finalCalcEnd))
                    .distinct()
                    .count();
            f.setTotalCheckInNum(totalDays);
            f.setCheckInNum((int) checkInNum);
        });
        return habits;
    }

    /** 状态：已归档 */
    private static final int STATUS_ARCHIVED = 2;

    @Override
    public HabitDetailResData getHabitById(HabitDetailReqData data) {
        log.info("getHabitById habitId:{}", data != null ? data.getId() : null);
        Habit habit = baseMapper.selectById(data.getId());
        HabitDetailResData habitDetailResData = BeanConvertUtil.copySingleProperties(habit, HabitDetailResData::new);

        LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getUserId, ContextCache.getUserId())
                .eq(HabitCheckIn::getHabitId, data.getId());
        List<HabitCheckIn> habitCheckIns = habitCheckInMapper.selectList(habitCheckInLambdaQueryWrapper);

        if (!CollectionUtils.isEmpty(habitCheckIns)) {
            LocalDate calcStart;
            LocalDate calcEnd;
            if (Integer.valueOf(STATUS_ARCHIVED).equals(habit.getStatus())) {
                // 已归档：按习惯周期 startDate~endDate 计算
                calcStart = habit.getStartDate() != null ? habit.getStartDate() : habit.getCreateTime().toLocalDate();
                LocalDate rawEnd = habit.getEndDate();
                boolean isLongTerm = rawEnd != null && (rawEnd.toString().startsWith("2099") || rawEnd.toString().startsWith("9999"));
                calcEnd = (rawEnd == null || isLongTerm) && habit.getArchiveDateTime() != null
                        ? habit.getArchiveDateTime().toLocalDate()
                        : (rawEnd != null ? rawEnd : LocalDate.now());
                if (calcEnd.isBefore(calcStart)) calcEnd = calcStart;
            } else {
                // 未归档：创建时间到今天
                calcStart = habitDetailResData.getCreateTime().toLocalDate();
                calcEnd = LocalDate.now();
            }
            int totalDays = (int) ChronoUnit.DAYS.between(calcStart, calcEnd) + 1;
            LocalDate finalCalcEnd = calcEnd;
            long checkInNum = habitCheckIns.stream()
                    .map(HabitCheckIn::getCheckInDate)
                    .filter(d -> d != null && !d.isBefore(calcStart) && !d.isAfter(finalCalcEnd))
                    .distinct()
                    .count();
            habitDetailResData.setTotalCheckInNum(totalDays);
            habitDetailResData.setCheckInNum((int) checkInNum);
            if (totalDays > 0) {
                habitDetailResData.setCheckInRate(new BigDecimal(checkInNum).divide(new BigDecimal(totalDays), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            }
        }

        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        long weekCount = habitCheckIns.stream()
                .filter(c -> !c.getCheckInDate().isBefore(weekStart) && !c.getCheckInDate().isAfter(weekEnd))
                .count();
        long monthCount = habitCheckIns.stream()
                .filter(c -> !c.getCheckInDate().isBefore(monthStart) && !c.getCheckInDate().isAfter(monthEnd))
                .count();
        habitDetailResData.setWeekCheckInCount((int) weekCount);
        habitDetailResData.setMonthCheckInCount((int) monthCount);

        return habitDetailResData;
    }

    @Override
    public void addHabit(AddHabitReqData data) {
        log.info("addHabit userId:{}, name:{}, habitTypeId:{}", ContextCache.getUserId(), data != null ? data.getName() : null, data != null ? data.getHabitTypeId() : null);
        Habit habit = new Habit();
        habit.setId(IdUtil.getId());
        habit.setName(data.getName());
        habit.setHabitTypeId(data.getHabitTypeId());
        habit.setUserId(ContextCache.getUserId());
        habit.setStartDate(data.getStartDate());
        habit.setEndDate(data.getEndDate());
        habit.setRemindFlag(Boolean.TRUE.equals(data.getRemindFlag()));
        habit.setRemindTime(data.getRemindTime());
        String type = StringUtils.isNotBlank(data.getCheckInFrequencyType()) ? data.getCheckInFrequencyType() : "fixed";
        habit.setCheckInFrequencyType(type);
        String freq = data.getCheckInFrequency();
        habit.setCheckInFrequency(parseFrequencyForSave(type, freq));
        baseMapper.insert(habit);
    }

    @Override
    public void updateHabit(UpdateHabitReqData data) {
        LambdaUpdateWrapper<Habit> habitLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        habitLambdaUpdateWrapper.eq(Habit::getId, data.getHabitId())
                .set(Habit::getHabitTypeId, data.getHabitTypeId())
                .set(Habit::getName, data.getName())
                .set(Habit::getStartDate, data.getStartDate())
                .set(Habit::getEndDate, data.getEndDate())
                .set(Habit::getRemindFlag, Boolean.TRUE.equals(data.getRemindFlag()))
                .set(Habit::getRemindTime, data.getRemindTime());
        if (data.getCheckInFrequencyType() != null || data.getCheckInFrequency() != null) {
            String type = data.getCheckInFrequencyType() != null ? data.getCheckInFrequencyType() : "fixed";
            String freq = parseFrequencyForSave(type, data.getCheckInFrequency());
            habitLambdaUpdateWrapper.set(Habit::getCheckInFrequencyType, type);
            habitLambdaUpdateWrapper.set(Habit::getCheckInFrequency, freq);
        }
        baseMapper.update(habitLambdaUpdateWrapper);
    }

    @Override
    public void archiveHabit(UpdateHabitReqData data) {
        LambdaUpdateWrapper<Habit> habitLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        habitLambdaUpdateWrapper.eq(Habit::getId, data.getHabitId())
                .set(Habit::getStatus, 2)
                .set(Habit::getArchiveDateTime, LocalDateTime.now());
        baseMapper.update(habitLambdaUpdateWrapper);
    }

    @Override
    public void deleteHabit(UpdateHabitReqData data) {
        LambdaUpdateWrapper<HabitCheckIn> habitCheckInLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        habitCheckInLambdaUpdateWrapper.eq(HabitCheckIn::getHabitId, data.getHabitId());
        habitCheckInMapper.delete(habitCheckInLambdaUpdateWrapper);
        baseMapper.deleteById(data.getHabitId());
    }

    @Override
    public void autoArchiveEndedHabits() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Habit> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Habit::getEndDate, today)
                .eq(Habit::getStatus, 1)
                .set(Habit::getStatus, 2)
                .set(Habit::getArchiveDateTime, now);
        baseMapper.update(null, updateWrapper);
    }

    @Override
    public List<Habit> listHabitsNeedRemind(String remindTime) {
        if (remindTime == null || remindTime.isEmpty()) {
            return List.of();
        }
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Habit> q = new LambdaQueryWrapper<>();
        q.eq(Habit::getRemindFlag, true)
                .eq(Habit::getRemindTime, remindTime)
                .eq(Habit::getStatus, 1)
                .le(Habit::getStartDate, today)
                .ge(Habit::getEndDate, today);
        return baseMapper.selectList(q);
    }
}
