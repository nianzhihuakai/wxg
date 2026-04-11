-- =============================================================================
-- 周期目标表：用户按周/月/年设定的打卡类目标，可关联多个习惯，服务端存「收获」富文本
-- =============================================================================

CREATE TABLE IF NOT EXISTS period_goal (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    title VARCHAR(256) NOT NULL DEFAULT '',
    period_type VARCHAR(16) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    metric_type VARCHAR(32) NOT NULL,
    target_value INT NOT NULL,
    habit_ids TEXT NOT NULL,
    harvest_html TEXT,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_period_goal_user_list ON period_goal(user_id, period_type, period_start, period_end, status);

-- -----------------------------------------------------------------------------
-- 表与字段注释（PostgreSQL，执行建表后可单独执行本段补注释）
-- -----------------------------------------------------------------------------
COMMENT ON TABLE period_goal IS '周期目标：按周/月/年绑定习惯与目标值，进度由客户端根据打卡接口计算';

COMMENT ON COLUMN period_goal.id IS '主键，服务端生成 UUID（无横线）';
COMMENT ON COLUMN period_goal.user_id IS '所属用户，与习惯等业务一致';
COMMENT ON COLUMN period_goal.title IS '目标标题，可默认「未命名目标」';
COMMENT ON COLUMN period_goal.period_type IS '周期维度：week=周，month=月，year=年';
COMMENT ON COLUMN period_goal.period_start IS '该目标覆盖区间的开始日期（含），yyyy-MM-dd 语义';
COMMENT ON COLUMN period_goal.period_end IS '该目标覆盖区间的结束日期（含），与 period_start 同周期口径';
COMMENT ON COLUMN period_goal.metric_type IS '统计方式：checkin_days=自然日去重打卡天数合计；checkin_count=各习惯打卡次数相加';
COMMENT ON COLUMN period_goal.target_value IS '目标值：与 metric_type 对应，表示需完成的天数或次数（≥1）';
COMMENT ON COLUMN period_goal.habit_ids IS '关联习惯 ID 列表，JSON 数组字符串，如 ["id1","id2"]，顺序无强制';
COMMENT ON COLUMN period_goal.harvest_html IS '收获：富文本 HTML，可为空；服务端限制长度（与接口校验一致）';
COMMENT ON COLUMN period_goal.status IS '1=有效；删除接口为物理删除行，此列保留供扩展或历史数据';
COMMENT ON COLUMN period_goal.created_at IS '创建时间';
COMMENT ON COLUMN period_goal.updated_at IS '最后更新时间';
