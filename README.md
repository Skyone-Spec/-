# 学院学生综合服务与党团管理平台后端

团队分工与当前仓库边界见 [TEAM_SCOPE.md](/home/skyone/spec/student-service-platform-backend/TEAM_SCOPE.md)。
[需求整合](/home/skyone/spec/student-service-platform-backend/需求整合) 中 2026-03-23 新增需求文档已复核，当前后端方向仍以“多级权限、知识库、审批留痕、学生状态管理、保守型学业分析”为主。

## 原型链接

### 服务端小程序

https://js.design/f/eyyd7G?type=plugin&resourceId=696d07b3b5e8b987e5b0c43c&p=IKnxcq3HOR&mode=design

1. 部分交互由于重复未重新设计
2. 即时设计交互可能存在卡顿、无法跳转等问题
3. 暂未设置AI智能总结原型

### 后端管理平台

https://www.figma.com/make/ZJGFsGdIck8BCnslRGJC50/%E5%90%8E%E7%AB%AF%E7%AE%A1%E7%90%86%E5%B9%B3%E5%8F%B0%E5%8E%9F%E5%9E%8B%E5%9B%BE?t=WFEF4Lm4R919F51L-1&preview-route=%2Foperation-log

1. 前端设计时可以参考侧边栏的代码（figma make）

## 项目定位

本项目基于需求文档《学院学生综合服务与党团管理平台》及 13:10 会议增补需求搭建后端框架，服务对象包括：

- 学生端 Web / H5
- 管理员端 Web 后台

当前版本重点完成：

- Spring Boot 项目骨架
- 模块化目录设计
- 基础实体与数据表初始化脚本
- 按最新需求补齐的 mock 业务逻辑
- Swagger/OpenAPI 文档支持
- Kingbase 兼容的 PostgreSQL 方言配置基础
- 面向真实数据接入的后端表结构预留
- 管理接口的基础角色权限收敛

## 技术栈建议

- Java 17+
- Spring Boot 3
- Spring Web / Validation / Security / JPA
- Flyway
- PostgreSQL 驱动
- KingbaseES 通过 PostgreSQL 协议兼容接入

## 模块划分

- `auth`: 登录认证、学校账号/微信绑定预留
- `student`: 学生基本信息管理
- `knowledge`: 智能问答与政策知识库
- `party`: 党团事务流程管理
- `notice`: 信息聚类与精准推送
- `certificate`: 电子证明生成与审批流程
- `academic`: 学业分析与预警
- `common`: 公共返回体、异常、基础模型、通用筛选/校验辅助
- `config`: 安全与文档配置

## 当前运行模式

默认启用 `mock` profile，不依赖真实数据库即可启动，适合：

- 你先完成后端任务
- 给学生端小程序联调
- 给管理后台先对接接口
- 后续再和数据库负责人做 Kingbase 联调

当前版本已改为统一 controller + 可切换 service 实现：

- `mock` profile 下：使用内存演示数据，但接口路由与鉴权方式保持可联调状态
- 非 `mock` profile 下：切换到 JPA/Flyway/数据库实现，不需要重写 controller 层

按最新会议需求，当前实现重点偏向：

- 多级角色与账号体系预留
- 知识库标准答案与模板下载
- 党团固定流程展示与节点提醒
- 基础通知分发
- 分级审批与撤回/重提状态流转预留
- 管理员操作日志与导入任务查看
- 学生本人信息查询与管理端数据脱敏
- 班主任默认按负责范围收敛学生信息查询
- 学生工作记录管理统计与学生画像元数据结构
- 学生工作记录编辑、删除、动作审计历史
- 电子证明“仅本人申请/查询”的后端权限校验
- 班主任负责范围管理接口

当前仓库只覆盖“人员 2：后端 + 业务逻辑负责人”的工作范围，不包含小程序页面、PC 管理后台页面和项目整合交付。

## 需求落地状态

结合 `需求整合` 中 2026-03-23 需求差异文档，当前后端已明确落地的需求包括：

