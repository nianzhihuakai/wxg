# 习惯连续打卡（Streak）计算逻辑

## 一、概述

**连续打卡天数（streak）**：从最近的有效打卡日起，向前追溯连续多少个有效日都完成了打卡。

本文档描述 streak 的计算规则、实现方案及与成就徽章的关系。

---

## 二、频次类型与 Streak 定义

习惯有三种打卡频次类型（`check_in_frequency_type`），streak 含义不同：

| 频次类型 | 说明 | Streak 含义 |
|---------|------|-------------|
| **fixed** | 固定周几打卡（如周一三五） | 从今天往前，连续多少个「有效打卡日」都打了卡 |
| **weekly** | 每周 N 次 | 连续多少周都达到当周目标 |
| **monthly** | 每月 N 次 | 连续多少月都达到当月目标 |

---

## 三、Fixed 类型 Streak 计算（核心逻辑）

### 3.1 有效打卡日

- 由 `check_in_frequency` 定义，格式为逗号分隔的 1-7（1=周一，7=周日）
- 例如 `1,3,5` 表示周一、周三、周五为有效日

### 3.2 计算规则

从**今天**开始向前遍历日期：

1. 若当天是有效日且已打卡 → streak +1，继续向前
2. 若当天是有效日但未打卡 → 中断，返回当前 streak
3. 若当天不是有效日 → 跳过，继续向前
4. 若日期早于习惯 `start_date` → 中断，返回当前 streak

### 3.3 伪代码

```java
/**
 * 计算 fixed 类型习惯的连续打卡天数
 * @param checkInDates 该习惯的所有打卡日期集合（LocalDate）
 * @param freqDays 有效日集合，如 {1,3,5} 表示周一三五
 * @param today 今日日期
 * @param habitStartDate 习惯开始日期
 */
int computeStreakForFixed(Set<LocalDate> checkInDates, Set<Integer> freqDays, 
                         LocalDate today, LocalDate habitStartDate) {
    int streak = 0;
    LocalDate cursor = today;
    
    while (!cursor.isBefore(habitStartDate)) {
        int dayOfWeek = cursor.getDayOfWeek().getValue();  // 1=Mon, 7=Sun
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
```

### 3.4 示例

习惯：周一三五打卡，今日为周三且已打卡

- 周三（有效日，已打卡）→ streak = 1，向前
- 周二（非有效日）→ 跳过
- 周一（有效日，已打卡）→ streak = 2，向前
- 周日（非有效日）→ 跳过
- 周六（非有效日）→ 跳过
- 周五（有效日，已打卡）→ streak = 3，向前
- ……

最终 streak = 3

---

## 四、Weekly 与 Monthly 类型

### 4.1 Weekly

- 从本周往前，连续多少周都达到「每周 N 次」的目标
- 计算：遍历每一周，若当周打卡次数 >= 目标则 streak +1，否则中断

### 4.2 Monthly

- 从本月往前，连续多少月都达到「每月 N 次」的目标
- 计算：遍历每一月，若当月打卡次数 >= 目标则 streak +1，否则中断

---

## 五、实现方案：更新 streak_days 字段

### 5.1 推荐方案

采用**打卡时更新 habit 表的 streak_days 字段**，而非每次查询实时计算。

原因：

- 后续要做成就徽章（如「连续打卡 7/30/100 天」），需在打卡后立即判断是否达标
- 成就页展示多习惯时，直接读字段性能更好
- 可顺带维护 `max_streak_days`（历史最高连续），支持「最长连续 X 天」类徽章

### 5.2 更新时机

在 `fillCheckIn`（打卡/取消打卡）完成后：

1. 根据习惯类型与打卡记录计算新的 streak
2. 更新 `habit.streak_days`
3. 若 `newStreak > habit.max_streak_days`，更新 `habit.max_streak_days`
4. 判断是否解锁新徽章（如 streak_days >= 7 且尚未获得 7 天徽章）

### 5.3 取消打卡

用户取消某日打卡时：

1. 删除/取消对应打卡记录
2. 重新计算 streak（从今日往前，因今日未打卡会中断）
3. 更新 `habit.streak_days`

`max_streak_days` 只增不减，取消打卡不影响历史最高。

---

## 六、边界情况

| 情况 | 处理 |
|-----|------|
| 今日尚未打卡 |  streak 从**昨天**开始算，不含今日 |
| 今日已打卡 |  streak 包含今日 |
| 习惯刚创建、从未打卡 |  streak = 0 |
| 补卡 |  补卡后重新计算 streak，可能使 streak 增加 |
| 习惯周期（start_date ~ end_date） |  只统计周期内的有效日 |

---

## 七、数据库字段

habit 表建议字段（若尚未存在）：

```sql
-- streak_days: 当前连续打卡天数
ALTER TABLE habit ADD COLUMN IF NOT EXISTS streak_days INTEGER DEFAULT 0;
COMMENT ON COLUMN habit.streak_days IS '当前连续打卡天数';

-- max_streak_days: 历史最高连续天数（用于成就徽章）
ALTER TABLE habit ADD COLUMN IF NOT EXISTS max_streak_days INTEGER DEFAULT 0;
COMMENT ON COLUMN habit.max_streak_days IS '历史最高连续打卡天数';
```

---

## 八、涉及模块

| 模块 | 改动 |
|-----|------|
| 后端 HabitServiceImpl | fillCheckIn 后调用 streak 计算并更新 habit |
| 后端 HabitCheckInServiceImpl | fillCheckIn 完成后通知/调用 habit 的 streak 更新 |
| 后端 getHabits / getHabitById | 返回 habit 时包含 streak_days（来自数据库） |
| 前端 parseHabit | 解析 streakDays 用于展示 |
| 前端 首页卡片 | 展示「🔥 X天连续」 |
| 前端 习惯详情页 | 统计区展示 streak |
| 成就徽章模块（后续） | 根据 streak_days / max_streak_days 判断解锁 |

---

## 九、成就徽章示例

| 徽章 | 条件 |
|-----|------|
| 初露锋芒 | streak_days >= 7 |
| 持之以恒 | streak_days >= 30 |
| 百日筑基 | streak_days >= 100 |
| 巅峰时刻 | max_streak_days >= 365 |

徽章解锁逻辑在 `fillCheckIn` 完成后、streak 更新后执行。
