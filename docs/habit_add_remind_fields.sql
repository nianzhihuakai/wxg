-- 习惯表新增提醒相关字段
-- 执行前请备份数据库
-- 适用于 PostgreSQL

-- 添加是否开启提醒字段，默认关闭
ALTER TABLE habit ADD COLUMN IF NOT EXISTS remind_flag BOOLEAN DEFAULT FALSE;

-- 添加提醒时间字段，格式 HH:mm，如 09:30
ALTER TABLE habit ADD COLUMN IF NOT EXISTS remind_time VARCHAR(5);

-- 添加注释
COMMENT ON COLUMN habit.remind_flag IS '是否开启提醒：false-关闭，true-开启';
COMMENT ON COLUMN habit.remind_time IS '提醒时间，格式 HH:mm';
