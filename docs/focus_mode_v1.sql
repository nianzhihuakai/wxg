-- V1: focus_session 支持倒计时/正计时模式
ALTER TABLE focus_session
    ADD COLUMN IF NOT EXISTS focus_mode VARCHAR(16) NOT NULL DEFAULT 'COUNTDOWN';

ALTER TABLE focus_session
    ADD COLUMN IF NOT EXISTS target_minutes INTEGER NULL;

ALTER TABLE focus_session
    ALTER COLUMN expected_end_time DROP NOT NULL;

ALTER TABLE focus_session
    ALTER COLUMN planned_minutes DROP NOT NULL;

ALTER TABLE focus_session
    DROP CONSTRAINT IF EXISTS focus_session_planned_minutes_check;

ALTER TABLE focus_session
    ADD CONSTRAINT focus_session_planned_minutes_check
    CHECK (planned_minutes IS NULL OR (planned_minutes >= 1 AND planned_minutes <= 720));

COMMENT ON COLUMN focus_session.focus_mode IS '专注模式: COUNTDOWN 倒计时, STOPWATCH 正计时';
COMMENT ON COLUMN focus_session.target_minutes IS '正计时目标分钟(可选, 用于提醒)';
