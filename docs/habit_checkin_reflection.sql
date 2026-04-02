-- 习惯打卡感悟：习惯开关 + 打卡记录上的感悟与配图（PostgreSQL）
-- 执行前请确认表名为 habit / habit_check_in（与当前 MyBatis 实体一致）

ALTER TABLE habit
    ADD COLUMN IF NOT EXISTS prompt_checkin_reflection BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN habit.prompt_checkin_reflection IS '打卡成功后是否弹出写感悟，默认否';

ALTER TABLE habit_check_in
    ADD COLUMN IF NOT EXISTS reflection VARCHAR(100);

ALTER TABLE habit_check_in
    ADD COLUMN IF NOT EXISTS reflection_image_url VARCHAR(1024);

COMMENT ON COLUMN habit_check_in.reflection IS '打卡感悟，最多100字，可空';
COMMENT ON COLUMN habit_check_in.reflection_image_url IS '感悟配图访问 URL，可空，最多一张';
