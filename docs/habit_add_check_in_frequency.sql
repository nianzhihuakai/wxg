-- 习惯表新增打卡频次字段
-- 执行前请备份数据库
-- 格式：逗号分隔的星期数字，1=周一 7=周日，如 "1,2,3,4,5,6,7" 表示每天

-- PostgreSQL
ALTER TABLE habit ADD COLUMN IF NOT EXISTS check_in_frequency VARCHAR(20) DEFAULT '1,2,3,4,5,6,7';
COMMENT ON COLUMN habit.check_in_frequency IS '打卡频次，逗号分隔 1-7，1=周一 7=周日';

-- MySQL (如使用 MySQL 可执行以下语句替代上面)
-- ALTER TABLE habit ADD COLUMN check_in_frequency VARCHAR(20) DEFAULT '1,2,3,4,5,6,7' COMMENT '打卡频次，逗号分隔 1-7，1=周一 7=周日';