- 多级角色与账号权限模型
- 学生数据范围控制与班主任负责范围收敛
- 知识库管理、附件上传、模板下载
- 审批流、审批历史、管理员操作日志
- 导入任务、导入错误明细、导入结果回执
- 导入执行结果整批回填、错误快照替换、执行批次号/回调来源留痕
- 学生状态字段、状态历史、学生画像元数据
- 平台安全策略、上传策略、文件上传与通知发送记录
- 电子证明“仅学生本人申请/查询/学生端撤回重提”的权限校验

当前仍应视为“部分完成 / 继续迭代”的需求包括：

- Kingbase 真实数据联调与桥接脚本落库验收
- 学业分析已补充结构化人工复核提示，后续仍可继续细化规则，但不输出高风险自动毕业判断
- 部分平台/管理能力的真实数据闭环验证，而不只是 mock 联调

## 已提供接口

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/wechat-login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/change-password`
- `POST /api/v1/auth/logout`
- `GET /api/v1/knowledge/search?keyword=`
- `GET /api/v1/knowledge/{id}`
- `GET /api/v1/knowledge/templates`
- `GET /api/v1/party-progress/{studentId}`
- `GET /api/v1/party-progress/{studentId}/timeline`
- `GET /api/v1/party-progress/{studentId}/reminders`
- `GET /api/v1/notices/student/{studentId}`
- `POST /api/v1/certificates/requests`
- `GET /api/v1/certificates/requests/student/{studentId}`
- `GET /api/v1/certificates/requests/{requestId}/history`
- `GET /api/v1/certificates/requests/{requestId}/preview`
- `POST /api/v1/certificates/requests/{requestId}/action`
- `GET /api/v1/student/me`
- `GET /api/v1/student/dashboard`
- `GET /api/v1/student/growth-suggestions`
- `GET /api/v1/student/notices`
- `GET /api/v1/student/certificates/requests`
- `GET /api/v1/student/party-progress`
- `GET /api/v1/student/party-progress/reminders`
- `GET /api/v1/student/knowledge/recommended`
- `GET /api/v1/academic/analysis/{studentId}`
- `POST /api/v1/worklogs`
- `PUT /api/v1/worklogs/{id}`
- `DELETE /api/v1/worklogs/{id}`
- `GET /api/v1/worklogs/student/{studentId}`
- `GET /api/v1/worklogs/student/{studentId}/summary`
- `GET /api/v1/worklogs/{id}/actions`
- `GET /api/v1/worklogs/overview`
- `GET /api/v1/worklogs/admin/filter`
- `GET /api/v1/worklogs/admin/stats`
- `GET /api/v1/worklogs/admin/page`
- `GET /api/v1/worklogs/admin/export-metadata`
- `GET /api/v1/admin/notices`
- `GET /api/v1/admin/notices/page`
- `GET /api/v1/admin/notices/stats`
- `POST /api/v1/admin/notices`
- `GET /api/v1/admin/knowledge`
- `GET /api/v1/admin/knowledge/page`
- `GET /api/v1/admin/knowledge/stats`
- `POST /api/v1/admin/knowledge`
- `PUT /api/v1/admin/knowledge/{id}`
- `GET /api/v1/admin/knowledge/{id}/attachments`
- `POST /api/v1/admin/knowledge/{id}/attachments`
- `DELETE /api/v1/admin/knowledge/attachments/{attachmentId}`
- `GET /api/v1/admin/advisor-scopes`
- `GET /api/v1/admin/advisor-scopes/page`
- `GET /api/v1/admin/advisor-scopes/stats`
- `POST /api/v1/admin/advisor-scopes`
- `PUT /api/v1/admin/advisor-scopes/{id}`
- `DELETE /api/v1/admin/advisor-scopes/{id}`
- `GET /api/v1/admin/students`
- `GET /api/v1/admin/students/page`
- `GET /api/v1/admin/students/stats`
- `GET /api/v1/admin/students/{id}`
- `POST /api/v1/admin/students`
- `PUT /api/v1/admin/students/{id}`
- `GET /api/v1/admin/students/{id}/status-history`
- `POST /api/v1/admin/students/{id}/status-history`
- `GET /api/v1/admin/students/{id}/portrait`
- `PUT /api/v1/admin/students/{id}/portrait`
- `GET /api/v1/admin/students/portraits/page`
- `GET /api/v1/admin/students/portraits/stats`
- `GET /api/v1/admin/approvals`
- `GET /api/v1/admin/approvals/page`
- `GET /api/v1/admin/approvals/stats`
- `GET /api/v1/admin/approvals/{requestId}/history`
- `POST /api/v1/admin/approvals/{requestId}/action`
- `GET /api/v1/admin/operation-logs`
- `GET /api/v1/admin/operation-logs/page`
- `GET /api/v1/admin/operation-logs/stats`
- `GET /api/v1/admin/import-tasks`
- `GET /api/v1/admin/import-tasks/page`
- `GET /api/v1/admin/import-tasks/stats`
- `POST /api/v1/admin/import-tasks`
- `PUT /api/v1/admin/import-tasks/{id}`
- `GET /api/v1/admin/import-tasks/{id}/errors`
- `GET /api/v1/admin/import-tasks/{id}/errors/page`
- `POST /api/v1/admin/import-tasks/{id}/errors`
- `GET /api/v1/admin/stats`
- `GET /api/v1/platform/contracts`
- `GET /api/v1/platform/student-ui-contract`
- 平台导入任务状态统一使用 `CREATED / RUNNING / SUCCESS / PARTIAL_SUCCESS / FAILED`
- `GET /api/v1/platform/users/me/permissions`
- `GET /api/v1/platform/users/me/student-scope`
- `GET /api/v1/platform/users/me/student-scope/check-student`
- `GET /api/v1/platform/users/me/student-scope/check-range`
- `GET /api/v1/platform/roles`
- `GET /api/v1/platform/security-policy`
- `PUT /api/v1/platform/security-policy`
- 平台安全策略已落到 `platform_system_setting`，更新后可供认证登录、用户创建、重置密码统一复用
- `GET /api/v1/platform/upload-policy`
- `PUT /api/v1/platform/upload-policy`
- 平台上传策略同样落到 `platform_system_setting`，统一约束 `/api/v1/platform/files/upload` 的大小与内容类型校验
- `GET /api/v1/platform/users`
- `GET /api/v1/platform/users/{userId}`
- `POST /api/v1/platform/users`
- `PUT /api/v1/platform/users/{userId}`
- `POST /api/v1/platform/users/{userId}/enabled`
- `POST /api/v1/platform/users/{userId}/unlock`
- `POST /api/v1/platform/users/{userId}/reset-password`
- `GET /api/v1/platform/users/page`
- `GET /api/v1/platform/users/stats`
- `GET /api/v1/platform/sessions/page`
- `POST /api/v1/platform/sessions/{sessionId}/revoke`
- `GET /api/v1/platform/students/{studentId}`
- `GET /api/v1/platform/students/page`
- `POST /api/v1/platform/files/upload`
- `GET /api/v1/platform/files/page`
- `POST /api/v1/platform/files/{id}/archive`
- `DELETE /api/v1/platform/files/{id}`
- 上传接口统一复用平台上传策略，返回格式保持 `PlatformFileUploadResponse`
- `GET /api/v1/platform/audit/login-logs/page`
- `POST /api/v1/platform/import-tasks`
- `PUT /api/v1/platform/import-tasks/{taskId}`
- `GET /api/v1/platform/import-tasks/page`
- `GET /api/v1/platform/import-tasks/{taskId}/receipt`
- `POST /api/v1/platform/import-tasks/{taskId}/execution-result`
- `POST /api/v1/platform/import-tasks/{taskId}/errors`
- `GET /api/v1/platform/import-tasks/{taskId}/errors/page`
- 平台侧已支持“导入任务创建 -> 执行结果整批回填 -> 错误快照替换 -> 结果回执查询”的完整后端链路
- 执行结果回填现已要求 `executionBatchNo`、`callbackSource`，并且仅允许任务负责人、`COLLEGE_ADMIN`、`SUPER_ADMIN` 回填
- `GET /api/v1/platform/audit/admin-operation-logs/page`
- 管理操作日志分页结果包含 `detail`，安全策略更新会记录修改前后值
- `GET /api/v1/platform/audit/approval-logs/{requestId}`
- `GET /api/v1/platform/notifications/send-records`
- `POST /api/v1/platform/notifications/send`
- `GET /api/v1/platform/notifications/send-records/page`
- 通知发送记录已落表到 `platform_notification_send_record`，与业务公告 `notice` 解耦
- 通知渠道统一使用 `IN_APP / EMAIL / WECHAT`，目标类型统一使用 `ALL / GRADE / CLASS / SELF`

其中学生端已提供两类聚合能力：

- `GET /api/v1/student/dashboard`：学生首页聚合数据，包含 `focusItems`
- `GET /api/v1/student/growth-suggestions`：学生成长建议清单，独立输出学业、画像、工作记录、证明待办建议

## 平台统一约定

- 用户标识字段统一为 `userId`，学生标识字段统一为 `studentId`，学号字段统一为 `studentNo`
- 角色枚举统一复用 `RoleType`：`STUDENT`、`CLASS_LEADER`、`LEAGUE_SECRETARY`、`COUNSELOR`、`CLASS_ADVISOR`、`COLLEGE_ADMIN`、`SUPER_ADMIN`、`ASSISTANT`
- 权限校验统一通过 `CurrentUserService`，学生数据范围能力对内复用 `StudentDataScopeService`
- 通用返回统一为 `ApiResponse<T>`，分页统一为 `ApiResponse<PageResponse<T>>`

## 最新数据库对接

`db/kingbase_schema.sql` 是 2026-03-27 新更新的统一数据库模型，表命名已经切到：

- `sys_*`：统一用户/认证/权限
- `kb_*`：知识库
- `notice_*`：通知公告
- `party_*`：党团流程
- `affair_* / cert_*`：办事与证明
- `aca_*`：学业审核

而当前后端非 mock 模式仍然围绕历史 JPA 表：

- `user_account`
- `student_profile`
- `knowledge_document`
- `notice`
- `certificate_request`
- `academic_warning_record`
- 以及审批、画像、工作日志、平台审计等扩展表

为避免一次性大改 Java 服务层，仓库新增了桥接脚本：

- [db/kingbase_backend_bridge.sql](/home/skyone/spec/student-service-platform-backend/db/kingbase_backend_bridge.sql)

它做两件事：

- 在 `campus` schema 下补齐当前后端依赖但新总库中不存在的扩展表
- 将 `sys_* / kb_* / notice_* / party_* / cert_* / aca_*` 中可直接映射的数据导入到当前后端使用的旧表

建议接入顺序：

1. 先执行 `db/kingbase_schema.sql`
2. 再执行 `db/kingbase_backend_bridge.sql`
3. 启动应用时切换到 `kingbase` profile：`--spring.profiles.active=kingbase`

也可以直接用初始化脚本：

- [scripts/init-kingbase-bridge.sh](/home/skyone/spec/student-service-platform-backend/scripts/init-kingbase-bridge.sh)

示例：

```bash
cd /home/skyone/spec/student-service-platform-backend
DB_HOST=127.0.0.1 DB_PORT=54321 DB_NAME=student_service_platform DB_USER=postgres DB_PASSWORD=postgres LOAD_SAMPLE_DATA=true \
  bash scripts/init-kingbase-bridge.sh
