package com.nzhk.wxg.business.habit.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 习惯连续打卡天数计算器
 * 详见 docs/habit_streak_calculation.md
 */
public final class StreakCalculator {

    private StreakCalculator() {
    }

    /**
     * 计算习惯的连续打卡天数
     *
     * @param checkInDates   该习惯的所有打卡日期集合
     * @param habitStartDate 习惯开始日期
     * @param habitEndDate   习惯结束日期，null 或 2099-12-31 表示长期
     * @param freqType       频次类型：fixed/weekly/monthly
     * @param freq           频次：fixed 时为 "1,2,3,4,5,6,7"；weekly 时为 "3"；monthly 时为 "15"
     * @param today          今日日期
     * @param todayCheckedIn 今日是否已打卡
     * @return 连续打卡天数
     */
    public static int compute(List<LocalDate> checkInDates,
                             LocalDate habitStartDate,
                             LocalDate habitEndDate,
                             String freqType,
                             String freq,
                             LocalDate today,
                             boolean todayCheckedIn) {
        if (checkInDates == null || checkInDates.isEmpty()) {
            return 0;
        }
        Set<LocalDate> checkInSet = checkInDates.stream().filter(d -> d != null).collect(Collectors.toSet());
        LocalDate calcStart = habitStartDate != null ? habitStartDate : today;
        LocalDate calcEnd = resolveEndDate(habitEndDate, today);

        String type = StringUtils.isNotBlank(freqType) ? freqType : "fixed";
        if ("fixed".equals(type)) {
            return computeForFixed(checkInSet, calcStart, calcEnd, freq, today, todayCheckedIn);
        }
        if ("weekly".equals(type)) {
            return computeForWeekly(checkInSet, calcStart, calcEnd, freq, today, todayCheckedIn);
        }
        if ("monthly".equals(type)) {
            return computeForMonthly(checkInSet, calcStart, calcEnd, freq, today, todayCheckedIn);
        }
        return computeForFixed(checkInSet, calcStart, calcEnd, freq, today, todayCheckedIn);
    }

    /**
     * Fixed 类型：从参考日起向前遍历，连续多少个有效日都打了卡
     * 参考日 = 今日已打卡则今日，否则昨日
     */
    private static int computeForFixed(Set<LocalDate> checkInDates,
                                      LocalDate habitStartDate,
                                      LocalDate habitEndDate,
                                      String freq,
                                      LocalDate today,
                                      boolean todayCheckedIn) {
        Set<Integer> freqDays = parseFreqDays(freq);
        if (freqDays.isEmpty()) {
            freqDays = Set.of(1, 2, 3, 4, 5, 6, 7);
        }
        LocalDate cursor = todayCheckedIn ? today : today.minusDays(1);
        if (cursor.isBefore(habitStartDate) || cursor.isAfter(habitEndDate)) {
            return 0;
        }

        int streak = 0;
        while (!cursor.isBefore(habitStartDate) && !cursor.isAfter(habitEndDate)) {
            int dayOfWeek = cursor.getDayOfWeek().getValue();
            if (freqDays.contains(dayOfWeek)) {
                if (checkInDates.contains(cursor)) {
                    streak++;
                    cursor = cursor.minusDays(1);
                } else {
                    break;
                }
            } else {
                cursor = cursor.minusDays(1);
            }
        }
        return streak;
    }

    /**
     * Weekly 类型：连续多少周都达到当周目标
     */
    private static int computeForWeekly(Set<LocalDate> checkInDates,
                                       LocalDate habitStartDate,
                                       LocalDate habitEndDate,
                                       String freq,
                                       LocalDate today,
                                       boolean todayCheckedIn) {
        int target = parseIntSafe(freq, 1);
        target = Math.min(7, Math.max(1, target));

        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        int streak = 0;
        while (!weekStart.plusDays(6).isBefore(habitStartDate)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekStart.isAfter(habitEndDate)) {
                weekStart = weekStart.minusWeeks(1);
                continue;
            }
            LocalDate finalWeekStart = weekStart;
            long count = checkInDates.stream()
                    .filter(d -> !d.isBefore(finalWeekStart) && !d.isAfter(weekEnd))
                    .count();
            if (count >= target) {
                streak++;
                weekStart = weekStart.minusWeeks(1);
            } else {
                break;
            }
        }
        return streak;
    }

    /**
     * Monthly 类型：连续多少月都达到当月目标
     */
    private static int computeForMonthly(Set<LocalDate> checkInDates,
                                        LocalDate habitStartDate,
                                        LocalDate habitEndDate,
                                        String freq,
                                        LocalDate today,
                                        boolean todayCheckedIn) {
        int target = parseIntSafe(freq, 1);
        target = Math.min(31, Math.max(1, target));

        LocalDate monthStart = today.withDayOfMonth(1);
        int streak = 0;
        while (!monthStart.plusMonths(1).minusDays(1).isBefore(habitStartDate)) {
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            if (monthStart.isAfter(habitEndDate)) {
                monthStart = monthStart.minusMonths(1);
                continue;
            }
            LocalDate finalMonthStart = monthStart;
            long count = checkInDates.stream()
                    .filter(d -> !d.isBefore(finalMonthStart) && !d.isAfter(monthEnd))
                    .count();
            if (count >= target) {
                streak++;
                monthStart = monthStart.minusMonths(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private static Set<Integer> parseFreqDays(String freq) {
        if (StringUtils.isBlank(freq)) {
            return Set.of();
        }
        return Arrays.stream(freq.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        int n = Integer.parseInt(s);
                        return n >= 1 && n <= 7 ? n : null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(n -> n != null)
                .collect(Collectors.toSet());
    }

    private static int parseIntSafe(String s, int defaultVal) {
        if (StringUtils.isBlank(s)) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static LocalDate resolveEndDate(LocalDate habitEndDate, LocalDate today) {
        if (habitEndDate == null) {
            return today;
        }
        String endStr = habitEndDate.toString();
        if (endStr.startsWith("2099") || endStr.startsWith("9999")) {
            return today;
        }
        return habitEndDate.isBefore(today) ? habitEndDate : today;
    }
}
