CREATE TABLE IF NOT EXISTS student_work_log (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(64) NOT NULL,
    category VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    workload_score INTEGER NOT NULL,
    work_date DATE NOT NULL,
    recorder_name VARCHAR(64) NOT NULL,
    recorder_role VARCHAR(32) NOT NULL
);
