# 后端接口文档

本文档对应项目：[student-service-platform-backend](/home/skyone/spec/student-service-platform-backend)

当前版本为 `mock` 联调版，默认不依赖真实数据库。

当前文档已同步 13:10 会议后的核心调整：

- 先以 Web/后台形态为主
- 微信登录保留预留接口，但不是当前唯一主路径
- 角色从简单二级扩展为多级角色
- 审批流支持分级审批语义
- 新增管理员操作日志与导入任务查看接口
- 管理端接口进一步统一为 `list + page + stats` 结构

## 通用说明

- 基础前缀：`/api/v1`
- 返回格式：

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

- 平台统一命名约定：
  - 账号主键统一为 `userId`
  - 学生实体主键统一为 `studentId`
  - 学号统一为 `studentNo`
  - 角色枚举统一复用 `RoleType`
  - 权限校验入口统一复用 `CurrentUserService`
- 分页接口统一为 `ApiResponse<PageResponse<T>>`
- 分页请求参数统一为 `page`、`size`

## 平台契约清单

- 标识字段：
  - 用户主键统一为 `userId`
  - 学生主键统一为 `studentId`
  - 学号字段统一为 `studentNo`
- 角色枚举：
  - 统一复用 `RoleType`
  - 当前枚举值：`STUDENT`、`CLASS_LEADER`、`LEAGUE_SECRETARY`、`COUNSELOR`、`CLASS_ADVISOR`、`COLLEGE_ADMIN`、`SUPER_ADMIN`、`ASSISTANT`
- 权限校验入口：
  - 统一通过 `CurrentUserService`
  - 学生数据范围判定对内复用 `StudentDataScopeService`
- 通用响应：
  - 所有接口统一返回 `ApiResponse<T>`
  - 成功返回固定字段：`success`、`message`、`data`
  - 分页响应统一为 `ApiResponse<PageResponse<T>>`
  - 分页字段统一为 `content`、`totalElements`、`totalPages`、`page`、`size`
- 文件上传返回：
  - 统一返回 `PlatformFileUploadResponse`
  - 字段统一为 `id`、`bizType`、`bizId`、`fileName`、`contentType`、`fileSize`、`storagePath`、`uploadedBy`、`uploadedAt`
  - 其中 `uploadedBy` 当前返回上传人展示名
  - 上传记录分页统一返回 `PlatformFileUploadRecordResponse`
  - 上传记录额外字段为 `uploadedById`、`archived`、`deleted`
- 审批状态命名：
  - 统一复用 `ApprovalStatus`
  - 当前状态值：`PENDING`、`COUNSELOR_APPROVED`、`APPROVED`、`REJECTED`、`WITHDRAWN`
- 审批日志字段命名：
  - 统一使用 `operatorId`、`operatorName`、`operatorRole`、`action`、`fromStatus`、`toStatus`、`comment`、`operatedAt`
- 管理操作日志字段命名：
  - 统一使用 `operatorId`、`operatorName`、`operatorRole`、`module`、`action`、`target`、`result`、`detail`
- 学生端动作约定：
  - 类型枚举统一复用 `studentActionTypes`
  - 优先级枚举统一复用 `studentActionPriorities`
  - 默认跳转统一复用 `studentActionPaths`
  - 前端展示元数据统一通过 `GET /api/v1/platform/student-ui-contract`

## 0. 平台基础能力

当前代码中的平台基础接口矩阵如下，以下清单以实际 controller 路由为准：

- `GET /api/v1/platform/contracts`
- `GET /api/v1/platform/student-ui-contract`
- `GET /api/v1/platform/users/me/permissions`
- `GET /api/v1/platform/users/me/student-scope`
- `GET /api/v1/platform/users/me/student-scope/check-student`
- `GET /api/v1/platform/users/me/student-scope/check-range`
- `GET /api/v1/platform/roles`
- `GET /api/v1/platform/security-policy`
- `PUT /api/v1/platform/security-policy`
- `GET /api/v1/platform/upload-policy`
- `PUT /api/v1/platform/upload-policy`
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
- `POST /api/v1/platform/import-tasks`
- `PUT /api/v1/platform/import-tasks/{taskId}`
- `GET /api/v1/platform/import-tasks/page`
- `GET /api/v1/platform/import-tasks/{taskId}/errors/page`
- `POST /api/v1/platform/import-tasks/{taskId}/errors`
- `GET /api/v1/platform/import-tasks/{taskId}/receipt`
- `GET /api/v1/platform/audit/admin-operation-logs/page`
- `GET /api/v1/platform/audit/login-logs/page`
- `GET /api/v1/platform/audit/approval-logs/{requestId}`
- `GET /api/v1/platform/notifications/send-records`
- `POST /api/v1/platform/notifications/send`
- `GET /api/v1/platform/notifications/send-records/page`

### 0.1 平台约定查询

- `GET /api/v1/platform/contracts`
- `GET /api/v1/platform/student-ui-contract`

