CREATE TABLE IF NOT EXISTS student_portrait (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL UNIQUE,
    honors VARCHAR(255),
    competitions VARCHAR(255),
    social_practice VARCHAR(255),
    research_experience VARCHAR(255),
    discipline_records VARCHAR(255),
    daily_performance VARCHAR(255),
    grade_rank INTEGER,
    major_rank INTEGER
);
