CREATE TABLE IF NOT EXISTS student_status_history (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    from_status VARCHAR(64),
    to_status VARCHAR(64) NOT NULL,
    changed_to_major VARCHAR(64),
    reason VARCHAR(1000),
    changed_by VARCHAR(64) NOT NULL,
    changed_by_role VARCHAR(32) NOT NULL
);
