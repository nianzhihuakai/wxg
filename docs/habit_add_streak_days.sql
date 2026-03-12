-- 习惯表新增连续打卡相关字段
-- 执行前请备份数据库
-- 适用于 PostgreSQL

-- 当前连续打卡天数
ALTER TABLE habit ADD COLUMN IF NOT EXISTS streak_days INTEGER DEFAULT 0;
COMMENT ON COLUMN habit.streak_days IS '当前连续打卡天数';

-- 历史最高连续打卡天数（用于成就徽章）
ALTER TABLE habit ADD COLUMN IF NOT EXISTS max_streak_days INTEGER DEFAULT 0;
COMMENT ON COLUMN habit.max_streak_days IS '历史最高连续打卡天数';
