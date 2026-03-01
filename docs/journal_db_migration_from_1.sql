-- ============================================================
-- 增量迁移脚本（基于已执行的 1.sql）
-- 目的：
-- 1) 补齐上传接口依赖的 uploaded_file 表
-- 2) 确保 journal_image 具备 file_size/mime_type 字段（幂等）
-- ============================================================

BEGIN;

-- 1) 上传文件记录表（当前后端 file/upload 依赖此表）
CREATE TABLE IF NOT EXISTS uploaded_file (
  id            VARCHAR(64)    NOT NULL,
  file_id       VARCHAR(64)    NOT NULL,
  user_id       VARCHAR(64)    NOT NULL,
  biz_type      VARCHAR(32)    NOT NULL,
  original_name VARCHAR(255)   DEFAULT NULL,
  storage_path  VARCHAR(1024)  NOT NULL,
  url           TEXT           NOT NULL,
  mime_type     VARCHAR(128)   DEFAULT NULL,
  file_size     BIGINT         DEFAULT NULL,
  width         INTEGER        DEFAULT NULL,
  height        INTEGER        DEFAULT NULL,
  status        SMALLINT       NOT NULL DEFAULT 1,
  created_at    TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uq_uploaded_file_file_id UNIQUE (file_id)
);

CREATE INDEX IF NOT EXISTS idx_uploaded_file_user_id ON uploaded_file(user_id);
CREATE INDEX IF NOT EXISTS idx_uploaded_file_status ON uploaded_file(status);
CREATE INDEX IF NOT EXISTS idx_uploaded_file_biz_type ON uploaded_file(biz_type);

COMMENT ON TABLE uploaded_file IS '上传文件记录表';
COMMENT ON COLUMN uploaded_file.id IS '主键';
COMMENT ON COLUMN uploaded_file.file_id IS '对外文件ID';
COMMENT ON COLUMN uploaded_file.user_id IS '上传用户ID';
COMMENT ON COLUMN uploaded_file.biz_type IS '业务类型，如 journal';
COMMENT ON COLUMN uploaded_file.original_name IS '原文件名';
COMMENT ON COLUMN uploaded_file.storage_path IS '服务器本地存储绝对路径';
COMMENT ON COLUMN uploaded_file.url IS '访问地址';
COMMENT ON COLUMN uploaded_file.mime_type IS 'MIME类型';
COMMENT ON COLUMN uploaded_file.file_size IS '文件大小（字节）';
COMMENT ON COLUMN uploaded_file.width IS '图片宽度';
COMMENT ON COLUMN uploaded_file.height IS '图片高度';
COMMENT ON COLUMN uploaded_file.status IS '状态：0-已删除，1-正常';
COMMENT ON COLUMN uploaded_file.created_at IS '创建时间';

-- 2) 兼容性兜底：若某环境 journal_image 缺列，则补齐
ALTER TABLE journal_image
  ADD COLUMN IF NOT EXISTS file_size BIGINT;

ALTER TABLE journal_image
  ADD COLUMN IF NOT EXISTS mime_type VARCHAR(64);

COMMIT;
