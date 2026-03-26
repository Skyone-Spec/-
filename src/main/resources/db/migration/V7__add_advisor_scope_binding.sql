CREATE TABLE IF NOT EXISTS advisor_scope_binding (
    id BIGSERIAL PRIMARY KEY,
    advisor_username VARCHAR(64) NOT NULL,
    advisor_name VARCHAR(64),
    grade VARCHAR(32),
    class_name VARCHAR(32),
    student_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_advisor_scope_binding_advisor
    ON advisor_scope_binding (advisor_username, advisor_name);

CREATE INDEX IF NOT EXISTS idx_advisor_scope_binding_student
    ON advisor_scope_binding (student_id);
