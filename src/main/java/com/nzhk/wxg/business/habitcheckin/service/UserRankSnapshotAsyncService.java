package com.nzhk.wxg.business.habitcheckin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nzhk.wxg.business.habitcheckin.entity.HabitCheckIn;
import com.nzhk.wxg.business.habitcheckin.entity.UserRankSnapshot;
import com.nzhk.wxg.mapper.HabitCheckInMapper;
import com.nzhk.wxg.mapper.UserRankSnapshotMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserRankSnapshotAsyncService {

    @Resource
    private HabitCheckInMapper habitCheckInMapper;

    @Resource
    private UserRankSnapshotMapper userRankSnapshotMapper;

    @Async
    public void refreshSingleUserRankSnapshotAsync(String userId) {
        try {
            refreshSingleUserRankSnapshot(userId);
        } catch (Exception e) {
            log.error("async refresh user rank snapshot error, userId={}", userId, e);
        }
    }

    private void refreshSingleUserRankSnapshot(String userId) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        LambdaQueryWrapper<HabitCheckIn> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HabitCheckIn::getUserId, userId);
        List<HabitCheckIn> records = habitCheckInMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(records)) {
            userRankSnapshotMapper.deleteById(userId);
            return;
        }
        Set<LocalDate> dateSet = records.stream()
                .map(HabitCheckIn::getCheckInDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
        List<LocalDate> distinctDates = dateSet.stream().toList();
        int currentStreak = computeCurrentStreak(distinctDates);
        int maxStreak = computeMaxStreak(distinctDates);
        BigDecimal momentumScore = new BigDecimal(currentStreak).multiply(new BigDecimal("0.7"))
                .add(new BigDecimal(maxStreak).multiply(new BigDecimal("0.3")));

        UserRankSnapshot snapshot = new UserRankSnapshot();
        snapshot.setUserId(userId);
        snapshot.setCheckInCount(records.size());
        snapshot.setCheckInDays(dateSet.size());
        snapshot.setCurrentStreakDays(currentStreak);
        snapshot.setMaxStreakDays(maxStreak);
        snapshot.setMomentumScore(momentumScore);
        snapshot.setLastCheckInDate(distinctDates.isEmpty() ? null : distinctDates.get(distinctDates.size() - 1));
        userRankSnapshotMapper.upsertSnapshot(snapshot);
    }

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

    private int computeMaxStreak(List<LocalDate> distinctDates) {
        if (CollectionUtils.isEmpty(distinctDates)) return 0;
        int maxStreak = 1;
        int current = 1;
        for (int i = 1; i < distinctDates.size(); i++) {
            if (distinctDates.get(i - 1).plusDays(1).equals(distinctDates.get(i))) {
                current++;
            } else {
                maxStreak = Math.max(maxStreak, current);
                current = 1;
            }
        }
        return Math.max(maxStreak, current);
    }
}
