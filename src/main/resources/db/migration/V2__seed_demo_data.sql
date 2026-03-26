INSERT INTO user_account (username, password_hash, role, enabled, wechat_open_id)
VALUES
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE, NULL),
    ('2023100001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT', TRUE, 'mock-open-id-10001')
ON CONFLICT (username) DO NOTHING;

INSERT INTO student_profile (id, student_no, name, major, grade, class_name, degree_level, encrypted_id_card_no, encrypted_phone, email, graduated)
VALUES
    (10001, '2023100001', '张三', '计算机类', '2023级', '计科一班', '本科', NULL, NULL, 'zhangsan@example.edu', FALSE)
ON CONFLICT (student_no) DO NOTHING;

INSERT INTO knowledge_document (title, category, content, official_url, published)
VALUES
    ('奖学金申请办法', '奖助学金', '优先查看学院标准申请通知，并按要求提交材料。', 'https://example.edu/scholarship', TRUE),
    ('休学复学流程', '学籍管理', '休学与复学需提交学院审核材料，敏感信息请以官方文件为准。', 'https://example.edu/status', TRUE),
    ('宿舍调整说明', '后勤服务', '宿舍调整按学校后勤通知办理。', 'https://example.edu/dorm', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO notice (title, summary, tag, target_grade, target_major, target_graduate_only, publish_time)
VALUES
    ('实习双选会报名开启', '面向2023级和2024级计算机类学生开放报名。', '就业,实习,计算机类', '2023级', '计算机类', FALSE, CURRENT_TIMESTAMP),
    ('清明节放假安排', '请关注学院节前安全提醒。', '假期,安全', NULL, NULL, FALSE, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

INSERT INTO certificate_request (student_id, certificate_type, status, reason, generated_pdf_path)
VALUES
    (10001, '在读证明', 'PENDING', '奖学金申请材料需要', NULL),
    (10001, '党员身份证明', 'APPROVED', '组织关系转接', '/exports/certificates/1002.pdf')
ON CONFLICT DO NOTHING;

INSERT INTO academic_warning_record (student_id, module_name, required_credits, earned_credits, recommended_courses)
VALUES
    (10001, '专业核心课', 18, 12, '数据结构、操作系统'),
    (10001, '通识选修', 8, 4, '艺术鉴赏、社会研究方法')
ON CONFLICT DO NOTHING;

INSERT INTO student_status_history (student_id, from_status, to_status, changed_to_major, reason, changed_by, changed_by_role)
VALUES
    (10001, NULL, 'ACTIVE', NULL, '初始建档', '系统管理员', 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;

INSERT INTO party_progress_record (student_id, current_stage, stage_start_date, completed_actions, next_action)
VALUES
    (10001, '积极分子', CURRENT_DATE - INTERVAL '30 days', '已提交入党申请书；已参加培训', '满3个月后提交思想汇报')
ON CONFLICT DO NOTHING;
