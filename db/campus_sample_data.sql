-- Sample data for campus schema
-- Assumptions:
-- 1) Tables in kingbase_schema.sql have already been created.
-- 2) campus.sys_role has been initialized by the schema script.
-- 3) This script is intended for an empty or freshly prepared demo database.

-- 1. Users and roles
insert into campus.sys_user (id, student_no, full_name, status, ext_json, created_at, updated_at, is_deleted) values
  (1001, '2022110001', '张晨皓', 'active', '{"campus":"主校区","enrollmentSource":"统招"}', timestamp with time zone '2022-09-01 08:30:00+08:00', timestamp with time zone '2026-03-01 09:00:00+08:00', 0),
  (1002, '2022110002', '李雨桐', 'active', '{"campus":"主校区","enrollmentSource":"统招"}', timestamp with time zone '2022-09-01 08:35:00+08:00', timestamp with time zone '2026-03-01 09:05:00+08:00', 0),
  (1003, '2022110003', '王子安', 'active', '{"campus":"主校区","classDuty":"学习委员"}', timestamp with time zone '2022-09-01 08:40:00+08:00', timestamp with time zone '2026-03-01 09:10:00+08:00', 0),
  (1004, '2023110201', '陈思琪', 'active', '{"campus":"南校区","enrollmentSource":"统招"}', timestamp with time zone '2023-09-01 08:20:00+08:00', timestamp with time zone '2026-03-01 09:15:00+08:00', 0),
  (9001, 'T2020001', '赵明老师', 'active', '{"department":"学生工作办公室"}', timestamp with time zone '2020-07-01 09:00:00+08:00', timestamp with time zone '2026-03-01 09:20:00+08:00', 0),
  (9002, 'T2020002', '刘海老师', 'active', '{"department":"教学科研办公室"}', timestamp with time zone '2020-07-10 09:00:00+08:00', timestamp with time zone '2026-03-01 09:25:00+08:00', 0),
  (9003, 'L2020001', '马文静', 'active', '{"department":"学院领导班子","title":"副书记"}', timestamp with time zone '2020-06-15 09:00:00+08:00', timestamp with time zone '2026-03-01 09:30:00+08:00', 0);

insert into campus.sys_user_role (id, user_id, role_id, created_at)
select 1101, 1001, id, timestamp with time zone '2022-09-01 09:00:00+08:00' from campus.sys_role where role_code = 'student';
insert into campus.sys_user_role (id, user_id, role_id, created_at)
select 1102, 1002, id, timestamp with time zone '2022-09-01 09:00:00+08:00' from campus.sys_role where role_code = 'student';
insert into campus.sys_user_role (id, user_id, role_id, created_at)
select 1103, 1003, id, timestamp with time zone '2022-09-01 09:00:00+08:00' from campus.sys_role where role_code = 'student';
insert into campus.sys_user_role (id, user_id, role_id, created_at)
select 1104, 1003, id, timestamp with time zone '2023-09-01 10:00:00+08:00' from campus.sys_role where role_code = 'cadre';
insert into campus.sys_user_role (id, user_id, role_id, created_at)
select 1105, 1004, id, timestamp with time zone '2023-09-01 09:00:00+08:00' from campus.sys_role where role_code = 'student';
insert into campus.sys_user_role (id, user_id, role_id, created_at)
select 1106, 9001, id, timestamp with time zone '2020-07-01 10:00:00+08:00' from campus.sys_role where role_code = 'teacher_admin';
insert into campus.sys_user_role (id, user_id, role_id, created_at)
select 1107, 9002, id, timestamp with time zone '2020-07-10 10:00:00+08:00' from campus.sys_role where role_code = 'teacher_admin';
insert into campus.sys_user_role (id, user_id, role_id, created_at)
select 1108, 9003, id, timestamp with time zone '2020-06-15 10:00:00+08:00' from campus.sys_role where role_code = 'college_leader';

insert into campus.sys_user_auth (id, student_no, login_method, wechat_openid, password_hash, created_at, updated_at, is_deleted) values
  (1201, '2022110001', 'wechat', 'wx_openid_zhangchenhao', null, timestamp with time zone '2022-09-01 09:10:00+08:00', timestamp with time zone '2026-03-01 10:00:00+08:00', 0),
  (1202, '2022110002', 'wechat', 'wx_openid_liyutong', null, timestamp with time zone '2022-09-01 09:12:00+08:00', timestamp with time zone '2026-03-01 10:00:00+08:00', 0),
  (1203, '2022110003', 'wechat', 'wx_openid_wangzian', null, timestamp with time zone '2022-09-01 09:14:00+08:00', timestamp with time zone '2026-03-01 10:00:00+08:00', 0),
  (1204, '2023110201', 'wechat', 'wx_openid_chensiqi', null, timestamp with time zone '2023-09-01 09:10:00+08:00', timestamp with time zone '2026-03-01 10:00:00+08:00', 0),
  (1205, 'T2020001', 'password', null, '$2b$12$teacher.admin.hash.001', timestamp with time zone '2020-07-01 10:30:00+08:00', timestamp with time zone '2026-03-01 10:00:00+08:00', 0),
  (1206, 'T2020002', 'password', null, '$2b$12$teacher.admin.hash.002', timestamp with time zone '2020-07-10 10:30:00+08:00', timestamp with time zone '2026-03-01 10:00:00+08:00', 0),
  (1207, 'L2020001', 'password', null, '$2b$12$leader.admin.hash.001', timestamp with time zone '2020-06-15 10:30:00+08:00', timestamp with time zone '2026-03-01 10:00:00+08:00', 0);

