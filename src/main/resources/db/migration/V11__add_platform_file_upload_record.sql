CREATE TABLE IF NOT EXISTS platform_file_upload_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    biz_type VARCHAR(64) NOT NULL,
    biz_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    uploaded_by_id BIGINT NOT NULL,
    uploaded_by VARCHAR(64) NOT NULL
);
