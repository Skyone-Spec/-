# 学院学生综合服务与党团管理平台 - 项目全景解读 (Interpretation.md)

你好！作为具有 Java 基础但初次接触全栈项目的开发者，这份文档专为你量身定制。由于本项目规模庞大（包含数百个后端 Java 文件和前端小程序文件），如果把每个文件的每一行代码都铺开，这会是一本几千页的书，不仅难以阅读也抓不住重点。

因此，本文档采用**“提纲挈领 + 核心范式逐段剖析”**的方式。只要你理解了后文提到的“文件夹分层逻辑”和“标准代码模板”，你就能像读懂一个文件一样，看懂项目中的所有文件。

---

## 一、 项目概览与当前进度

### 1. 项目背景
本项目是一个**“学院学生综合服务与党团管理平台”**。旨在为高校（如中国人民大学，代码包名为 `edu.ruc`）提供学生档案管理、辅导员工作日志、党团流程跟踪、政策知识库查询等功能。

### 2. 当前开发进度与联调状态
- **整体进度**：系统基础架构已搭建完毕。后端已按领域驱动设计（DDD）划分了多个模块；前端（微信小程序）已完成核心页面的 UI 编写和路由跳转。
- **前后端联调状态**：**已实现初步联调**。
  - 在前端 `student-mini/app.js` 中，可以看到 `USE_MOCK: false` 以及 `baseUrl: 'https://camping-penny-concert.ngrok-free.dev/api/v1'`。这表明前端已经准备好通过内网穿透工具连接本地后端真实接口。
  - 前端 `api/request.js` 中封装了完整的 HTTP 请求逻辑，并处理了 Token 鉴权（JWT）和 401 登录失效拦截。
  - 后端实现了大量的 `MockService` 和部分真实的基于数据库的 `Service`（如 `KingbaseStudentProfileService`），处于逐步从假数据（Mock）向真数据库迁移的阶段。

---

## 二、 项目使用的工具与技术栈

### 1. 后端技术栈 (Java)
- **核心框架**：`Java 17` + `Spring Boot 3.3.6`。
- **数据库与持久层**：
  - `PostgreSQL` / `Kingbase` (人大金仓，国产库适配)
  - `Spring Data JPA` (ORM框架，用 Java 对象操作数据库)
  - `Flyway` (数据库版本控制，用于管理 `V1__xxx.sql` 建表脚本)
  - `H2 Database` (用于本地测试的内存数据库)
- **安全与鉴权**：
  - `Spring Security` (处理权限拦截)
  - `JJWT` (生成和解析 JWT Token，实现无状态登录)
- **实用工具**：
  - `Lombok` (通过注解自动生成 Getter/Setter/构造函数，减少样板代码)
  - `MapStruct` (优雅地实现实体类 Entity 和 DTO 之间的数据拷贝转换)
  - `SpringDoc OpenAPI` (自动生成 Swagger 接口文档)
  - `Apache POI` (处理 Excel 导入导出)
  - `Apache PDFBox` (处理 PDF 文件)

### 2. 前端技术栈 (微信小程序)
- **核心框架**：**原生微信小程序开发**（没有使用 Vue/Taro/UniApp 等跨端框架）。
- **文件结构**：采用标准的四件套：`.wxml`（结构）、`.wxss`（样式）、`.js`（逻辑）、`.json`（页面配置）。

---

## 三、 详细目录结构解读 (文件夹表示什么)

项目主要分为两大部分：根目录下的后端代码 和 `student-mini` 目录下的前端代码。

### 1. 后端目录结构 (`/src`)
后端代码位于 `src/main/java/edu/ruc/platform/`，采用了**按业务领域分包**的架构。

*   **`admin/`**: 后台管理模块（处理超级管理员相关的接口）。
*   **`auth/`**: 认证与授权模块（登录、Token 签发、权限校验）。
*   **`certificate/`**: 证明开具模块（学生申请在读证明等功能）。
*   **`common/`**: 公共模块（全局异常处理、通用配置、基础 API 响应封装 `ApiResponse`）。
*   **`knowledge/`**: 知识库模块（政策问答、FAQ 数据管理）。
*   **`notice/`**: 通知中心模块（发送系统通知、站内信）。
*   **`party/`**: **党建流程模块**（核心业务之一，管理入党积极分子、发展对象的流程进度、提醒任务）。
*   **`platform/`**: 平台基础模块（文件上传记录、系统设置、角色权限策略）。
*   **`student/`**: **学生画像模块**（核心业务之二，包含学生基础信息 `StudentProfile`、多维画像 `StudentPortrait`、学籍异动等）。
*   **`worklog/`**: **辅导员工作日志模块**（核心业务之三，辅导员记录与学生谈话的日志）。

