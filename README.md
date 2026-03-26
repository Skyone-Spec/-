# 学院学生综合服务与党团管理平台后端

团队分工与当前仓库边界见 [TEAM_SCOPE.md](/home/skyone/spec/student-service-platform-backend/TEAM_SCOPE.md)。
[需求整合](/home/skyone/spec/student-service-platform-backend/需求整合) 中 2026-03-23 新增需求文档已复核，当前后端方向仍以“多级权限、知识库、审批留痕、学生状态管理、保守型学业分析”为主。

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
- `common`: 公共返回体、异常、基础模型
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
- `POST /api/v1/platform/import-tasks/{taskId}/errors`
- `GET /api/v1/platform/import-tasks/{taskId}/errors/page`
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