java -jar target/student-service-platform-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=kingbase
```

说明：

- `LOAD_SAMPLE_DATA=true` 时会先执行 [db/campus_sample_data.sql](/home/skyone/spec/student-service-platform-backend/db/campus_sample_data.sql)
- 桥接脚本会把最新样例库里的用户、学生档案、知识库、通知、审批、学业审核、导入任务、操作日志同步到当前后端依赖表
- 对于最新样例库里无法直接用于 BCrypt 校验的占位密码，桥接脚本会统一回填演示密码 `123456`

对应配置文件：

- [src/main/resources/application-kingbase.yml](/home/skyone/spec/student-service-platform-backend/src/main/resources/application-kingbase.yml)

当前桥接策略说明：

- 这是“先跑通后端”的兼容层，不是最终态领域模型
- 新库到旧表是初始化导入，不做双向同步
- 如果后续要彻底统一到 `sys_* / kb_* / notice_* ...`，需要继续把 JPA 实体和 Service 改成新模型
- `application-dev.yml` 已补 `hibernate.default_schema=campus`，避免非 mock 模式默认查到 `public` schema

当前已开始原生接最新库的模块：

- 认证模块在 `kingbase` profile 下优先直连 `sys_user / sys_user_auth / sys_user_role / sys_student_ext`
- 知识库学生侧接口在 `kingbase` profile 下优先直连 `kb_policy / cert_template / file_object`
- 证明申请学生侧主流程在 `kingbase` profile 下优先直连 `affair_request / cert_application / workflow_*`
- 证明审批列表/统计/处理与申请预览在 `kingbase` profile 下优先直连 `affair_request / cert_application / workflow_*`
- 学生档案核心信息在 `kingbase` profile 下优先直连 `sys_user / sys_student_ext`
- 学生通知列表在 `kingbase` profile 下优先直连 `notice_item / notice_item_tag / notice_tag_dict`
- 党团进度与提醒在 `kingbase` profile 下优先直连 `party_student_progress / party_flow_node / party_reminder_task`
- 管理端通知列表/分页/统计/创建在 `kingbase` profile 下优先直连 `notice_item / notice_item_tag / notice_tag_dict / notice_delivery`
- 管理端知识库列表/分页/统计/增改/附件管理在 `kingbase` profile 下优先直连 `kb_policy / file_object`
- 学业分析与预警在 `kingbase` profile 下优先直连 `aca_audit_report / aca_audit_missing / aca_course_recommendation / aca_term_course`
- 平台统计、管理操作日志、导入任务主表在 `kingbase` profile 下优先直连 `sys_user / notice_item / kb_policy / sys_operation_log / audit_import_job`
- 平台服务侧产生的操作日志在 `kingbase` profile 下同步写入 `sys_operation_log`
- 导入错误明细在 `kingbase` profile 下优先内嵌到 `audit_import_job.result_json.errors`
- 平台上传记录在 `kingbase` profile 下优先使用 `file_object` 存储文件元数据，并通过 `sys_operation_log` 回放 `bizType / bizId / archived`
- 登录失败次数、锁定时间、会话黑名单等平台安全辅助状态仍复用现有后端表

当前仍通过桥接表运行的模块：

- 学生画像、状态轨迹等扩展档案，当前仍复用 `student_portrait / student_status_history`
- 班主任负责范围，当前仍复用 `advisor_scope_binding`
- 学生工作日志，当前仍复用 `student_work_log / student_work_log_action_log`
- 平台安全与会话辅助状态，当前仍复用 `user_account / login_audit_log / user_session_record / revoked_token_record`
- 平台通知发送记录与平台系统配置，当前仍复用 `platform_notification_send_record / platform_system_setting`
- 文件上传统一返回 `PlatformFileUploadResponse`
- 审批状态统一复用 `ApprovalStatus`：`PENDING`、`COUNSELOR_APPROVED`、`APPROVED`、`REJECTED`、`WITHDRAWN`
- 审批日志字段统一为 `operatorId`、`operatorName`、`operatorRole`、`action`、`fromStatus`、`toStatus`、`comment`、`operatedAt`
- 管理操作日志字段统一为 `operatorId`、`operatorName`、`operatorRole`、`module`、`action`、`target`、`result`、`detail`
- 学生端动作类型、优先级、默认跳转路径统一由 `/api/v1/platform/contracts` 和 `/api/v1/platform/student-ui-contract` 对外提供

## 演示账号

- 管理员：`admin / 123456`
- 辅导员：`teacher01 / 123456`
- 团支书：`2023100002 / 123456`
- 学生：`2023100001 / 123456`

在非 `mock` 模式下，如果数据库已执行 Flyway migration，也会自动写入以上种子账号与基础演示数据。

## 数据库说明

默认配置使用 PostgreSQL URL：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:54321/student_service_platform
```

