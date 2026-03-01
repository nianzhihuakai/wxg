CREATE TABLE IF NOT EXISTS uploaded_file (
    id VARCHAR(64) PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL UNIQUE,
    user_id VARCHAR(64) NOT NULL,
    biz_type VARCHAR(32) NOT NULL,
    original_name VARCHAR(255),
    storage_path VARCHAR(1024) NOT NULL,
    url VARCHAR(512) NOT NULL,
    mime_type VARCHAR(128),
    file_size BIGINT,
    width INTEGER,
    height INTEGER,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_uploaded_file_user ON uploaded_file(user_id);

CREATE TABLE IF NOT EXISTS journal (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    journal_date DATE NOT NULL,
    mood_value VARCHAR(64) NOT NULL,
    mood_label VARCHAR(64),
    subject VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    client_request_id VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_journal_user_date ON journal(user_id, journal_date);
CREATE INDEX IF NOT EXISTS idx_journal_user_request ON journal(user_id, client_request_id);

CREATE TABLE IF NOT EXISTS journal_image (
    id VARCHAR(64) PRIMARY KEY,
    journal_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    file_id VARCHAR(64) NOT NULL,
    image_url VARCHAR(512) NOT NULL,
    sort_order INTEGER NOT NULL,
    width INTEGER,
    height INTEGER,
    file_size BIGINT,
    mime_type VARCHAR(64),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_journal_image_journal ON journal_image(journal_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uq_journal_image_file ON journal_image(journal_id, file_id);
