-- 习惯表新增打卡频次类型字段
-- 执行前请备份数据库
-- 固定=fixed(按周几), 每周=weekly(每周N次), 每月=monthly(每月N次)

-- PostgreSQL
ALTER TABLE habit ADD COLUMN IF NOT EXISTS check_in_frequency_type VARCHAR(20) DEFAULT 'fixed';
COMMENT ON COLUMN habit.check_in_frequency_type IS '打卡频次类型：fixed-固定周几，weekly-每周N次，monthly-每月N次';
