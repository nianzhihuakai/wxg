-- 订阅消息发送记录表
-- 执行前请备份数据库
-- 适用于 PostgreSQL

CREATE TABLE IF NOT EXISTS subscribe_message_log (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64),
    openid VARCHAR(128) NOT NULL,
    template_id VARCHAR(128) NOT NULL,
    send_content TEXT,
    biz_type VARCHAR(64) NOT NULL,
    biz_id VARCHAR(64),
    extra_data VARCHAR(512),
    send_status SMALLINT NOT NULL,
    errcode INTEGER,
    errmsg VARCHAR(256),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subscribe_message_log_user ON subscribe_message_log(user_id);
CREATE INDEX IF NOT EXISTS idx_subscribe_message_log_openid ON subscribe_message_log(openid);
CREATE INDEX IF NOT EXISTS idx_subscribe_message_log_created ON subscribe_message_log(created_at);
CREATE INDEX IF NOT EXISTS idx_subscribe_message_log_biz ON subscribe_message_log(biz_type, biz_id);

COMMENT ON TABLE subscribe_message_log IS '订阅消息发送记录表';
COMMENT ON COLUMN subscribe_message_log.user_id IS '用户表 id (wx_user.id)';
COMMENT ON COLUMN subscribe_message_log.openid IS '用户 openid';
COMMENT ON COLUMN subscribe_message_log.send_content IS '发送的模板数据 JSON';
COMMENT ON COLUMN subscribe_message_log.template_id IS '微信模板 ID';
COMMENT ON COLUMN subscribe_message_log.biz_type IS '业务类型：habit_remind-习惯提醒等';
COMMENT ON COLUMN subscribe_message_log.biz_id IS '业务ID，如 habit_id';
COMMENT ON COLUMN subscribe_message_log.extra_data IS '扩展数据，如 habitName';
COMMENT ON COLUMN subscribe_message_log.send_status IS '发送状态：0-失败，1-成功';
COMMENT ON COLUMN subscribe_message_log.errcode IS '微信返回错误码，成功时为 null';
COMMENT ON COLUMN subscribe_message_log.errmsg IS '微信返回错误信息，成功时为 null';

-- 若表已存在，执行以下语句新增字段：
-- ALTER TABLE subscribe_message_log ADD COLUMN IF NOT EXISTS user_id VARCHAR(64);
-- ALTER TABLE subscribe_message_log ADD COLUMN IF NOT EXISTS send_content TEXT;
-- CREATE INDEX IF NOT EXISTS idx_subscribe_message_log_user ON subscribe_message_log(user_id);