如果你后续直接切到 Kingbase，请按实际驱动与连接串调整：

- 驱动
- JDBC URL
- Hibernate dialect

切换到真实数据库模式时，不要启用 `mock` profile。例如：

```bash
cd /home/skyone/spec/student-service-platform-backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

或直接运行打包后的 jar：

```bash
java -jar target/student-service-platform-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

只要不是 `mock`，应用就会启用数据源、JPA、Flyway 和数据库实现。

## 当前限制

- `mock` profile 下已接入 JWT 登录与 Bearer Token 鉴权，但用户数据仍来自演示账号
- 非 `mock` profile 下已支持 JWT 签发与 Bearer Token 校验，微信登录仍未接入
- 非 `mock` profile 下部分业务仍是基础版实现，复杂审批流、精准筛选、统计口径仍需继续细化
- 已新增 `advisor_scope_binding` 负责范围结构；`advisorScope` 仍保留为兼容字段，后续可逐步下线
- 已新增 `student_work_log_action_log`，用于保留工作记录新增/修改/删除的审计轨迹
- 已新增知识附件接口，支持老师持续上传政策文件附件元数据
- 已新增导入错误明细接口，支持把 Excel 校验失败结果绑定到具体导入任务
- 未实现 Excel/Word/PDF 导入导出服务
- 未实现成绩单 PDF 解析与课程匹配逻辑
- 未实现消息推送调度

