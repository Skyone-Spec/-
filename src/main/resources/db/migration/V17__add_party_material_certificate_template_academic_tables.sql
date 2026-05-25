-- V17: 添加党团材料提交、操作日志、证明模板、学业管理相关表

-- 党团材料提交表
CREATE TABLE IF NOT EXISTS party_material_submission (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    flow_type VARCHAR(100) NOT NULL,
    stage_name VARCHAR(100) NOT NULL,
    material_type VARCHAR(100) NOT NULL,
    title VARCHAR(500),
    content TEXT,
    attachment_path VARCHAR(500),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    review_comment VARCHAR(1000),
    reviewer_id BIGINT,
    reviewer_name VARCHAR(50),
    reviewed_at TIMESTAMP,
    withdrawal_deadline TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 党团操作日志表
CREATE TABLE IF NOT EXISTS party_action_log (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    flow_type VARCHAR(100) NOT NULL,
    stage_name VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(100),
    target_id BIGINT,
    operator_id BIGINT NOT NULL,
    operator_name VARCHAR(50) NOT NULL,
    operator_role VARCHAR(50) NOT NULL,
    detail TEXT,
    operated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 证明模板表
CREATE TABLE IF NOT EXISTS certificate_template (
    id BIGSERIAL PRIMARY KEY,
    template_code VARCHAR(100) NOT NULL UNIQUE,
    template_name VARCHAR(200) NOT NULL,
    certificate_type VARCHAR(100) NOT NULL,
    template_content TEXT,
    template_file_path VARCHAR(500),
    output_format VARCHAR(50) DEFAULT 'PDF',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    updated_by VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 学业培养方案表
CREATE TABLE IF NOT EXISTS academic_program (
    id BIGSERIAL PRIMARY KEY,
    program_code VARCHAR(100) NOT NULL UNIQUE,
    program_name VARCHAR(200) NOT NULL,
    major VARCHAR(50) NOT NULL,
    grade VARCHAR(20) NOT NULL,
    total_credits INTEGER NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 学业培养方案模块表
CREATE TABLE IF NOT EXISTS academic_program_module (
    id BIGSERIAL PRIMARY KEY,
    program_id BIGINT NOT NULL,
    module_code VARCHAR(100) NOT NULL,
    module_name VARCHAR(200) NOT NULL,
    module_type VARCHAR(50) NOT NULL,
    required_credits INTEGER NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 学业成绩单表
CREATE TABLE IF NOT EXISTS academic_transcript (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    term VARCHAR(20) NOT NULL,
    gpa DOUBLE PRECISION NOT NULL,
    total_credits DOUBLE PRECISION NOT NULL,
    total_courses INTEGER NOT NULL,
    passed_courses INTEGER NOT NULL,
    parse_status VARCHAR(50) DEFAULT 'PARSED',
    parsed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 学业成绩单明细表
CREATE TABLE IF NOT EXISTS academic_transcript_item (
    id BIGSERIAL PRIMARY KEY,
    transcript_id BIGINT NOT NULL,
    term VARCHAR(20) NOT NULL,
    course_code VARCHAR(50) NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    credits DOUBLE PRECISION NOT NULL,
    grade_text VARCHAR(20),
    grade_point DOUBLE PRECISION,
    passed BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 添加字段到 knowledge_document 表
ALTER TABLE knowledge_document ADD COLUMN IF NOT EXISTS tags VARCHAR(500);
ALTER TABLE knowledge_document ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE knowledge_document ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- 添加字段到 student_portrait 表
ALTER TABLE student_portrait ADD COLUMN IF NOT EXISTS leadership_roles VARCHAR(500);

-- 添加字段到 notice_delivery_target 表 (Kingbase环境)
-- ALTER TABLE notice_delivery_target ADD COLUMN IF NOT EXISTS is_read BOOLEAN NOT NULL DEFAULT FALSE;
-- ALTER TABLE notice_delivery_target ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;

-- 党团流程模板表
CREATE TABLE IF NOT EXISTS party_flow_template (
    id BIGSERIAL PRIMARY KEY,
    flow_code VARCHAR(100) NOT NULL UNIQUE,
    flow_name VARCHAR(200) NOT NULL,
    flow_type VARCHAR(50) NOT NULL,
    total_stages INTEGER NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 党团流程阶段表
CREATE TABLE IF NOT EXISTS party_flow_stage (
    id BIGSERIAL PRIMARY KEY,
    flow_id BIGINT NOT NULL,
    seq_no INTEGER NOT NULL,
    stage_code VARCHAR(100) NOT NULL,
    stage_name VARCHAR(200) NOT NULL,
    description TEXT,
    required_materials TEXT,
    estimated_days INTEGER,
    reminder_days_before INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 党团题库表
CREATE TABLE IF NOT EXISTS party_question_bank (
    id BIGSERIAL PRIMARY KEY,
    bank_code VARCHAR(100) NOT NULL UNIQUE,
    bank_name VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    question_count INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 党团题目表
CREATE TABLE IF NOT EXISTS party_question (
    id BIGSERIAL PRIMARY KEY,
    bank_id BIGINT NOT NULL,
    seq_no INTEGER NOT NULL,
    question_text TEXT NOT NULL,
    options TEXT NOT NULL,
    correct_answer VARCHAR(10) NOT NULL,
    explanation TEXT,
    score INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 党团自测记录表
CREATE TABLE IF NOT EXISTS party_quiz_record (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    bank_id BIGINT NOT NULL,
    total_questions INTEGER NOT NULL,
    correct_count INTEGER NOT NULL,
    score INTEGER NOT NULL,
    total_score INTEGER NOT NULL,
    passed BOOLEAN NOT NULL,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