insert into campus.sys_student_ext (student_no, major_name, grade_year, class_name, political_status, party_status, id_card_cipher, id_card_hash, home_address_cipher, home_address_hash, phone_cipher, phone_hash, gpa, ext_json, created_at, updated_at, is_deleted) values
  ('2022110001', '软件工程', 2022, '软件工程2201班', '共青团员', '入党积极分子', null, null, null, null, null, null, 3.62, '{"dormitory":"12舍A-306","advisor":"赵明老师"}', timestamp with time zone '2022-09-05 10:00:00+08:00', timestamp with time zone '2026-03-01 10:10:00+08:00', 0),
  ('2022110002', '软件工程', 2022, '软件工程2201班', '共青团员', '群众', null, null, null, null, null, null, 3.48, '{"dormitory":"12舍A-308","advisor":"赵明老师"}', timestamp with time zone '2022-09-05 10:05:00+08:00', timestamp with time zone '2026-03-01 10:10:00+08:00', 0),
  ('2022110003', '软件工程', 2022, '软件工程2201班', '共青团员', '发展对象', null, null, null, null, null, null, 3.71, '{"dormitory":"12舍B-201","advisor":"赵明老师"}', timestamp with time zone '2022-09-05 10:10:00+08:00', timestamp with time zone '2026-03-01 10:10:00+08:00', 0),
  ('2023110201', '计算机科学与技术', 2023, '计科2302班', '共青团员', '群众', null, null, null, null, null, null, 3.33, '{"dormitory":"8舍C-509","advisor":"刘海老师"}', timestamp with time zone '2023-09-05 10:15:00+08:00', timestamp with time zone '2026-03-01 10:10:00+08:00', 0);

