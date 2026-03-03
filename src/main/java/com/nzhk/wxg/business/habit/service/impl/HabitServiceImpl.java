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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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
        List<HabitListResData> habits = baseMapper.selectHabitList(ContextCache.getUserId(), LocalDate.now(), data.getHabitTypeId());
        LambdaQueryWrapper<HabitCheckIn> habitCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        habitCheckInLambdaQueryWrapper.eq(HabitCheckIn::getUserId, ContextCache.getUserId());
        List<HabitCheckIn> habitCheckIns = habitCheckInMapper.selectList(habitCheckInLambdaQueryWrapper);
        Map<String, List<HabitCheckIn>> habitIdMap = habitCheckIns.stream().collect(Collectors.groupingBy(HabitCheckIn::getHabitId));

        habits.forEach(f->{
            if (StringUtils.isNotEmpty(f.getCheckInId())) {
                f.setAlreadyCheckedInToday(true);
            }
            List<HabitCheckIn> habitCheckInsById = habitIdMap.get(f.getId());
            if (!CollectionUtils.isEmpty(habitCheckInsById)) {
                f.setCheckInNum(habitCheckInsById.size());
                int totalDays = (int) ChronoUnit.DAYS.between(f.getCreateTime().toLocalDate(), LocalDate.now()) + 1;
                f.setTotalCheckInNum(totalDays);
//                if (0 != totalDays) {
//                    f.setCheckInRate(new BigDecimal(habitCheckInsById.size()).divide(new BigDecimal(totalDays),2, RoundingMode.HALF_UP));
//                }
            }
        });
        return habits;
    }

    @Override
    public List<HabitListResData> getArchiveHabits(HabitListReqData data) {
        log.info("getArchiveHabits userId:{}", ContextCache.getUserId());
        List<HabitListResData> habits = baseMapper.selectArchiveHabitList(ContextCache.getUserId(), LocalDate.now(), data.getHabitTypeId());
        return habits;
    }

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
            habitDetailResData.setCheckInNum(habitCheckIns.size());
            int totalDays = (int) ChronoUnit.DAYS.between(habitDetailResData.getCreateTime().toLocalDate(), LocalDate.now()) + 1;
            habitDetailResData.setTotalCheckInNum(totalDays);
            if (0 != totalDays) {
                habitDetailResData.setCheckInRate(new BigDecimal(habitCheckIns.size()).divide(new BigDecimal(totalDays),2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
            }
        }

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
        baseMapper.insert(habit);
    }

    @Override
    public void updateHabit(UpdateHabitReqData data) {
        LambdaUpdateWrapper<Habit> habitLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        habitLambdaUpdateWrapper.eq(Habit::getId, data.getHabitId())
                .set(Habit::getHabitTypeId, data.getHabitTypeId())
                .set(Habit::getName, data.getName())
                .set(Habit::getStartDate, data.getStartDate())
                .set(Habit::getEndDate, data.getEndDate());
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
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Habit> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Habit::getEndDate, yesterday)
                .eq(Habit::getStatus, 1)
                .set(Habit::getStatus, 2)
                .set(Habit::getArchiveDateTime, now);
        baseMapper.update(null, updateWrapper);
    }
}