返回字段：
- `userIdField`
- `studentIdField`
- `studentNoField`
- `permissionCheckEntry`
- `uploadResponseType`
- `pageRequest`
- `pageResponse`
- `roleEnums`
- `dataScopes`
- `approvalStatuses`
- `importTaskStatuses`
- `notificationChannels`
- `notificationTargetTypes`
- `notificationSendStatuses`
- `approvalLogFields`
- `operationLogFields`
- `studentActionTypes`
- `studentActionPriorities`
- `studentActionPaths`

说明：
- `studentActionTypes` 返回学生端聚合卡片/建议卡片的统一类型枚举
- `studentActionPriorities` 返回学生端卡片优先级统一枚举
- `studentActionPaths` 返回学生端默认跳转路径约定，供 `focusItems.actionPath`、`growth-suggestions.actionPath` 以及前端路由映射统一复用

### 0.1.1 学生端 UI 契约

返回字段：
- `actionTypes`
- `actionPriorities`
- `actions`

`actions` 子项字段：
- `type`
- `defaultPriority`
- `actionPath`
- `iconKey`
- `colorKey`

说明：
- 该接口面向前端展示层，返回学生端卡片的默认展示元数据
- `type` 与 `studentActionTypes` 对齐
- `defaultPriority` 与 `studentActionPriorities` 对齐
- `iconKey`、`colorKey` 用于前端统一映射图标和视觉样式，不承载业务判断

### 0.2 当前用户权限快照

- `GET /api/v1/platform/users/me/permissions`

返回字段：
- `userId`
- `username`
- `role`
- `studentNo`
- `grade`
- `dataScopes`
- `permissionEntries`

### 0.2.1 用户管理与角色查询

- `GET /api/v1/platform/users`

查询参数：
- `role`
- `enabled`
- `keyword`

返回字段：
- `userId`
- `username`
- `role`
- `enabled`
- `studentNo`
- `name`
- `grade`
- `major`

### 0.3 通用学生信息查询

- `GET /api/v1/platform/students/{studentId}`
- `GET /api/v1/platform/students/page`

说明：
- 对外统一使用 `studentId`
- 敏感字段统一放在 `sensitiveFields` 中返回
- 未授权场景只返回已脱敏字段

### 0.4 通用文件上传

- `POST /api/v1/platform/files/upload`

表单字段：
- `file`
- `bizType`
- `bizId`

返回字段：
- `id`
- `bizType`
- `bizId`
- `fileName`
- `contentType`
- `fileSize`
- `storagePath`
- `uploadedBy`
- `uploadedAt`

### 0.5 导入任务分页

- `GET /api/v1/platform/import-tasks/page`

导入任务状态约定：
- `CREATED`
- `RUNNING`
- `SUCCESS`
- `PARTIAL_SUCCESS`
- `FAILED`

导入结果回执字段：
- `taskId`
- `taskType`
- `fileName`
- `status`
- `totalRows`
- `successRows`
- `failedRows`
- `owner`
- `createdAt`
- `errorCount`
- `recentErrors`
- `receiptCode`
- `generatedAt`

### 0.6 审计基础接口

- `GET /api/v1/platform/audit/admin-operation-logs/page`
- `GET /api/v1/platform/audit/approval-logs/{requestId}`

### 0.7 通知基础接口

- `GET /api/v1/platform/notifications/send-records`

返回字段：
- `id`
- `title`
- `channel`
- `targetType`
- `targetDescription`
- `status`
- `recipientCount`
- `triggeredBy`
- `sentAt`
- `extensionChannels`

说明：
- 当前先提供站内通知发送记录基础模型
- `extensionChannels` 预留邮件、微信等扩展位
- 学生数据范围判定已抽为公共服务，`CurrentUserService` 继续作为统一权限入口，对内复用 `StudentDataScopeService`

日志字段命名规范：
- 审批日志统一使用 `operatorId`、`operatorName`、`operatorRole`、`action`、`fromStatus`、`toStatus`、`comment`、`operatedAt`
- 管理操作日志统一使用 `operatorId`、`operatorName`、`operatorRole`、`module`、`action`、`target`、`result`、`detail`

- 演示账号：
  - 超级管理员：`admin / 123456`
  - 辅导员：`teacher01 / 123456`
  - 团支书：`2023100002 / 123456`
  - 学生：`2023100001 / 123456`

## 1. 认证模块

