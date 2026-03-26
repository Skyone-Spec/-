CREATE TABLE user_account (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    wechat_open_id VARCHAR(128)
);

CREATE TABLE student_profile (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_no VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    major VARCHAR(64),
    grade VARCHAR(32),
    class_name VARCHAR(32),
    degree_level VARCHAR(32),
    encrypted_id_card_no VARCHAR(128),
    encrypted_phone VARCHAR(128),
    email VARCHAR(128),
    graduated BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE knowledge_document (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    official_url VARCHAR(500),
    published BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE party_progress_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    current_stage VARCHAR(64) NOT NULL,
    stage_start_date DATE NOT NULL,
    completed_actions VARCHAR(500),
    next_action VARCHAR(500)
);

CREATE TABLE notice (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    tag VARCHAR(100),
    target_grade VARCHAR(64),
    target_major VARCHAR(64),
    target_graduate_only BOOLEAN NOT NULL DEFAULT FALSE,
    publish_time TIMESTAMP NOT NULL
);

CREATE TABLE certificate_request (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    certificate_type VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason VARCHAR(500),
    generated_pdf_path VARCHAR(255)
);

CREATE TABLE academic_warning_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    student_id BIGINT NOT NULL,
    module_name VARCHAR(100) NOT NULL,
    required_credits INTEGER NOT NULL,
    earned_credits INTEGER NOT NULL,
    recommended_courses VARCHAR(500)
);
