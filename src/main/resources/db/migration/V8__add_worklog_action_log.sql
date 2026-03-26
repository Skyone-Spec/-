CREATE TABLE IF NOT EXISTS student_work_log_action_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    work_log_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(64) NOT NULL,
    operator_role VARCHAR(32) NOT NULL,
    action VARCHAR(32) NOT NULL,
    detail VARCHAR(1000)
);