## 当前已识别问题

结合现有代码、接口矩阵与测试，当前后端的主要细化不足集中在以下几类：

- 输入约束不统一：部分 controller 已有 `@Valid`，但分页参数、路径主键、查询主键、行号等仍缺少统一校验，负页码、`size=0`、非法 ID 可能直接进入业务层。
- mock / 非 mock 行为不完全一致：学生档案、状态历史、学生画像在 mock 模式下有较完整的业务校验，真实数据库模式下同类校验明显偏弱，存在“联调正常、落库后可写入脏数据”的风险。
- 文档与契约约束写得不够明确：现有 README / API 文档更偏接口清单，缺少参数边界、错误约定、这一轮修复范围说明，前后端联调时不利于快速对齐。
- 错误路径覆盖还偏少：主流程测试已建立，但对非法分页、非法主键、非法筛选边界等输入类错误覆盖不足。

## 本轮修复范围

本轮优先处理会直接影响联调稳定性和真实落库一致性的部分：

- 为高频后台接口补齐分页、主键、行号等参数校验，统一拦截明显非法请求
- 补齐学生档案、状态历史、画像在非 `mock` 模式下缺失的业务校验
- 补充对应错误流测试，确保契约可回归
- 同步 README / API 文档中的参数约束与修复说明

## 启动方式