-- 2. Files
insert into campus.file_object (id, purpose, original_name, mime_type, size_bytes, sha256, storage_provider, storage_path, uploaded_by, uploaded_at, created_at, updated_at, is_deleted) values
  (2001, 'kb_policy', '2026年党员发展工作指引.pdf', 'application/pdf', 823456, 'sha256_policy_2001', 'minio', '/campus/kb/policy/2026-party-guide.pdf', 9001, timestamp with time zone '2026-02-18 15:00:00+08:00', timestamp with time zone '2026-02-18 15:00:00+08:00', timestamp with time zone '2026-02-18 15:00:00+08:00', 0),
  (2002, 'kb_policy', '2025-2026学年国家奖学金评审办法.pdf', 'application/pdf', 1045120, 'sha256_policy_2002', 'minio', '/campus/kb/policy/2025-scholarship-rule.pdf', 9002, timestamp with time zone '2026-02-20 09:30:00+08:00', timestamp with time zone '2026-02-20 09:30:00+08:00', timestamp with time zone '2026-02-20 09:30:00+08:00', 0),
  (2003, 'notice_attachment', '2026春季第二轮选课通知附件.pdf', 'application/pdf', 652314, 'sha256_notice_2003', 'minio', '/campus/notice/2026-spring-course-selection.pdf', 9002, timestamp with time zone '2026-02-24 11:00:00+08:00', timestamp with time zone '2026-02-24 11:00:00+08:00', timestamp with time zone '2026-02-24 11:00:00+08:00', 0),
  (2004, 'cert_template', '在读证明模板.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 186245, 'sha256_cert_tpl_2004', 'minio', '/campus/cert/template/study-cert-template.docx', 9001, timestamp with time zone '2026-01-12 14:20:00+08:00', timestamp with time zone '2026-01-12 14:20:00+08:00', timestamp with time zone '2026-01-12 14:20:00+08:00', 0),
  (2005, 'transcript_source', '张晨皓_成绩单_2025秋.pdf', 'application/pdf', 332145, 'sha256_transcript_2005', 'minio', '/campus/aca/transcript/2022110001-2025-fall.pdf', 1001, timestamp with time zone '2026-02-26 20:10:00+08:00', timestamp with time zone '2026-02-26 20:10:00+08:00', timestamp with time zone '2026-02-26 20:10:00+08:00', 0),
  (2006, 'affair_attachment', '贫困生认定补充材料.zip', 'application/zip', 845123, 'sha256_affair_2006', 'minio', '/campus/affair/request/6202-material.zip', 1002, timestamp with time zone '2026-03-01 16:40:00+08:00', timestamp with time zone '2026-03-01 16:40:00+08:00', timestamp with time zone '2026-03-01 16:40:00+08:00', 0),
  (2007, 'cert_output', '在读证明_张晨皓.pdf', 'application/pdf', 125478, 'sha256_cert_out_2007', 'minio', '/campus/cert/output/study-cert-2022110001.pdf', 9001, timestamp with time zone '2026-03-02 10:30:00+08:00', timestamp with time zone '2026-03-02 10:30:00+08:00', timestamp with time zone '2026-03-02 10:30:00+08:00', 0),
  (2008, 'aca_program', '软件工程2022版培养方案.pdf', 'application/pdf', 556301, 'sha256_program_2008', 'minio', '/campus/aca/program/se-2022.pdf', 9002, timestamp with time zone '2025-09-01 09:00:00+08:00', timestamp with time zone '2025-09-01 09:00:00+08:00', timestamp with time zone '2025-09-01 09:00:00+08:00', 0);

-- 3. Knowledge base
insert into campus.kb_policy (id, title, summary, content, source_type, source_url, attachment_file_id, is_published, published_at, created_by, ext_json, created_at, updated_at, is_deleted) values
  (3001, '2026年党员发展工作指引', '覆盖入党申请、积极分子培养、发展对象考察等环节。', '本指引用于规范学院 2026 年党员发展工作流程，明确材料提交时间、谈话安排和支部公示要求。', 'manual', 'https://campus.example.edu/policy/party-guide-2026', 2001, 1, timestamp with time zone '2026-02-18 16:00:00+08:00', 9001, '{"department":"学生工作办公室","tags":["入党","流程"]}', timestamp with time zone '2026-02-18 15:30:00+08:00', timestamp with time zone '2026-02-18 16:00:00+08:00', 0),
  (3002, '2025-2026学年国家奖学金评审办法', '说明申请条件、加分规则和评审流程。', '适用于 2025-2026 学年国家奖学金评审，重点说明综合测评、学业成绩、竞赛加分和公示流程。', 'manual', 'https://campus.example.edu/policy/scholarship-2025', 2002, 1, timestamp with time zone '2026-02-20 10:00:00+08:00', 9002, '{"department":"教学科研办公室","tags":["奖学金","评优"]}', timestamp with time zone '2026-02-20 09:40:00+08:00', timestamp with time zone '2026-02-20 10:00:00+08:00', 0),
  (3003, '软件工程专业课程替代认定说明', '用于指导课程替代与培养方案审核。', '适用于软件工程专业课程替代、重修置换以及跨专业选修认定，需结合培养方案模块进行审核。', 'manual', 'https://campus.example.edu/policy/course-replacement', null, 1, timestamp with time zone '2026-02-22 09:30:00+08:00', 9002, '{"department":"教学科研办公室","tags":["课程替代","培养方案"]}', timestamp with time zone '2026-02-22 09:00:00+08:00', timestamp with time zone '2026-02-22 09:30:00+08:00', 0);

insert into campus.kb_faq (id, question, answer, source_policy_id, is_published, created_by, ext_json, created_at, updated_at, is_deleted) values
  (3101, '入党申请书提交后多久能收到支部反馈？', '原则上在支部完成材料初审后的 5 个工作日内反馈；如遇寒暑假，反馈时间顺延。', 3001, 1, 9001, '{"channel":"manual"}', timestamp with time zone '2026-02-19 09:00:00+08:00', timestamp with time zone '2026-02-19 09:00:00+08:00', 0),
  (3102, '国家奖学金申请时，综测成绩和绩点哪个权重更高？', '以学校当学年评审细则为准，通常学业成绩为基础门槛，综测成绩用于同等条件排序。', 3002, 1, 9002, '{"channel":"manual"}', timestamp with time zone '2026-02-20 11:00:00+08:00', timestamp with time zone '2026-02-20 11:00:00+08:00', 0),
  (3103, '学业审核里推荐课程为什么会出现同一课程编码的不同老师？', '推荐结果会细化到实际开课班次，因此同一课程编码在同一学期由不同老师开设时会分别展示。', 3003, 1, 9002, '{"channel":"manual"}', timestamp with time zone '2026-02-22 10:00:00+08:00', timestamp with time zone '2026-02-22 10:00:00+08:00', 0);

insert into campus.kb_keyword (id, keyword, created_at) values
  (3201, '入党', timestamp with time zone '2026-02-18 15:40:00+08:00'),
  (3202, '奖学金', timestamp with time zone '2026-02-20 09:45:00+08:00'),
  (3203, '评优', timestamp with time zone '2026-02-20 09:46:00+08:00'),
  (3204, '选课', timestamp with time zone '2026-02-24 10:00:00+08:00'),
  (3205, '补考', timestamp with time zone '2026-02-24 10:05:00+08:00'),
  (3206, '在读证明', timestamp with time zone '2026-01-12 14:30:00+08:00'),
  (3207, '培养方案', timestamp with time zone '2026-02-22 09:10:00+08:00'),
  (3208, '课程替代', timestamp with time zone '2026-02-22 09:11:00+08:00'),
  (3209, '学业审核', timestamp with time zone '2026-02-22 09:12:00+08:00'),
  (3210, '证明模板', timestamp with time zone '2026-01-12 14:31:00+08:00');

insert into campus.kb_policy_keyword (id, policy_id, keyword_id, created_at) values
  (3301, 3001, 3201, timestamp with time zone '2026-02-18 16:05:00+08:00'),
  (3302, 3002, 3202, timestamp with time zone '2026-02-20 10:05:00+08:00'),
  (3303, 3002, 3203, timestamp with time zone '2026-02-20 10:05:30+08:00'),
  (3304, 3003, 3207, timestamp with time zone '2026-02-22 09:35:00+08:00'),
  (3305, 3003, 3208, timestamp with time zone '2026-02-22 09:35:30+08:00'),
  (3306, 3003, 3209, timestamp with time zone '2026-02-22 09:36:00+08:00');

insert into campus.kb_faq_keyword (id, faq_id, keyword_id, created_at) values
  (3311, 3101, 3201, timestamp with time zone '2026-02-19 09:05:00+08:00'),
  (3312, 3102, 3202, timestamp with time zone '2026-02-20 11:05:00+08:00'),
  (3313, 3102, 3203, timestamp with time zone '2026-02-20 11:05:30+08:00'),
  (3314, 3103, 3207, timestamp with time zone '2026-02-22 10:05:00+08:00'),
  (3315, 3103, 3209, timestamp with time zone '2026-02-22 10:05:30+08:00');

insert into campus.kb_keyword_search_history (id, search_user_id, keyword_id, search_keyword, search_scope, search_channel, result_count, session_token, client_ip, ext_json, created_at) values
  (3401, 1001, 3204, '选课', 'notice', 'miniprogram', 2, 'sess_20260301_1001', '10.10.1.21', '{"matchedNoticeIds":[5101]}', timestamp with time zone '2026-03-01 19:42:10+08:00'),
  (3402, 1001, 3206, '在读证明', 'template', 'miniprogram', 1, 'sess_20260301_1001', '10.10.1.21', '{"matchedTemplateIds":[6301]}', timestamp with time zone '2026-03-01 19:44:06+08:00'),
  (3403, 1002, 3202, '奖学金', 'policy', 'miniprogram', 1, 'sess_20260302_1002', '10.10.1.22', '{"matchedPolicyIds":[3002]}', timestamp with time zone '2026-03-02 08:12:33+08:00'),
  (3404, 1003, 3209, '学业审核', 'faq', 'admin', 1, 'sess_20260302_1003', '10.10.1.23', '{"matchedFaqIds":[3103]}', timestamp with time zone '2026-03-02 09:03:27+08:00');

insert into campus.kb_qa_ticket (id, ask_user_id, question_text, status, matched_faq_id, handled_by, handled_at, created_at, updated_at) values
  (3501, 1002, '国家奖学金申请时，如果竞赛证书还没发下来，可以先提交电子版吗？', 'answered', 3102, 9002, timestamp with time zone '2026-03-02 11:10:00+08:00', timestamp with time zone '2026-03-02 10:30:00+08:00', timestamp with time zone '2026-03-02 11:10:00+08:00'),
  (3502, 1004, '学业审核推荐课程里两个老师的数据库原理，我该怎么选？', 'open', 3103, null, null, timestamp with time zone '2026-03-03 09:20:00+08:00', timestamp with time zone '2026-03-03 09:20:00+08:00');

-- 4. Party flow
insert into campus.party_flow (id, flow_code, flow_name, flow_type, is_active, created_by, created_at, updated_at, is_deleted) values
  (4001, 'party_dev_standard', '党员发展标准流程', 'party', 1, 9001, timestamp with time zone '2026-02-10 09:00:00+08:00', timestamp with time zone '2026-02-10 09:00:00+08:00', 0);

insert into campus.party_flow_node (id, flow_id, seq_no, node_code, node_name, description, expected_days, reminder_offset_days, created_at, updated_at, is_deleted) values
  (4011, 4001, 1, 'apply_submit', '提交入党申请书', '学生提交纸质与电子版申请材料。', 7, 2, timestamp with time zone '2026-02-10 09:10:00+08:00', timestamp with time zone '2026-02-10 09:10:00+08:00', 0),
  (4012, 4001, 2, 'activist_train', '积极分子培养考察', '完成支部谈话、培养联系人安排和阶段考察。', 120, 7, timestamp with time zone '2026-02-10 09:15:00+08:00', timestamp with time zone '2026-02-10 09:15:00+08:00', 0),
  (4013, 4001, 3, 'public_notice', '发展对象公示', '完成资格审查并进入公示环节。', 10, 2, timestamp with time zone '2026-02-10 09:20:00+08:00', timestamp with time zone '2026-02-10 09:20:00+08:00', 0);

insert into campus.party_student_progress (id, student_user_id, flow_id, current_node_id, status, started_at, updated_node_at, next_deadline_at, created_at, updated_at, is_deleted) values
  (4101, 1001, 4001, 4012, 'in_progress', timestamp with time zone '2025-10-20 18:00:00+08:00', timestamp with time zone '2026-02-28 17:30:00+08:00', timestamp with time zone '2026-04-15 23:59:59+08:00', timestamp with time zone '2025-10-20 18:00:00+08:00', timestamp with time zone '2026-02-28 17:30:00+08:00', 0);

insert into campus.party_reminder_task (id, progress_id, node_id, due_at, channel, status, sent_at, created_at, updated_at) values
  (4201, 4101, 4012, timestamp with time zone '2026-04-08 09:00:00+08:00', 'miniprogram', 'sent', timestamp with time zone '2026-04-08 09:00:05+08:00', timestamp with time zone '2026-04-07 18:00:00+08:00', timestamp with time zone '2026-04-08 09:00:05+08:00');

-- 5. Notice module
insert into campus.notice_tag_dict (id, tag_code, tag_name, created_at, updated_at, is_deleted) values
  (5001, 'academic', '教学通知', timestamp with time zone '2026-02-24 09:00:00+08:00', timestamp with time zone '2026-02-24 09:00:00+08:00', 0),
  (5002, 'scholarship', '奖助评优', timestamp with time zone '2026-02-24 09:01:00+08:00', timestamp with time zone '2026-02-24 09:01:00+08:00', 0),
  (5003, 'party', '党团事务', timestamp with time zone '2026-02-24 09:02:00+08:00', timestamp with time zone '2026-02-24 09:02:00+08:00', 0);

insert into campus.notice_item (id, title, content, source_type, source_name, source_url, attachment_file_id, publish_at, created_by, ext_json, created_at, updated_at, is_deleted) values
  (5101, '2025-2026学年第二学期第二轮选课通知', '第二轮选课时间为 3 月 4 日 08:00 至 3 月 6 日 22:00，请同学们结合培养方案和已修课程谨慎选课。', 'manual', '教务处', 'https://campus.example.edu/notice/course-selection-2026-spring', 2003, timestamp with time zone '2026-03-03 08:00:00+08:00', 9002, '{"forGrades":[2022,2023],"priority":"high"}', timestamp with time zone '2026-03-02 16:20:00+08:00', timestamp with time zone '2026-03-02 16:20:00+08:00', 0),
  (5102, '2025-2026学年国家奖学金材料补充提醒', '请已进入学院推荐名单的同学于 3 月 8 日前提交电子版佐证材料。', 'manual', '学生工作办公室', 'https://campus.example.edu/notice/scholarship-material', null, timestamp with time zone '2026-03-02 09:00:00+08:00', 9001, '{"forMajors":["软件工程","计算机科学与技术"],"priority":"medium"}', timestamp with time zone '2026-03-01 18:00:00+08:00', timestamp with time zone '2026-03-01 18:00:00+08:00', 0);

insert into campus.notice_item_tag (id, notice_id, tag_id, created_at) values
  (5111, 5101, 5001, timestamp with time zone '2026-03-02 16:21:00+08:00'),
  (5112, 5102, 5002, timestamp with time zone '2026-03-01 18:01:00+08:00');

insert into campus.notice_item_keyword (id, notice_id, keyword_id, created_at) values
  (5121, 5101, 3204, timestamp with time zone '2026-03-02 16:22:00+08:00'),
  (5122, 5101, 3207, timestamp with time zone '2026-03-02 16:22:10+08:00'),
  (5123, 5102, 3202, timestamp with time zone '2026-03-01 18:02:00+08:00'),
  (5124, 5102, 3203, timestamp with time zone '2026-03-01 18:02:10+08:00');

insert into campus.notice_delivery (id, notice_id, channel, target_rule_json, status, scheduled_at, sent_at, created_by, ext_json, created_at, updated_at) values
  (5201, 5101, 'miniprogram', '{"gradeYears":[2022,2023],"majors":["软件工程","计算机科学与技术"]}', 'done', timestamp with time zone '2026-03-03 08:00:00+08:00', timestamp with time zone '2026-03-03 08:00:03+08:00', 9002, '{"batch":"spring-course-selection-round2"}', timestamp with time zone '2026-03-02 17:00:00+08:00', timestamp with time zone '2026-03-03 08:00:03+08:00'),
  (5202, 5102, 'miniprogram', '{"scholarshipCandidates":true}', 'done', timestamp with time zone '2026-03-02 09:00:00+08:00', timestamp with time zone '2026-03-02 09:00:01+08:00', 9001, '{"batch":"scholarship-material-reminder"}', timestamp with time zone '2026-03-01 18:05:00+08:00', timestamp with time zone '2026-03-02 09:00:01+08:00');

insert into campus.notice_delivery_target (id, delivery_id, target_user_id, status, sent_at, error_message, created_at, updated_at) values
  (5301, 5201, 1001, 'sent', timestamp with time zone '2026-03-03 08:00:05+08:00', null, timestamp with time zone '2026-03-02 17:05:00+08:00', timestamp with time zone '2026-03-03 08:00:05+08:00'),
  (5302, 5201, 1002, 'sent', timestamp with time zone '2026-03-03 08:00:05+08:00', null, timestamp with time zone '2026-03-02 17:05:10+08:00', timestamp with time zone '2026-03-03 08:00:05+08:00'),
  (5303, 5201, 1004, 'sent', timestamp with time zone '2026-03-03 08:00:06+08:00', null, timestamp with time zone '2026-03-02 17:05:20+08:00', timestamp with time zone '2026-03-03 08:00:06+08:00'),
  (5304, 5202, 1002, 'sent', timestamp with time zone '2026-03-02 09:00:04+08:00', null, timestamp with time zone '2026-03-01 18:06:00+08:00', timestamp with time zone '2026-03-02 09:00:04+08:00');

-- 6. Workflow and affairs
insert into campus.wf_definition (id, wf_code, wf_name, business_type, is_active, created_by, created_at, updated_at, is_deleted) values
  (6001, 'affair_certificate_standard', '办事申请-证明开具流程', 'affair_request', 1, 9001, timestamp with time zone '2026-01-10 10:00:00+08:00', timestamp with time zone '2026-01-10 10:00:00+08:00', 0);

insert into campus.wf_node (id, wf_id, seq_no, node_name, approver_role_id, approver_user_id, sla_hours, allow_reject, created_at, updated_at, is_deleted)
select 6011, 6001, 1, '辅导员初审', null, 9001, 24, 1, timestamp with time zone '2026-01-10 10:05:00+08:00', timestamp with time zone '2026-01-10 10:05:00+08:00', 0
from campus.sys_role where role_code = 'teacher_admin'
union all
select 6012, 6001, 2, '学院终审', null, 9003, 24, 1, timestamp with time zone '2026-01-10 10:06:00+08:00', timestamp with time zone '2026-01-10 10:06:00+08:00', 0
from campus.sys_role where role_code = 'college_leader';

insert into campus.affair_request (id, requester_user_id, request_type, title, purpose, remark, payload_json, status, submitted_at, closed_at, created_at, updated_at, is_deleted) values
  (6201, 1001, 'certificate', '在读证明申请', '办理暑期实习单位入职手续', '需要电子版和纸质盖章版各一份。', '{"useCase":"internship","receiveMode":"pdf+paper","company":"星海软件有限公司"}', 'approved', timestamp with time zone '2026-03-01 09:10:00+08:00', timestamp with time zone '2026-03-02 10:35:00+08:00', timestamp with time zone '2026-03-01 09:08:00+08:00', timestamp with time zone '2026-03-02 10:35:00+08:00', 0),
  (6202, 1002, 'other', '困难认定补充材料提交', '用于学院困难认定复核', '已补交家庭情况说明与收入证明。', '{"category":"financialAidReview","attachmentsExpected":2}', 'submitted', timestamp with time zone '2026-03-01 16:42:00+08:00', null, timestamp with time zone '2026-03-01 16:40:00+08:00', timestamp with time zone '2026-03-01 16:42:00+08:00', 0);

insert into campus.affair_request_attachment (id, request_id, file_id, created_at) values
  (6211, 6202, 2006, timestamp with time zone '2026-03-01 16:42:30+08:00');

insert into campus.wf_instance (id, wf_id, business_table, business_id, status, started_by, started_at, finished_at, created_at, updated_at) values
  (6101, 6001, 'campus.affair_request', 6201, 'approved', 1001, timestamp with time zone '2026-03-01 09:10:00+08:00', timestamp with time zone '2026-03-02 10:20:00+08:00', timestamp with time zone '2026-03-01 09:10:00+08:00', timestamp with time zone '2026-03-02 10:20:00+08:00');

insert into campus.wf_task (id, wf_instance_id, wf_node_id, assignee_user_id, status, due_at, completed_at, created_at, updated_at) values
  (6111, 6101, 6011, 9001, 'approved', timestamp with time zone '2026-03-02 09:10:00+08:00', timestamp with time zone '2026-03-01 14:20:00+08:00', timestamp with time zone '2026-03-01 09:10:00+08:00', timestamp with time zone '2026-03-01 14:20:00+08:00'),
  (6112, 6101, 6012, 9003, 'approved', timestamp with time zone '2026-03-03 09:10:00+08:00', timestamp with time zone '2026-03-02 10:20:00+08:00', timestamp with time zone '2026-03-01 14:20:00+08:00', timestamp with time zone '2026-03-02 10:20:00+08:00');

insert into campus.wf_task_action (id, wf_task_id, actor_user_id, action, action_comment, action_at, created_at) values
  (6121, 6111, 9001, 'approve', '学生信息齐全，符合开具条件。', timestamp with time zone '2026-03-01 14:20:00+08:00', timestamp with time zone '2026-03-01 14:20:00+08:00'),
  (6122, 6112, 9003, 'approve', '同意出具在读证明。', timestamp with time zone '2026-03-02 10:20:00+08:00', timestamp with time zone '2026-03-02 10:20:00+08:00');

insert into campus.cert_template (id, template_code, template_name, file_id, output_format, is_active, created_by, ext_json, created_at, updated_at, is_deleted) values
  (6301, 'study_cert', '本科生在读证明', 2004, 'pdf', 1, 9001, '{"placeholders":["studentName","studentNo","majorName","gradeYear"],"paperSize":"A4"}', timestamp with time zone '2026-01-12 14:30:00+08:00', timestamp with time zone '2026-01-12 14:30:00+08:00', 0);

insert into campus.cert_template_keyword (id, template_id, keyword_id, created_at) values
  (6311, 6301, 3206, timestamp with time zone '2026-01-12 14:35:00+08:00'),
  (6312, 6301, 3210, timestamp with time zone '2026-01-12 14:35:10+08:00');

insert into campus.cert_application (id, request_id, template_id, generated_pdf_file_id, student_snapshot_json, created_at, updated_at) values
  (6401, 6201, 6301, 2007, '{"studentNo":"2022110001","fullName":"张晨皓","majorName":"软件工程","gradeYear":2022,"className":"软件工程2201班"}', timestamp with time zone '2026-03-02 10:25:00+08:00', timestamp with time zone '2026-03-02 10:30:00+08:00');

insert into campus.cert_generated_file (id, cert_app_id, file_id, created_at) values
  (6501, 6401, 2007, timestamp with time zone '2026-03-02 10:30:00+08:00');

-- 7. Academic analysis
insert into campus.aca_program (id, program_code, major_name, grade_year, version_name, description, is_active, created_by, ext_json, created_at, updated_at, is_deleted) values
  (7001, 'SE2022', '软件工程', 2022, '2022版', '软件工程专业 2022 级本科培养方案。', 1, 9002, '{"minimumCredits":150,"auditCycle":"semester"}', timestamp with time zone '2025-09-01 09:10:00+08:00', timestamp with time zone '2025-09-01 09:10:00+08:00', 0);

insert into campus.aca_program_module (id, program_id, module_code, module_name, module_type, required_credits, created_at, updated_at, is_deleted) values
  (7101, 7001, 'major_required', '专业必修', 'required', 60.00, timestamp with time zone '2025-09-01 09:20:00+08:00', timestamp with time zone '2025-09-01 09:20:00+08:00', 0),
  (7102, 7001, 'major_elective', '专业选修', 'elective', 18.00, timestamp with time zone '2025-09-01 09:20:30+08:00', timestamp with time zone '2025-09-01 09:20:30+08:00', 0),
  (7103, 7001, 'general', '通识教育', 'general', 12.00, timestamp with time zone '2025-09-01 09:21:00+08:00', timestamp with time zone '2025-09-01 09:21:00+08:00', 0);

insert into campus.aca_course (id, course_code, course_name, credits, course_type, created_at, updated_at, is_deleted) values
  (7201, 'SE220101', '程序设计基础', 4.00, 'required', timestamp with time zone '2025-09-01 09:30:00+08:00', timestamp with time zone '2025-09-01 09:30:00+08:00', 0),
  (7202, 'SE220305', '数据库原理', 3.50, 'required', timestamp with time zone '2025-09-01 09:31:00+08:00', timestamp with time zone '2025-09-01 09:31:00+08:00', 0),
  (7203, 'SE220401', '软件工程', 3.00, 'required', timestamp with time zone '2025-09-01 09:32:00+08:00', timestamp with time zone '2025-09-01 09:32:00+08:00', 0),
  (7204, 'GE000101', '大学英语Ⅳ', 2.00, 'general', timestamp with time zone '2025-09-01 09:33:00+08:00', timestamp with time zone '2025-09-01 09:33:00+08:00', 0),
  (7205, 'SE220512', '数据挖掘导论', 2.00, 'elective', timestamp with time zone '2025-09-01 09:34:00+08:00', timestamp with time zone '2025-09-01 09:34:00+08:00', 0),
  (7206, 'SE220333', 'Web应用开发', 2.50, 'elective', timestamp with time zone '2025-09-01 09:35:00+08:00', timestamp with time zone '2025-09-01 09:35:00+08:00', 0);

insert into campus.aca_term (id, term_code, term_name, academic_year, semester_no, start_date, end_date, is_current, created_at, updated_at, is_deleted) values
  (7301, '2025-2026-1', '2025-2026学年第一学期', '2025-2026', 1, date '2025-09-01', date '2026-01-10', 0, timestamp with time zone '2025-09-01 08:50:00+08:00', timestamp with time zone '2025-09-01 08:50:00+08:00', 0),
  (7302, '2025-2026-2', '2025-2026学年第二学期', '2025-2026', 2, date '2026-02-24', date '2026-07-05', 1, timestamp with time zone '2026-02-20 08:50:00+08:00', timestamp with time zone '2026-02-20 08:50:00+08:00', 0);

insert into campus.aca_term_course (id, term_id, course_id, teaching_class_code, course_code, course_name, teacher_name, course_location, credits, total_hours, created_at, updated_at, is_deleted) values
  (7401, 7302, 7202, '2025-2026-2-SE220305-01', 'SE220305', '数据库原理', '周文静', '教三-402', 3.50, 56.00, timestamp with time zone '2026-02-22 10:00:00+08:00', timestamp with time zone '2026-02-22 10:00:00+08:00', 0),
  (7402, 7302, 7202, '2025-2026-2-SE220305-02', 'SE220305', '数据库原理', '唐海峰', '教三-405', 3.50, 56.00, timestamp with time zone '2026-02-22 10:05:00+08:00', timestamp with time zone '2026-02-22 10:05:00+08:00', 0),
  (7403, 7302, 7203, '2025-2026-2-SE220401-01', 'SE220401', '软件工程', '刘志鹏', '教二-201', 3.00, 48.00, timestamp with time zone '2026-02-22 10:10:00+08:00', timestamp with time zone '2026-02-22 10:10:00+08:00', 0),
  (7404, 7302, 7205, '2025-2026-2-SE220512-01', 'SE220512', '数据挖掘导论', '冯媛媛', '教四-308', 2.00, 32.00, timestamp with time zone '2026-02-22 10:15:00+08:00', timestamp with time zone '2026-02-22 10:15:00+08:00', 0),
  (7405, 7302, 7206, '2025-2026-2-SE220333-01', 'SE220333', 'Web应用开发', '孙凯', '实验楼-406', 2.50, 40.00, timestamp with time zone '2026-02-22 10:20:00+08:00', timestamp with time zone '2026-02-22 10:20:00+08:00', 0),
  (7408, 7302, 7205, '2025-2026-2-SE220512-02', 'SE220512', '数据挖掘导论', '田晓莉', '教四-310', 2.00, 32.00, timestamp with time zone '2026-02-22 10:22:00+08:00', timestamp with time zone '2026-02-22 10:22:00+08:00', 0),
  (7406, 7301, 7201, '2025-2026-1-SE220101-01', 'SE220101', '程序设计基础', '黄敏', '教一-101', 4.00, 64.00, timestamp with time zone '2025-09-02 10:00:00+08:00', timestamp with time zone '2025-09-02 10:00:00+08:00', 0),
  (7407, 7301, 7204, '2025-2026-1-GE000101-03', 'GE000101', '大学英语Ⅳ', '顾晓岚', '教六-202', 2.00, 32.00, timestamp with time zone '2025-09-02 10:05:00+08:00', timestamp with time zone '2025-09-02 10:05:00+08:00', 0);

insert into campus.aca_module_course (id, module_id, course_id, created_at) values
  (7411, 7101, 7201, timestamp with time zone '2025-09-01 09:40:00+08:00'),
  (7412, 7101, 7202, timestamp with time zone '2025-09-01 09:40:10+08:00'),
  (7413, 7101, 7203, timestamp with time zone '2025-09-01 09:40:20+08:00'),
  (7414, 7102, 7205, timestamp with time zone '2025-09-01 09:40:30+08:00'),
  (7415, 7102, 7206, timestamp with time zone '2025-09-01 09:40:40+08:00'),
  (7416, 7103, 7204, timestamp with time zone '2025-09-01 09:40:50+08:00');

insert into campus.aca_transcript (id, student_user_id, source_file_id, parse_status, parsed_at, total_credits, gpa, created_at, updated_at) values
  (7501, 1001, 2005, 'parsed', timestamp with time zone '2026-02-26 20:20:00+08:00', 68.50, 3.62, timestamp with time zone '2026-02-26 20:10:00+08:00', timestamp with time zone '2026-02-26 20:20:00+08:00');

insert into campus.aca_transcript_item (id, transcript_id, term, course_code, course_name, credits, grade_text, grade_point, passed, created_at) values
  (7511, 7501, '2025-2026-1', 'SE220101', '程序设计基础', 4.00, '92', 4.20, 1, timestamp with time zone '2026-02-26 20:21:00+08:00'),
  (7512, 7501, '2025-2026-1', 'GE000101', '大学英语Ⅳ', 2.00, '85', 3.70, 1, timestamp with time zone '2026-02-26 20:21:10+08:00'),
  (7513, 7501, '2025-2026-1', 'SE220305', '数据库原理', 3.50, '88', 3.90, 1, timestamp with time zone '2026-02-26 20:21:20+08:00'),
  (7514, 7501, '2025-2026-1', 'SE220401', '软件工程', 3.00, '86', 3.80, 1, timestamp with time zone '2026-02-26 20:21:30+08:00');

insert into campus.aca_audit_report (id, student_user_id, program_id, transcript_id, status, missing_module_count, generated_at, created_at, updated_at) values
  (7601, 1001, 7001, 7501, 'generated', 1, timestamp with time zone '2026-03-03 14:10:00+08:00', timestamp with time zone '2026-03-03 14:10:00+08:00', timestamp with time zone '2026-03-03 14:10:00+08:00');

insert into campus.aca_audit_missing (id, report_id, module_id, missing_credits, created_at) values
  (7701, 7601, 7102, 2.00, timestamp with time zone '2026-03-03 14:11:00+08:00');

insert into campus.aca_course_recommendation (id, report_id, module_id, course_id, recommendation_reason, created_at) values
  (7801, 7601, 7102, 7404, '与专业选修模块匹配，且当前学期仍可补退选。', timestamp with time zone '2026-03-03 14:12:00+08:00'),
  (7802, 7601, 7102, 7405, '课程实践性较强，适合作为专业选修补足缺失学分。', timestamp with time zone '2026-03-03 14:12:10+08:00'),
  (7803, 7601, 7102, 7408, '同一课程编码存在不同任课老师时，推荐结果会细化到实际开课班次。', timestamp with time zone '2026-03-03 14:12:20+08:00');

insert into campus.audit_import_job (id, job_type, source_file_id, status, total_rows, success_rows, failed_rows, error_message, result_json, started_by, started_at, finished_at, created_at, updated_at) values
  (7901, 'transcript_import', 2005, 'done', 4, 4, 0, null, '{"reportId":7601,"transcriptId":7501,"warnings":[]}', 9002, timestamp with time zone '2026-02-26 20:15:00+08:00', timestamp with time zone '2026-02-26 20:20:00+08:00', timestamp with time zone '2026-02-26 20:15:00+08:00', timestamp with time zone '2026-02-26 20:20:00+08:00');

-- 8. Operation logs
insert into campus.sys_operation_log (id, module_code, business_type, business_id, operation_type, operation_desc, operator_user_id, trace_id, request_uri, request_method, request_ip, user_agent, log_level, result_status, error_message, ext_json, created_at) values
  (8001, 'notice', 'notice_item', 5101, 'publish_notice', '发布春季学期第二轮选课通知', 9002, 'trace_notice_5101', '/api/admin/notice/5101/publish', 'POST', '10.20.0.15', 'Mozilla/5.0 AdminPortal', 'audit', 'success', null, '{"deliveryId":5201}', timestamp with time zone '2026-03-02 17:00:05+08:00'),
  (8002, 'certificate', 'cert_application', 6401, 'generate_certificate', '生成张晨皓在读证明 PDF', 9001, 'trace_cert_6401', '/api/admin/certificates/6401/generate', 'POST', '10.20.0.16', 'Mozilla/5.0 AdminPortal', 'audit', 'success', null, '{"outputFileId":2007}', timestamp with time zone '2026-03-02 10:30:05+08:00'),
  (8003, 'academic', 'aca_audit_report', 7601, 'generate_audit_report', '生成张晨皓学业审核报告并给出课程推荐', 9002, 'trace_aca_7601', '/api/admin/academic/audit/7601/generate', 'POST', '10.20.0.18', 'Mozilla/5.0 AdminPortal', 'info', 'success', null, '{"recommendationCount":3}', timestamp with time zone '2026-03-03 14:12:30+08:00');
