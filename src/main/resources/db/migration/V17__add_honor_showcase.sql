CREATE TABLE IF NOT EXISTS honor_showcase (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    award_year INTEGER NOT NULL,
    honor_category VARCHAR(100) NOT NULL,
    recipient_type VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    public_visible BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    display_start_at TIMESTAMP,
    display_end_at TIMESTAMP,
    import_task_id BIGINT,
    created_by_id BIGINT,
    created_by_name VARCHAR(64),
    updated_by VARCHAR(64),
    CONSTRAINT chk_honor_showcase_recipient_type CHECK (recipient_type IN ('PERSONAL', 'COLLECTIVE')),
    CONSTRAINT chk_honor_showcase_display_time CHECK (display_end_at IS NULL OR display_start_at IS NULL OR display_end_at >= display_start_at)
);

CREATE INDEX IF NOT EXISTS idx_honor_showcase_public_display
    ON honor_showcase (public_visible, display_order, award_year);

CREATE INDEX IF NOT EXISTS idx_honor_showcase_category
    ON honor_showcase (award_year, honor_category, recipient_type);

CREATE TABLE IF NOT EXISTS honor_recipient (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    showcase_id BIGINT NOT NULL,
    recipient_type VARCHAR(32) NOT NULL,
    student_id BIGINT,
    student_no VARCHAR(32),
    recipient_name VARCHAR(128) NOT NULL,
    major VARCHAR(64),
    grade VARCHAR(32),
    class_name VARCHAR(32),
    award_intro TEXT,
    advanced_deeds TEXT,
    photo_file_id BIGINT,
    public_visible BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER NOT NULL DEFAULT 0,
    display_start_at TIMESTAMP,
    display_end_at TIMESTAMP,
    import_task_id BIGINT,
    created_by_id BIGINT,
    created_by_name VARCHAR(64),
    updated_by VARCHAR(64),
    CONSTRAINT chk_honor_recipient_type CHECK (recipient_type IN ('PERSONAL', 'COLLECTIVE')),
    CONSTRAINT chk_honor_recipient_display_time CHECK (display_end_at IS NULL OR display_start_at IS NULL OR display_end_at >= display_start_at)
);

CREATE INDEX IF NOT EXISTS idx_honor_recipient_showcase_display
    ON honor_recipient (showcase_id, public_visible, display_order);

CREATE INDEX IF NOT EXISTS idx_honor_recipient_student
    ON honor_recipient (student_id, student_no);

CREATE TABLE IF NOT EXISTS honor_recipient_member (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recipient_id BIGINT NOT NULL,
    student_id BIGINT,
    student_no VARCHAR(32),
    student_name VARCHAR(64) NOT NULL,
    major VARCHAR(64),
    grade VARCHAR(32),
    class_name VARCHAR(32),
    member_role VARCHAR(64),
    display_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_honor_recipient_member_recipient
    ON honor_recipient_member (recipient_id, display_order);

CREATE INDEX IF NOT EXISTS idx_honor_recipient_member_student
    ON honor_recipient_member (student_id, student_no);

CREATE TABLE IF NOT EXISTS honor_recipient_attachment (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recipient_id BIGINT NOT NULL,
    file_id BIGINT,
    attachment_type VARCHAR(32) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    file_size BIGINT,
    storage_path VARCHAR(500),
    caption VARCHAR(255),
    public_visible BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_honor_attachment_type CHECK (attachment_type IN ('PHOTO', 'DOCUMENT', 'VIDEO', 'OTHER'))
);

CREATE INDEX IF NOT EXISTS idx_honor_attachment_recipient
    ON honor_recipient_attachment (recipient_id, public_visible, display_order);
