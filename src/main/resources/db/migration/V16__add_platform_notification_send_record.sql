CREATE TABLE IF NOT EXISTS platform_notification_send_record (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    title VARCHAR(200) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_description VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    recipient_count INTEGER NOT NULL,
    triggered_by VARCHAR(64) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    extension_channels VARCHAR(255)
);

INSERT INTO platform_notification_send_record (
    title,
    channel,
    target_type,
    target_description,
    status,
    recipient_count,
    triggered_by,
    sent_at,
    extension_channels
)
SELECT
    notice.title,
    'IN_APP',
    CASE
        WHEN notice.target_grade IS NOT NULL AND notice.target_major IS NOT NULL THEN 'GRADE_MAJOR'
        WHEN notice.target_grade IS NOT NULL THEN 'GRADE'
        WHEN notice.target_major IS NOT NULL THEN 'MAJOR'
        WHEN notice.target_graduate_only = TRUE THEN 'GRADUATE'
        ELSE 'ALL'
    END,
    CASE
        WHEN notice.target_grade IS NOT NULL AND notice.target_major IS NOT NULL THEN notice.target_grade || '/' || notice.target_major
        WHEN notice.target_grade IS NOT NULL THEN notice.target_grade
        WHEN notice.target_major IS NOT NULL THEN notice.target_major
        WHEN notice.target_graduate_only = TRUE THEN '毕业生'
        ELSE '全体学生'
    END,
    'SENT',
    CASE
        WHEN notice.target_grade IS NOT NULL AND notice.target_major IS NOT NULL THEN 120
        WHEN notice.target_grade IS NOT NULL THEN 300
        WHEN notice.target_major IS NOT NULL THEN 200
        WHEN notice.target_graduate_only = TRUE THEN 80
        ELSE 500
    END,
    'system',
    notice.publish_time,
    'EMAIL,WECHAT'
FROM notice
WHERE NOT EXISTS (
    SELECT 1 FROM platform_notification_send_record existing
    WHERE existing.title = notice.title AND existing.sent_at = notice.publish_time
);
