ALTER TABLE user_account
    ADD COLUMN IF NOT EXISTS password_reset_required BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP;

UPDATE user_account
SET password_reset_required = COALESCE(password_reset_required, FALSE),
    failed_login_attempts = COALESCE(failed_login_attempts, 0);