```bash
cd /home/skyone/spec/student-service-platform-backend
mvn spring-boot:run
```

如果你的环境可以正常访问 Maven Central，也可以直接使用：

```bash
cd /home/skyone/spec/student-service-platform-backend
mvn spring-boot:run
```

## Maven 构建说明

项目内已提供以下构建辅助配置：

- `.mvn/jvm.config`：固定 Maven 的 TLS 协议并优先使用 IPv4，降低 HTTPS 握手失败概率
- `settings.xml.example`：示例 Maven 仓库配置，按顺序声明 Maven Central、阿里云、华为云多个公共仓库
- `.m2/repository`：建议作为项目本地仓库目录使用，避免默认用户目录权限或环境差异影响构建

首次构建建议使用：

```bash
cd /home/skyone/spec/student-service-platform-backend
mkdir -p .m2/repository
mvn clean package
```

如果出现：

- `error: release version 17 not supported`

说明 Maven 当前实际使用的 JDK 低于 17。先检查：

```bash
mvn -version
java -version
echo "$JAVA_HOME"
which java
which javac
```

如果 `mvn -version` 显示的 Java 不是 17 或更高，需要修正环境变量。例如：

```bash
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
mvn -version
```

在 Ubuntu / Debian 上也可以用：

```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

修正后再重新构建：

```bash
mvn -s settings.xml.example -Dmaven.repo.local=.m2/repository clean package
```

项目已通过 [.mvn/maven.config](/home/skyone/spec/student-service-platform-backend/.mvn/maven.config) 固化以下 Maven 参数：

- `-s settings.xml.example`
- `-Dmaven.repo.local=.m2/repository`

因此在项目目录下，后续直接执行 `mvn clean package`、`mvn spring-boot:run` 即可。

如果仍然报下面这类错误：

- `Remote host terminated the handshake: SSL peer shut down incorrectly`
- `Unknown host ... Temporary failure in name resolution`

说明问题在当前机器的外网访问、DNS、代理或证书链，而不是 `pom.xml` 依赖声明本身。可优先检查：

- 是否能访问 `https://repo.maven.apache.org/maven2`
- 是否能解析 `maven.aliyun.com`
- 是否能解析 `repo.huaweicloud.com`
- 是否需要配置系统代理或 Maven 代理
- 公司/校园网络是否拦截了 HTTPS 下载
- 系统 CA 证书是否完整