当前认证接口矩阵：

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/wechat-login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/change-password`
- `POST /api/v1/auth/logout`

### 1.1 账号登录

- `POST /api/v1/auth/login`

请求体：

```json
{
  "username": "2023100001",
  "password": "123456"
}
```

返回示例：

```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "userId": 10001,
    "username": "2023100001",
    "role": "STUDENT",
    "token": "mock-jwt-token-student"
  }
}
```

### 1.2 微信登录

- `POST /api/v1/auth/wechat-login`

请求体：

```json
{
  "code": "wx-login-code"
}
```

### 1.3 当前用户信息

- `GET /api/v1/auth/me`

返回字段：
- `userId`
- `username`
- `role`
- `studentNo`
- `name`
- `major`
- `grade`

角色示例：
- `STUDENT`
- `LEAGUE_SECRETARY`
- `COUNSELOR`
- `SUPER_ADMIN`

### 1.4 当前用户修改密码

- `POST /api/v1/auth/change-password`

请求体：

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

### 1.5 退出登录

- `POST /api/v1/auth/logout`

说明：
- 需要携带当前 Bearer Token
- 平台会将该令牌加入失效列表

## 平台层接口

以下接口属于基础平台能力，供业务模块和前端复用：

- 统一命名：`userId / studentId / studentNo`
- 统一分页：`ApiResponse<PageResponse<T>>`
- 统一权限入口：`CurrentUserService`
- 统一审批状态：`PENDING / COUNSELOR_APPROVED / APPROVED / REJECTED / WITHDRAWN`
- 统一导入任务状态：`CREATED / RUNNING / SUCCESS / PARTIAL_SUCCESS / FAILED`
- 通知渠道：`IN_APP / EMAIL / WECHAT`
- 通知目标类型：`ALL / GRADE / CLASS / SELF`
- 通知发送状态：`PENDING / SENT / FAILED`

### P1. 平台约定

- `GET /api/v1/platform/contracts`
- `GET /api/v1/platform/users/me/permissions`
- `GET /api/v1/platform/users/me/student-scope`
- `GET /api/v1/platform/users/me/student-scope/check-student`
- `GET /api/v1/platform/users/me/student-scope/check-range`
- `GET /api/v1/platform/security-policy`
- `PUT /api/v1/platform/security-policy`
- `GET /api/v1/platform/upload-policy`
- `PUT /api/v1/platform/upload-policy`
- 当前用户学生数据范围解析通过平台接口输出，底层复用 `StudentDataScopeService`
- 平台同时提供按 `studentId` 与按 `grade/className` 的范围预校验接口，返回 `allowed/reason`
- 安全策略配置持久化在 `platform_system_setting`，认证与平台管理共享同一套取值
- 上传策略配置也持久化在 `platform_system_setting`，供平台上传接口统一复用

### P2. 用户与权限

- `GET /api/v1/platform/roles`
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

用户写接口请求体：

```json
{
  "username": "platform-user-01",
  "role": "COUNSELOR",
  "enabled": true,
  "rawPassword": "123456",
  "passwordResetRequired": true
}
```

说明：
- 角色字段只允许 `RoleType`
- 若创建时不传 `rawPassword`，默认初始化为 `123456`
- `passwordResetRequired=true` 时，表示该账号首次登录后应尽快改密

会话接口说明：
- 平台会记录登录后的活动会话
- 管理员可按 `sessionId` 强制下线指定会话

密码重置请求体：

```json
{
  "newPassword": "654321"
}
```

### P3. 学生主数据通用查询

- `GET /api/v1/platform/students/{studentId}`
- `GET /api/v1/platform/students/page`

说明：
- 返回统一学生主数据结构
- 敏感字段通过 `sensitiveFields` 子对象脱敏返回

### P4. 通用上传

- `GET /api/v1/platform/upload-policy`
- `PUT /api/v1/platform/upload-policy`
- `POST /api/v1/platform/files/upload`
- `GET /api/v1/platform/files/page`
- `POST /api/v1/platform/files/{id}/archive`
- `DELETE /api/v1/platform/files/{id}`

上传策略字段：
- `maxFileSizeBytes`
- `allowedContentTypes`
- `allowEmptyContentType`

请求参数：
- `bizType`
- `bizId`
- `file`

上传记录分页查询参数：
- `bizType`
- `bizId`
- `uploaderKeyword`
- `page`
- `size`

说明：
- `/api/v1/platform/files/upload` 会先按平台上传策略校验文件大小与 `contentType`
- 若 `allowEmptyContentType=false` 且未识别到 `contentType`，接口会直接拒绝上传

### P5. 导入任务基础能力

- `POST /api/v1/platform/import-tasks`
- `PUT /api/v1/platform/import-tasks/{taskId}`
- `GET /api/v1/platform/import-tasks/page`
- `GET /api/v1/platform/import-tasks/{taskId}/receipt`
- `POST /api/v1/platform/import-tasks/{taskId}/errors`
- `GET /api/v1/platform/import-tasks/{taskId}/errors/page`
- 平台创建导入任务时自动取当前登录人作为 owner，并支持统一状态流转、错误登记与结果回执

### P6. 审计与日志

- `GET /api/v1/platform/audit/admin-operation-logs/page`
- `GET /api/v1/platform/audit/login-logs/page`
- `GET /api/v1/platform/audit/approval-logs/{requestId}`
- 平台安全策略更新会在管理操作日志 `detail` 中记录 before/after 变更内容

### P7. 通知发送记录

- `POST /api/v1/platform/notifications/send`
- `GET /api/v1/platform/notifications/send-records`
- `GET /api/v1/platform/notifications/send-records/page`
- 发送记录基础模型持久化在 `platform_notification_send_record`，可供业务模块复用
- 发送入口统一落通知发送记录，并保留 `triggeredBy / sentAt / extensionChannels`

## 2. 知识库模块

### 2.1 知识检索

- `GET /api/v1/knowledge/search?keyword=奖学金`

返回字段：
- `id`
- `title`
- `category`
- `officialUrl`
- `answer`

### 2.2 模板下载列表

- `GET /api/v1/knowledge/templates`

说明：
- 返回模板名称、分类、下载地址占位
- 当前已内置常用模板和知识库导入模板

### 2.3 知识详情

- `GET /api/v1/knowledge/{id}`

返回字段：
- `id`
- `title`
- `category`
- `officialUrl`
- `answer`
- `sourceFileName`
- `audienceScope`
- `attachments`
- `relatedItems`

## 3. 党团流程模块

### 3.1 当前进度查询

- `GET /api/v1/party-progress/{studentId}`

返回字段：
- `currentStage`
- `stageStartDate`
- `completedActions`
- `nextAction`

说明：
- 流程以固定党团步骤展示为主
- 具体日期仍以学院实际通知为准

### 3.2 流程时间线

- `GET /api/v1/party-progress/{studentId}/timeline`

返回字段：
- `studentId`
- `currentStage`
- `stages`

`stages` 子项字段：
- `stageName`
- `completed`
- `current`
- `reachedDate`
- `description`

### 3.3 节点提醒

- `GET /api/v1/party-progress/{studentId}/reminders`

返回字段：
- `title`
- `content`
- `remindDate`
- `level`

## 4. 通知模块

### 4.1 学生端精准通知列表

- `GET /api/v1/notices/student/{studentId}`

返回字段：
- `id`
- `title`
- `summary`
- `tags`
- `targetDescription`
- `publishTime`

## 5. 电子证明模块

### 5.1 学生提交证明申请

- `POST /api/v1/certificates/requests`

权限说明：
- 仅学生本人可为自己提交
- `SUPER_ADMIN`、`COLLEGE_ADMIN`、`COUNSELOR` 可代办/代查

请求体：

```json
{
  "studentId": 10001,
  "certificateType": "在读证明",
  "reason": "奖学金申请材料需要"
}
```

### 5.2 学生查看自己的申请记录

- `GET /api/v1/certificates/requests/student/{studentId}`

权限说明：
- 默认仅本人可查看
- 班主任不直接查看学生个人证明申请列表，统一走审批任务视图

返回字段：
- `id`
- `studentId`
- `certificateType`
- `status`
- `generatedPdfPath`

### 5.3 学生查看申请预览

- `GET /api/v1/certificates/requests/{requestId}/preview`

返回字段：
- `requestId`
- `studentId`
- `studentName`
- `certificateType`
- `status`
- `reason`
- `previewUrl`
- `generatedContent`
- `submittedAt`
- `withdrawalDeadline`
- `canWithdraw`
- `canResubmit`

### 5.4 学生查看申请历史

- `GET /api/v1/certificates/requests/{requestId}/history`

### 5.5 学生撤回 / 重提申请

- `POST /api/v1/certificates/requests/{requestId}/action`

请求体：

```json
{
  "action": "withdraw",
  "comment": "学生主动撤回"
}
```

说明：
- 学生端仅支持 `withdraw` 和 `resubmit`
- 审批端的 `approve`、`reject` 仍走管理员审批接口

## 5.6 学生自助聚合接口

当前学生自服务接口矩阵：

- `GET /api/v1/student/me`
- `GET /api/v1/student/dashboard`
- `GET /api/v1/student/growth-suggestions`
- `GET /api/v1/student/notices`
- `GET /api/v1/student/certificates/requests`
- `GET /api/v1/student/party-progress`
- `GET /api/v1/student/party-progress/reminders`
- `GET /api/v1/student/knowledge/recommended`

说明：
- 以上接口基于当前登录学生自动定位本人数据
- 前端无需再自行拼接 `studentId`

### `GET /api/v1/student/dashboard`

返回字段：
- `profile`
- `partyProgress`
- `reminders`
- `notices`
- `certificateRequests`
- `recommendedKnowledge`
- `focusItems`

`focusItems` 子项字段：
- `type`
- `title`
- `description`
- `priority`
- `actionLabel`
- `actionPath`

`focusItems.type` 当前约定值：
- `PARTY_REMINDER`
- `CERTIFICATE`
- `ACADEMIC`
- `WORKLOG`
- `PORTRAIT`
- `NOTICE`

说明：
- `focusItems.type` 与平台契约中的 `studentActionTypes` 保持一致

`focusItems.priority` 当前约定值：
- `HIGH`
- `MEDIUM`
- `LOW`

说明：
- `focusItems.priority` 与平台契约中的 `studentActionPriorities` 保持一致

首页关注项生成规则说明：
- `PARTY_REMINDER` 取当前最需优先处理的一条党团提醒，优先级跟随提醒等级
- `CERTIFICATE` 针对 `PENDING`、`WITHDRAWN` 的申请生成待办提示，其中撤回场景强调重新提交
- `ACADEMIC` 基于 `riskSummary.level` 和 `needsManualReview` 生成学业风险提示
- `WORKLOG` 根据学生工作记录数量、累计工作量分值与画像竞争力，区分“缺少记录”“记录偏少”“活跃沉淀”等提示
- `PORTRAIT` 根据 GPA、年级排名、荣誉与职业倾向生成“补强学业表现”“优势展示”“成长方向”类提示
- `NOTICE` 取学生首页排序后的第一条通知作为兜底提醒

说明：
- `focusItems` 按 `HIGH -> MEDIUM -> LOW` 排序
- 首页聚合接口保留统一结构，但 `focusItems` 用于承载学生端首页的优先行动建议
- 文案可随业务规则微调，但字段结构与类型枚举应保持稳定
- `actionPath` 为前端建议跳转路径，避免页面自行硬编码映射关系

`focusItems` 示例：

```json
[
  {
    "type": "ACADEMIC",
    "title": "学业风险提示",
    "description": "当前仍有培养方案学分未补齐，建议优先处理缺失课程并关注学业预警。",
    "priority": "HIGH",
    "actionLabel": "查看学业分析",
    "actionPath": "/student/academic-analysis"
  },
  {
    "type": "WORKLOG",
    "title": "优势经历建议尽快沉淀",
    "description": "画像显示你具备较强竞争力，已登记 5 分工作量，建议同步完善成果证明与展示材料。",
    "priority": "MEDIUM",
    "actionLabel": "查看工作记录",
    "actionPath": "/student/worklogs"
  },
  {
    "type": "PORTRAIT",
    "title": "画像优势明显",
    "description": "当前年级排名靠前，已有国奖,校优，职业倾向偏向升学，建议及时整理成果用于评优、推荐或升学材料。",
    "priority": "LOW",
    "actionLabel": "完善展示材料",
    "actionPath": "/student/portrait"
  }
]
```

### `GET /api/v1/student/growth-suggestions`

返回字段：
- `studentId`
- `studentName`
- `suggestions`

`suggestions` 子项字段：
- `category`
- `title`
- `description`
- `priority`
- `actionLabel`
- `actionPath`

`suggestions.category` 当前约定值：
- `ACADEMIC`
- `WORKLOG`
- `PORTRAIT`
- `CERTIFICATE`

说明：
- 该接口面向学生端单独渲染“成长建议”卡片，不依赖首页聚合结构
- 建议来源统一复用学业分析、工作记录汇总、成长画像、证明申请状态四类基础能力
- 返回列表按 `HIGH -> MEDIUM -> LOW` 排序
- `category` 表示建议来源模块，前端可据此映射图标、颜色或跳转入口
- `actionPath` 为建议卡片默认跳转页面路径
- `category` 当前复用 `studentActionTypes` 的子集
- `priority` 当前复用 `studentActionPriorities`

返回示例：

```json
{
  "studentId": 10001,
  "studentName": "张三",
  "suggestions": [
    {
      "category": "ACADEMIC",
      "title": "优先补齐学业短板",
      "description": "当前仍有培养方案学分未补齐，建议优先处理缺失课程并关注学业预警。",
      "priority": "HIGH",
      "actionLabel": "查看学业分析",
      "actionPath": "/student/academic-analysis"
    },
    {
      "category": "WORKLOG",
      "title": "沉淀已有成果材料",
      "description": "已累计登记 5 分工作量，建议同步整理证明、总结和展示材料。",
      "priority": "MEDIUM",
      "actionLabel": "查看工作记录",
      "actionPath": "/student/worklogs"
    },
    {
      "category": "PORTRAIT",
      "title": "围绕目标方向完善经历",
      "description": "当前职业倾向为升学，建议围绕该方向持续补充实践、竞赛或研究经历。",
      "priority": "LOW",
      "actionLabel": "查看学生画像",
      "actionPath": "/student/portrait"
    }
  ]
}
```

建议来源说明：
- `ACADEMIC`：来自学业分析结果与风险等级
- `WORKLOG`：来自学生工作记录条数与累计工作量
- `PORTRAIT`：来自成长画像中的 GPA、排名、荣誉、职业倾向
- `CERTIFICATE`：来自当前证明申请状态，主要关注 `PENDING`、`WITHDRAWN`

## 6. 学业分析模块

### 6.1 学业分析结果

- `GET /api/v1/academic/analysis/{studentId}`

返回字段：
- `studentId`
- `studentName`
- `major`
- `grade`
- `missingModules`
- `recommendedCourses`
- `summary`
- `riskSummary`
- `dataSourceNote`

`missingModules` 子项字段：
- `moduleName`
- `requiredCredits`
- `earnedCredits`
- `recommendedCourses`

`riskSummary` 字段：
- `level`
- `message`
- `needsManualReview`

## 6.2 学生工作记录

### 创建工作记录

- `POST /api/v1/worklogs`

权限说明：
- `SUPER_ADMIN`、`COLLEGE_ADMIN`、`COUNSELOR` 可全院登记
- `CLASS_ADVISOR` 仅可对自己负责范围内学生登记
- `LEAGUE_SECRETARY` 仅可对本年级学生登记

### 更新工作记录

- `PUT /api/v1/worklogs/{id}`

权限说明：
- `SUPER_ADMIN`、`COLLEGE_ADMIN`、`COUNSELOR` 可更新任意记录
- `CLASS_ADVISOR` 可更新自己负责范围内记录
- `LEAGUE_SECRETARY` 仅可更新自己创建且本年级范围内的记录

### 删除工作记录

- `DELETE /api/v1/worklogs/{id}`

权限说明同更新工作记录，并保留操作留痕。

请求体字段：
- `studentId`
- `studentName`
- `category`
- `title`
- `description`
- `workloadScore`
- `workDate`

### 查询学生工作记录

- `GET /api/v1/worklogs/student/{studentId}`

### 查询学生工作量汇总

- `GET /api/v1/worklogs/student/{studentId}/summary`

### 查询工作记录动作历史

- `GET /api/v1/worklogs/{id}/actions`

返回字段：
- `id`
- `workLogId`
- `operatorName`
- `operatorRole`
- `action`
- `detail`
- `operatedAt`

### 查询工作记录总览

- `GET /api/v1/worklogs/overview`

### 管理端筛选工作记录

- `GET /api/v1/worklogs/admin/filter`

可选参数：
- `studentId`
- `category`
- `recorderRole`
- `grade`
- `className`
- `startDate`
- `endDate`
- `sortBy`
- `sortDir`

### 管理端工作记录统计

- `GET /api/v1/worklogs/admin/stats`

可选参数：
- `studentId`
- `category`
- `recorderRole`
- `grade`
- `className`
- `startDate`
- `endDate`

返回字段：
- `totalEntries`
- `totalStudents`
- `totalWorkloadScore`
- `categoryStats`
- `topStudents`
- `monthStats`
- `dailyTrend`
- `recorderRoleStats`
- `scoreBandStats`
- `gradeStats`
- `classStats`

### 管理端工作记录分页明细

- `GET /api/v1/worklogs/admin/page`

可选参数：
- `studentId`
- `category`
- `recorderRole`
- `grade`
- `className`
- `startDate`
- `endDate`
- `page`
- `size`
- `sortBy`
- `sortDir`

### 管理端工作记录导出字段

- `GET /api/v1/worklogs/admin/export-metadata`

当前版本的工作记录管理能力已覆盖：
- 新增
- 修改
- 删除
- 审计历史
- 范围化统计与分页

排序白名单：
- `workDate`
- `workloadScore`
- `createdAt`
- `studentName`

## 7. 管理后台接口

当前管理后台接口矩阵：

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
- `GET /api/v1/admin/advisor-scopes`
- `GET /api/v1/admin/advisor-scopes/page`
- `GET /api/v1/admin/advisor-scopes/stats`
- `POST /api/v1/admin/advisor-scopes`
- `PUT /api/v1/admin/advisor-scopes/{id}`
- `DELETE /api/v1/admin/advisor-scopes/{id}`

### 7.1 通知管理

#### 查询通知列表

- `GET /api/v1/admin/notices`
- `GET /api/v1/admin/notices/page`
- `GET /api/v1/admin/notices/stats`

权限说明：
- `SUPER_ADMIN`、`COLLEGE_ADMIN`、`COUNSELOR`、`CLASS_ADVISOR` 可查看
- `LEAGUE_SECRETARY` 不进入管理端通知视图
- `CLASS_ADVISOR` 默认只查看“全体学生”及本年级相关通知

分页筛选参数：
- `keyword`
- `tag`
- `targetKeyword`
- `page`
- `size`

统计返回字段：
- `totalNotices`
- `taggedNotices`
- `targetedGradeNotices`
- `targetedGraduateNotices`

#### 创建通知

- `POST /api/v1/admin/notices`

请求体：

```json
{
  "title": "就业讲座报名通知",
  "summary": "面向计算机类学生开放报名",
  "tags": ["就业", "讲座"],
  "targetDescription": "2023级计算机类学生"
}
```

### 7.1.1 学生信息与学生画像

- `GET /api/v1/admin/students`
- `GET /api/v1/admin/students/page`
- `GET /api/v1/admin/students/stats`
- `GET /api/v1/admin/students/{studentId}`
- `GET /api/v1/admin/students/{studentId}/portrait`
- `PUT /api/v1/admin/students/{studentId}/portrait`
- `GET /api/v1/admin/students/portraits/page`
- `GET /api/v1/admin/students/portraits/stats`

范围说明：
- `CLASS_ADVISOR` 默认只看自己负责年级/班级/学生范围
- `LEAGUE_SECRETARY` 仅能读取公开画像
- 敏感字段按角色脱敏展示

画像统计返回重点字段：
- `totalPortraits`
- `publicVisibleCount`
- `averageGpa`
- `careerStats`
- `gpaBandStats`
- `gradeRankBandStats`

### 7.2 知识库管理

#### 查询知识条目

- `GET /api/v1/admin/knowledge`
- `GET /api/v1/admin/knowledge/page`
- `GET /api/v1/admin/knowledge/stats`

返回字段：
- `id`
- `title`
- `category`
- `published`
- `officialUrl`

分页筛选参数：
- `keyword`
- `category`
- `published`
- `page`
- `size`

统计返回字段：
- `totalItems`
- `publishedItems`
- `unpublishedItems`
- `totalAttachments`
- `categoryStats`

#### 创建知识条目

- `POST /api/v1/admin/knowledge`

#### 更新知识条目

- `PUT /api/v1/admin/knowledge/{id}`

#### 查询知识附件

- `GET /api/v1/admin/knowledge/{id}/attachments`

权限说明：
- 当前仅 `SUPER_ADMIN`、`COLLEGE_ADMIN`、`COUNSELOR`、`CLASS_ADVISOR` 可查看
- `LEAGUE_SECRETARY` 不可查看附件元数据

返回字段：
- `id`
- `knowledgeId`
- `fileName`
- `contentType`
- `fileSize`
- `storagePath`
- `uploadedBy`
- `uploadedAt`

#### 上传知识附件

- `POST /api/v1/admin/knowledge/{id}/attachments`

请求方式：
- `multipart/form-data`
- 参数名：`file`

规则：
- 单文件大小限制为 `30MB`

#### 删除知识附件

- `DELETE /api/v1/admin/knowledge/attachments/{attachmentId}`

请求体字段：
- `title`
- `category`
- `content`
- `officialUrl`
- `sourceFileName`
- `audienceScope`
- `updatedBy`
- `published`

### 7.3 审批管理

#### 查询审批任务

- `GET /api/v1/admin/approvals`
- `GET /api/v1/admin/approvals/page`
- `GET /api/v1/admin/approvals/stats`

权限说明：
- `SUPER_ADMIN`、`COLLEGE_ADMIN`、`COUNSELOR` 可查看全量审批任务
- `CLASS_ADVISOR` 默认只查看自己负责范围内学生的审批任务

返回字段：
- `requestId`
- `studentId`
- `studentName`
- `certificateType`
- `status`
- `reason`
- `submittedAt`

分页筛选参数：
- `studentId`
- `status`
- `certificateType`
- `keyword`
- `page`
- `size`

统计返回字段：
- `totalTasks`
- `pendingTasks`
- `counselorApprovedTasks`
- `approvedTasks`
- `rejectedTasks`
- `withdrawnTasks`

#### 执行审批动作

- `POST /api/v1/admin/approvals/{requestId}/action`

请求体：

```json
{
  "action": "approve",
  "comment": "材料齐全，审批通过"
}
```

支持动作：
- `approve`
- `reject`
- `withdraw`
- `resubmit`

当前 mock 状态流转示例：
- `PENDING -> COUNSELOR_APPROVED -> APPROVED`
- 任意阶段可进入 `REJECTED`
- 撤回后为 `WITHDRAWN`
- 重提后回到 `PENDING`

规则补充：
- 新申请默认带有约 2 天撤回窗口
- 只有 `WITHDRAWN` 状态的申请支持 `resubmit`

#### 查询审批历史

- `GET /api/v1/admin/approvals/{requestId}/history`

说明：
- `CLASS_ADVISOR` 仅可查看自己负责范围内学生的审批历史

返回字段：
- `id`
- `requestId`
- `operatorName`
- `operatorRole`
- `action`
- `fromStatus`
- `toStatus`
- `comment`
- `operatedAt`

### 7.4 管理员操作日志

- `GET /api/v1/admin/operation-logs`
- `GET /api/v1/admin/operation-logs/page`
- `GET /api/v1/admin/operation-logs/stats`

权限说明：
- 默认仅 `SUPER_ADMIN`、`COLLEGE_ADMIN` 可查看

返回字段：
- `id`
- `operatorName`
- `operatorRole`
- `module`
- `action`
- `target`
- `result`
- `operatedAt`

分页筛选参数：
- `module`
- `action`
- `operatorRole`
- `targetKeyword`
- `page`
- `size`

统计返回字段：
- `totalLogs`
- `successLogs`
- `failedLogs`
- `distinctModules`
- `distinctOperatorRoles`
- `moduleStats`
- `operatorRoleStats`

### 7.5 数据导入任务

- `GET /api/v1/admin/import-tasks`
- `GET /api/v1/admin/import-tasks/page`
- `GET /api/v1/admin/import-tasks/stats`

返回字段：
- `id`
- `taskType`
- `fileName`
- `status`
- `totalRows`
- `successRows`
- `failedRows`
- `owner`
- `createdAt`

分页筛选参数：
- `taskType`
- `status`
- `ownerKeyword`
- `page`
- `size`

导入任务统计返回字段：
- `totalTasks`
- `createdTasks`
- `runningTasks`
- `successTasks`
- `partialSuccessTasks`
- `failedTasks`
- `totalRows`
- `totalSuccessRows`
- `totalFailedRows`

#### 创建导入任务

- `POST /api/v1/admin/import-tasks`

请求体字段：
- `taskType`
- `fileName`
- `owner`
- `totalRows`

#### 更新导入任务状态

- `PUT /api/v1/admin/import-tasks/{id}`

请求体字段：
- `status`
- `successRows`
- `failedRows`
- `errorSummary`

#### 查询导入错误明细

- `GET /api/v1/admin/import-tasks/{id}/errors`
- `GET /api/v1/admin/import-tasks/{id}/errors/page`

#### 新增导入错误明细

- `POST /api/v1/admin/import-tasks/{id}/errors`

请求体字段：
- `rowNumber`
- `fieldName`
- `errorMessage`
- `rawValue`

### 7.6 学生信息管理

- `GET /api/v1/admin/students`
- `GET /api/v1/admin/students/page`
- `GET /api/v1/admin/students/stats`
- `GET /api/v1/admin/students/{id}`
- `POST /api/v1/admin/students`
- `PUT /api/v1/admin/students/{id}`
- `GET /api/v1/admin/students/{id}/portrait`
- `PUT /api/v1/admin/students/{id}/portrait`
- `GET /api/v1/admin/students/portraits/page`
- `GET /api/v1/admin/students/portraits/stats`

说明：
- 返回值中对敏感字段采用脱敏形式展示
- 当前保留了毕业、休学、转专业等状态字段
- 已增加学生画像字段结构，包括性别、民族、荣誉、奖学金、竞赛、社会实践、志愿服务、科研经历、日常表现、绩点、学分、排名、发展方向、备注等
- 学生画像已增加元数据字段，包括 `updatedBy`、`dataSource`、`publicVisible`
- 学生档案保留 `advisorScope` 兼容字段，同时后端已增加 `advisor_scope_binding` 负责范围结构

学生信息分页筛选参数：
- `grade`
- `className`
- `status`
- `keyword`
- `page`
- `size`

学生信息统计返回字段：
- `totalStudents`
- `activeStudents`
- `graduatedStudents`
- `suspendedStudents`
- `statusUnknownStudents`

学生画像分页筛选参数：
- `grade`
- `className`
- `publicVisible`
- `careerOrientation`
- `minGpa`
- `page`
- `size`

学生画像统计返回字段：
- `totalPortraits`
- `publicVisibleCount`
- `averageGpa`
- `careerStats`

### 7.7 班主任负责范围管理

- `GET /api/v1/admin/advisor-scopes`
- `GET /api/v1/admin/advisor-scopes/page`
- `GET /api/v1/admin/advisor-scopes/stats`
- `POST /api/v1/admin/advisor-scopes`
- `PUT /api/v1/admin/advisor-scopes/{id}`
- `DELETE /api/v1/admin/advisor-scopes/{id}`

说明：
- 支持按 `advisorUsername`、`grade`、`className` 筛选
- `page` 接口返回分页结构

说明：
- `GET /api/v1/admin/advisor-scopes` 支持按 `advisorUsername`、`grade`、`className` 筛选

请求体字段：
- `advisorUsername`
- `advisorName`
- `grade`
- `className`
- `studentId`

统计返回字段：
- `totalBindings`
- `totalAdvisors`
- `totalGrades`
- `totalClasses`
- `advisorStats`

### 8. 学生自服务接口

- `GET /api/v1/student/me`

说明：
- 返回当前登录学生本人的基础档案与脱敏后的敏感字段
- 不返回其他学生的信息

## 权限说明

- `SUPER_ADMIN`、`COLLEGE_ADMIN`、`COUNSELOR`、`CLASS_ADVISOR` 可访问大部分管理查询接口
- `LEAGUE_SECRETARY` 当前可读取知识库管理列表，但不能新增或修改知识条目
- 学生端只能访问自己的数据与学生侧业务接口
- `CLASS_ADVISOR` 查询学生信息时默认仅能看到自己负责年级且命中 `advisorScope` 的学生

说明：
- `action` 目前支持：`approve`、`reject`

### 7.4 统计概览

- `GET /api/v1/admin/stats`

返回字段：
- `totalStudents`
- `pendingApprovals`
- `publishedNotices`
- `knowledgeDocuments`

## 8. 联调建议

人员 1 优先联调：
- `/api/v1/auth/*`
- `/api/v1/knowledge/*`
- `/api/v1/party-progress/*`
- `/api/v1/notices/student/{studentId}`
- `/api/v1/certificates/requests*`
- `/api/v1/academic/analysis/{studentId}`

人员 3 优先联调：
- `/api/v1/admin/notices`
- `/api/v1/admin/knowledge`
- `/api/v1/admin/approvals`
- `/api/v1/admin/stats`

## 9. 后续替换点

后续接入真实版本时，建议替换以下模块：

- `MockAuthService`
- `MockKnowledgeService`
- `MockPartyProgressService`
- `MockNoticeService`
- `MockCertificateService`
- `MockAcademicWarningService`
- `MockAdminService`