**在每个业务模块内部，通常有以下 5 个标准子文件夹（以 `student` 为例）：**
1.  **`controller/`**: 控制器层。直接面向前端，定义 API 接口路径（如 `@GetMapping("/me")`），接收 HTTP 请求并返回结果。
2.  **`service/`**: 业务逻辑层。包含接口（Interface）和实现类（Impl），处理如“计算学生绩点排名”、“校验权限”等复杂逻辑。
3.  **`repository/`**: 数据访问层。继承 `JpaRepository`，负责直接与数据库交互（增删改查）。
4.  **`domain/` (或 `entity/`)**: 实体类。与数据库表一一对应的 Java 类。
5.  **`dto/`**: 数据传输对象 (Data Transfer Object)。专门用于前后端交互的数据结构（如 `xxxRequest` 表示前端传来的参数，`xxxResponse` 表示返回给前端的数据）。

### 2. 后端资源与配置 (`/src/main/resources`)
*   **`application.yml` 等**: Spring Boot 的核心配置文件，定义了数据库连接、服务器端口 (通常是 8080) 等。
*   **`db/migration/`**: 存放 Flyway 数据库脚本。如 `V1__init_schema.sql` 是第一次建表，`V2__seed_demo_data.sql` 是插入初始测试数据。系统启动时会自动执行这些 SQL。

### 3. 前端目录结构 (`/student-mini`)
*   **`api/`**: 封装所有与后端交互的网络请求。如 `party.js` 专门请求党建接口，`request.js` 是底层拦截器。
*   **`pages/`**: 小程序的主包页面（底部导航栏所在的页面）。
    *   `index/`: 首页（Dashboard）。
    *   `message/`: 消息通知页。
    *   `profile/`: 我的/个人中心页。
*   **`sub-pages/`**: 小程序的分包页面（为了减少首屏加载时间，具体功能放在子包）。
    *   `login/`: 登录页。
    *   `party/`: 党建进度详情页。
    *   `academic/`: 学业分析页。
    *   `feedback/`: 意见反馈页等。
*   **`static/`**: 存放本地静态资源，如 `icons/` (底部导航图标)、`images/` (占位图/Banner)。
*   **`utils/`**: 工具类。如 `date.js` (日期格式化)、`storage.js` (本地缓存操作)。
*   **`app.js` / `app.json` / `app.wxss`**: 小程序的全局入口文件。

---

## 四、 核心文件与代码段逐段剖析

因为同类文件写法高度一致，掌握以下几个核心文件的代码段，你就掌握了整个项目。

### 1. 后端 Controller 范式解读
以 `StudentSelfController.java` 为例，这是学生个人端的入口。

```java
package edu.ruc.platform.student.controller; // 声明包名

// 引入相关的 DTO 和 Service 依赖
import edu.ruc.platform.common.api.ApiResponse;
import edu.ruc.platform.student.dto.StudentProfileResponse;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 告诉 Spring 这是一个 API 接口类，返回 JSON 格式数据
@RequestMapping("/api/v1/student") // 定义该类下所有接口的统一前缀
@RequiredArgsConstructor // Lombok 注解：自动为 final 属性生成构造函数，用于依赖注入（代替 @Autowired）
public class StudentSelfController {

    // 注入业务服务类，final 保证不可变
    private final StudentProfileApplicationService studentProfileService;

    @GetMapping("/me") // 定义一个 HTTP GET 接口，完整路径为 GET /api/v1/student/me
    public ApiResponse<StudentProfileResponse> me() {
        // ApiResponse 是全局统一的返回格式（包含 code, message, data）
        // 调用 service 层获取当前登录学生的档案信息，并包装返回
        return ApiResponse.success(studentProfileService.currentStudentProfile());
    }
}
```
**解读**：前端如果想获取当前学生信息，就会发请求到 `GET /api/v1/student/me`，Controller 收到请求后，什么逻辑都不写，直接丢给 `Service` 层处理，然后把结果包在一个统一的 `ApiResponse` 里返回。

