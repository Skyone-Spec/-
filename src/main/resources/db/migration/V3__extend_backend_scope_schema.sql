ALTER TABLE student_profile
    ADD COLUMN IF NOT EXISTS status VARCHAR(64),
    ADD COLUMN IF NOT EXISTS major_changed_to VARCHAR(64),
    ADD COLUMN IF NOT EXISTS encrypted_native_place VARCHAR(128),
    ADD COLUMN IF NOT EXISTS encrypted_household_address VARCHAR(128),
    ADD COLUMN IF NOT EXISTS encrypted_supervisor VARCHAR(128);

UPDATE student_profile
SET status = COALESCE(status, CASE WHEN graduated THEN 'GRADUATED' ELSE 'ACTIVE' END);

ALTER TABLE knowledge_document
    ADD COLUMN IF NOT EXISTS source_file_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS audience_scope VARCHAR(64),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(64);

ALTER TABLE certificate_request
    ADD COLUMN IF NOT EXISTS current_approver_role VARCHAR(32) NOT NULL DEFAULT 'COUNSELOR',
    ADD COLUMN IF NOT EXISTS approval_level INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS withdrawal_deadline TIMESTAMP;

CREATE TABLE IF NOT EXISTS approval_action_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(64) NOT NULL,
    operator_role VARCHAR(32) NOT NULL,
    action VARCHAR(32) NOT NULL,
    from_status VARCHAR(32) NOT NULL,
    to_status VARCHAR(32) NOT NULL,
    comment VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS admin_operation_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(64) NOT NULL,
    operator_role VARCHAR(32) NOT NULL,
    module VARCHAR(64) NOT NULL,
    action VARCHAR(64) NOT NULL,
    target VARCHAR(255) NOT NULL,
    result VARCHAR(32) NOT NULL,
    detail VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS data_import_task (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    task_type VARCHAR(64) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_rows INTEGER NOT NULL DEFAULT 0,
    success_rows INTEGER NOT NULL DEFAULT 0,
    failed_rows INTEGER NOT NULL DEFAULT 0,
    owner_id BIGINT NOT NULL,
    owner_name VARCHAR(64) NOT NULL,
    error_summary VARCHAR(1000)
);
