-- 用户排行预计算表（支撑天数/次数/坚持势头三种排行）
CREATE TABLE IF NOT EXISTS user_rank_snapshot (
  user_id VARCHAR(64) PRIMARY KEY,                          -- 用户ID（关联 wx_user.id）
  check_in_days INTEGER NOT NULL DEFAULT 0,                -- 打卡天数（按日期去重）
  check_in_count INTEGER NOT NULL DEFAULT 0,               -- 打卡次数（不去重）
  current_streak_days INTEGER NOT NULL DEFAULT 0,          -- 当前连续天数（今天打卡则从今天向前，否则从昨天向前）
  max_streak_days INTEGER NOT NULL DEFAULT 0,              -- 历史最大连续天数
  momentum_score NUMERIC(10, 2) NOT NULL DEFAULT 0,        -- 坚持势头混合分（当前连续*0.7 + 历史最大*0.3）
  last_check_in_date DATE NULL,                            -- 最近打卡日期
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 快照最后更新时间
);

COMMENT ON TABLE user_rank_snapshot IS '用户排行预计算快照表（支撑天数/次数/坚持势头排行）';
COMMENT ON COLUMN user_rank_snapshot.user_id IS '用户ID（关联 wx_user.id）';
COMMENT ON COLUMN user_rank_snapshot.check_in_days IS '打卡天数（按日期去重）';
COMMENT ON COLUMN user_rank_snapshot.check_in_count IS '打卡次数（不去重）';
COMMENT ON COLUMN user_rank_snapshot.current_streak_days IS '当前连续天数（今天打卡则从今天向前，否则从昨天向前）';
COMMENT ON COLUMN user_rank_snapshot.max_streak_days IS '历史最大连续天数';
COMMENT ON COLUMN user_rank_snapshot.momentum_score IS '坚持势头混合分（当前连续*0.7 + 历史最大*0.3）';
COMMENT ON COLUMN user_rank_snapshot.last_check_in_date IS '最近打卡日期';
COMMENT ON COLUMN user_rank_snapshot.updated_at IS '快照最后更新时间';

-- 按天数排行（同分按 user_id 稳定排序）
CREATE INDEX IF NOT EXISTS idx_user_rank_days
  ON user_rank_snapshot (check_in_days DESC, user_id ASC);

-- 按次数排行（同分按 user_id 稳定排序）
CREATE INDEX IF NOT EXISTS idx_user_rank_count
  ON user_rank_snapshot (check_in_count DESC, user_id ASC);

-- 按坚持势头排行（先看混合分，再看当前连续、历史最大，最后按 user_id 稳定排序）
CREATE INDEX IF NOT EXISTS idx_user_rank_momentum
  ON user_rank_snapshot (momentum_score DESC, current_streak_days DESC, max_streak_days DESC, user_id ASC);
