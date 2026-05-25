CREATE TABLE IF NOT EXISTS student_award_support_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    assessment_academic_year VARCHAR(32) NOT NULL,
    award_name VARCHAR(128) NOT NULL,
    batch_name VARCHAR(128),
    award_level VARCHAR(64),
    award_grade VARCHAR(64),
    award_amount DECIMAL(12, 2),
    award_type VARCHAR(64)
);

CREATE INDEX IF NOT EXISTS idx_student_award_support_record_student
    ON student_award_support_record (student_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS student_competition_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    award_date DATE NOT NULL,
    competition_name VARCHAR(255) NOT NULL,
    competition_level VARCHAR(64),
    competition_grade VARCHAR(64),
    competition_category VARCHAR(64),
    organizer VARCHAR(255),
    advisor_teacher_info VARCHAR(255),
    remarks VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_student_competition_record_student
    ON student_competition_record (student_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS student_innovation_entrepreneurship_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    project_code VARCHAR(64),
    project_name VARCHAR(255) NOT NULL,
    college_name VARCHAR(128),
    project_status VARCHAR(64),
    project_level VARCHAR(64),
    completion_grade VARCHAR(64),
    participant_role VARCHAR(64),
    project_type VARCHAR(64),
    project_batch VARCHAR(64),
    participant_count INTEGER,
    advisor_teacher VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_student_innovation_record_student
    ON student_innovation_entrepreneurship_record (student_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS student_social_practice_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    practice_start_date DATE NOT NULL,
    practice_end_date DATE,
    practice_team_name VARCHAR(255) NOT NULL,
    practice_theme VARCHAR(255) NOT NULL,
    practice_location VARCHAR(255),
    practice_team_level VARCHAR(64),
    advisor_teacher VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_student_social_practice_record_student
    ON student_social_practice_record (student_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS student_student_work_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    organization_name VARCHAR(255) NOT NULL,
    position_name VARCHAR(128) NOT NULL,
    work_description VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_student_student_work_record_student
    ON student_student_work_record (student_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS student_volunteer_service_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    service_date DATE NOT NULL,
    service_project VARCHAR(255) NOT NULL,
    service_location VARCHAR(255),
    service_duration_hours DECIMAL(8, 2),
    service_organization_name VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_student_volunteer_service_record_student
    ON student_volunteer_service_record (student_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS student_skill_certificate_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    certificate_name VARCHAR(255) NOT NULL,
    obtained_date DATE NOT NULL,
    certificate_level VARCHAR(64),
    description VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_student_skill_certificate_record_student
    ON student_skill_certificate_record (student_id, updated_at DESC);