启动后可访问：

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- 健康检查: `http://localhost:8080/actuator/health`

## 鉴权说明

登录成功后会返回 JWT token。后续访问除登录、微信登录、健康检查、Swagger 外的大多数接口时，需要携带：

```bash
Authorization: Bearer <token>
```

示例：

```bash
curl -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}' \
  http://localhost:8080/api/v1/auth/login
```

取到返回中的 `token` 后，可继续访问：

```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/v1/auth/me
```

## 冒烟验证

项目已提供一键冒烟脚本：

```bash
cd /home/skyone/spec/student-service-platform-backend
./scripts/smoke-test.sh
```

默认会使用：

- 地址：`http://localhost:8080`
- 账号：`admin`
- 密码：`123456`

也可以通过环境变量覆盖：

```bash
BASE_URL=http://localhost:8080 \
SMOKE_USERNAME=2023100001 \
SMOKE_PASSWORD=123456 \
./scripts/smoke-test.sh
```

## 测试

已补充基于 `MockMvc` 的认证集成测试：

- `src/test/java/edu/ruc/platform/AuthIntegrationTest.java`
- `src/test/java/edu/ruc/platform/BusinessFlowIntegrationTest.java`
- `src/test/java/edu/ruc/platform/StudentFeatureIntegrationTest.java`
- `src/test/java/edu/ruc/platform/PartyAcademicIntegrationTest.java`
- `src/test/java/edu/ruc/platform/ErrorFlowIntegrationTest.java`

当前覆盖：

- 登录签发 JWT
- `/api/v1/auth/me`
- 受保护接口未登录拒绝访问
- 管理端统计、审批动作
- 学生通知列表
- 知识库搜索与模板列表
- 证明申请创建与学生侧查询
- 党团进度、时间线、提醒
- 学业分析接口
- 错误登录、非法 Token、错误审批动作

执行：

```bash
cd /home/skyone/spec/student-service-platform-backend
mvn test
```

如果首次执行测试时卡在 `surefire-junit-platform` 下载，说明仍是当前机器的 Maven 仓库网络问题，不是测试代码本身的问题。

## 目前适合联调的范围

- 人员 1：学生端小程序页面
- 人员 3：管理后台页面
- 你自己：接口联调、流程演示、答辩演示

## 下一步建议

1. 先让人员 1 和人员 3 按接口联调。
2. 再和人员 4 对接 Kingbase 表结构。
3. 最后把 mock service 替换成真实 repository + service 实现。
 现在 mock profile 下，主业务面和主要错误路径基本都已经有测试了。下一步如果还继续，最合理的是：

  - 开始补 dev profile 的最小启动测试
  - 或者把真实模式的 repository/service 做更多业务校验，再配套补测试

  我建议下一轮开始做 dev profile 最小启动测试。