### 2. 后端 Domain / Entity 范式解读
以 `StudentWorkLog.java` (工作日志实体) 为例：

```java
@Entity // 告诉 JPA 这是一个数据库实体类
@Table(name = "student_work_log") // 映射到数据库的 student_work_log 表
@Data // Lombok 注解：自动生成所有字段的 getter/setter
public class StudentWorkLog {
    
    @Id // 标明主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增策略
    private Long id;

    @Column(name = "student_id", nullable = false) // 对应数据库列名，且不允许为空
    private String studentId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 谈话/日志内容

    // ... 其他字段
}
```

### 3. 前端网络请求底层 `request.js` 解读
位于 `student-mini/api/request.js`。

```javascript
const request = (options) => {
  return new Promise((resolve, reject) => {
    // 解析传入的参数：url, 请求方法(默认GET), 数据
    const { url, method = 'GET', data, header = {} } = options
    
    // 从全局 app.js 获取基础 URL 和 Token
    let app = getApp()
    
    wx.request({ // 调用微信原生网络请求 API
      url: app.globalData.baseUrl + url, 
      method,
      data: requestData,
      header: {
        'Content-Type': 'application/json',
        // 在请求头中携带 Token 证明身份。这是前后端鉴权的核心！
        'Authorization': app.globalData.token ? `Bearer ${app.globalData.token}` : '',
        ...header
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.success) {
          resolve(res.data) // 请求成功，返回数据
        } else if (res.statusCode === 401) {
          // 401 状态码表示 Token 过期或未登录
          app.clearLoginData() // 清除本地缓存
          wx.redirectTo({ url: '/sub-pages/login/index' }) // 强制跳回登录页
          reject(res.data)
        }
      }
      // ... 错误处理省略
    })
  })
}
```
**解读**：这是前端的“咽喉”。所有页面请求数据都会走这里。它负责自动加上域名、自动带上身份 Token，并在发现后端返回 401 (未授权) 时，自动踢回登录页。

### 4. 前端页面 JS 范式解读
以一个典型的页面 JS 为例（如展示党建进度）：

```javascript
import { getPartyProgress } from '../../api/party.js' // 引入接口定义

Page({
  data: { // 绑定的页面数据，wxml 会读取这里的值
    progressData: null,
    loading: true
  },

  onLoad(options) { // 生命周期钩子：页面加载时触发
    this.fetchData() // 加载数据
  },

  async fetchData() { // 异步获取数据
    try {
      // 发送网络请求，等待后端返回
      const res = await getPartyProgress() 
      if (res.success) {
        this.setData({ // 使用 setData 更新视图
          progressData: res.data,
          loading: false
        })
      }
    } catch (e) {
      console.error(e)
    }
  }
})
```

---

## 五、 如何阅读需求与运行本项目

### 1. 了解业务逻辑的捷径
如果你想知道为什么要写某些代码，强烈建议阅读 `/需求整合/后端建议与实施路线-Web优先.md`。这份文档详细记载了甲方（学校老师）的需求：为什么要做知识库、审批流、权限划分，这是代码架构（DDD包划分）的直接来源。

### 2. 本地开发与运行步骤
1. **启动后端**：
   - 确保安装了 Java 17 和 PostgreSQL（或开启 H2 内存模式）。
   - 在 IDE (IntelliJ IDEA) 中打开根目录，等待 Maven 下载完 `pom.xml` 中的依赖。
   - 运行 `edu.ruc.platform.StudentServicePlatformApplication` 类的 `main` 方法启动后端（默认运行在 8080 端口）。
2. **启动前端**：
   - 下载并安装**微信开发者工具**。
   - 导入 `student-mini` 文件夹作为一个小程序项目。
   - 在 `student-mini/app.js` 中，把 `baseUrl` 修改为你的本地地址（如 `http://localhost:8080/api/v1`），确保勾选微信开发者工具右上角的“不校验合法域名”。
   - 编译运行即可在模拟器中看到界面。

### 结语
这个项目的代码规范非常标准，是经典的 Spring Boot 企业级开发架构。你不用被文件的数量吓倒，**紧抓 Controller（接请求）-> Service（写逻辑）-> Repository（查数据库）这条主线**，对照着前端小程序的页面展示，遇到问题就去对应的包下找代码，很快就能得心应手！