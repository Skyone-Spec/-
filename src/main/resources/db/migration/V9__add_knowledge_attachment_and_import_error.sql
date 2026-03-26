CREATE TABLE IF NOT EXISTS knowledge_attachment (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    knowledge_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    file_size BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    uploaded_by VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS data_import_error_item (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    task_id BIGINT NOT NULL,
    row_number INTEGER NOT NULL,
    field_name VARCHAR(64) NOT NULL,
    error_message VARCHAR(500) NOT NULL,
    raw_value VARCHAR(500)
);
