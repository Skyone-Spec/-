package edu.ruc.platform.common.mock;

import edu.ruc.platform.academic.dto.AcademicWarningResponse;
import edu.ruc.platform.auth.dto.LoginResponse;
import edu.ruc.platform.auth.dto.UserProfileResponse;
import edu.ruc.platform.knowledge.dto.KnowledgeSearchResponse;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.party.dto.PartyProgressResponse;
import edu.ruc.platform.party.dto.PartyStageTimelineResponse;
import edu.ruc.platform.party.dto.ReminderResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Profile("mock")
public class MockDataStore {

    private final Map<String, UserProfileResponse> users = Map.of(
            "2023100001", new UserProfileResponse(10001L, 10001L, "2023100001", "STUDENT", "2023100001", "张三", "计算机类", "2023级"),
            "2023100002", new UserProfileResponse(10002L, 10002L, "2023100002", "LEAGUE_SECRETARY", "2023100002", "李四", "计算机类", "2023级"),
            "teacher01", new UserProfileResponse(20001L, null, "teacher01", "COUNSELOR", null, "胡浩老师", null, null),
            "advisor01", new UserProfileResponse(20002L, null, "advisor01", "CLASS_ADVISOR", null, "王老师", null, "2023级"),
            "admin", new UserProfileResponse(1L, null, "admin", "SUPER_ADMIN", null, "系统管理员", null, null)
    );

    public LoginResponse adminLogin() {
        return new LoginResponse(1L, "admin", "SUPER_ADMIN", "mock-jwt-token-admin", false);
    }

    public LoginResponse studentLogin() {
        return new LoginResponse(10001L, "2023100001", "STUDENT", "mock-jwt-token-student", false);
    }

    public UserProfileResponse currentUser() {
        return users.get("2023100001");
    }

    public UserProfileResponse user(String username) {
        return users.get(username);
    }

    public List<KnowledgeSearchResponse> knowledgeDocuments() {
        return List.of(
                new KnowledgeSearchResponse(1L, "奖学金评选与常见问题", "奖助学金", "https://example.edu/scholarship", "适合回答名额、资格、申请节点等标准问题；特殊情况请联系负责老师。", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(2L, "入党全流程说明", "党团事务", "https://example.edu/party", "入党流程稳定，可按申请人、积极分子、发展对象、预备党员、正式党员阶段查询。", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(3L, "入团流程与材料要求", "党团事务", "https://example.edu/league", "入团流程适合做固定步骤说明和节点提醒。", "STANDARD_ANSWER", false),
                new KnowledgeSearchResponse(4L, "学生信息字段公开与保密原则", "数据安全", "https://example.edu/security", "该主题涉及隐私和敏感字段，前台仅提供公开口径与官方链接，具体数据不直接展示。", "OFFICIAL_LINK_ONLY", true),
                new KnowledgeSearchResponse(5L, "电子证明与常用表格入口", "日常事务", "https://example.edu/forms", "可统一提供在读证明、请假条、活动预算表等模板下载和流程说明。", "STANDARD_ANSWER", false)
        );
    }

    public PartyProgressResponse partyProgress() {
        return new PartyProgressResponse(
                "积极分子",
                LocalDate.of(2026, 2, 15),
                39,
                LocalDate.of(2026, 5, 15),
                "已提交入党申请书；已参加党课学习小组；已完成基础培训",
                "满培养期后进入发展对象推优准备",
                "积极分子阶段满 3 个月提交思想汇报并准备发展对象材料"
        );
    }

    public PartyStageTimelineResponse partyTimeline() {
        return new PartyStageTimelineResponse(
                10001L,
                "积极分子",
                List.of(
                        new PartyStageTimelineResponse.StageNode("入党申请人", true, false, LocalDate.of(2025, 11, 10), LocalDate.of(2025, 12, 10), 30, "提交申请书后完成基础登记", "提交申请书并完成基础登记"),
                        new PartyStageTimelineResponse.StageNode("党课学习小组", true, false, LocalDate.of(2026, 1, 6), LocalDate.of(2026, 2, 5), 30, "完成党课学习与考核", "完成党课学习小组课程"),
                        new PartyStageTimelineResponse.StageNode("积极分子", true, true, LocalDate.of(2026, 2, 15), LocalDate.of(2026, 5, 15), 90, "培养期满 3 个月提交思想汇报", "完成培训并进入培养阶段"),
                        new PartyStageTimelineResponse.StageNode("发展对象", false, false, null, LocalDate.of(2026, 8, 28), 90, "按学期节点完成推优与答辩", "满足培养期后按学期节点推进"),
                        new PartyStageTimelineResponse.StageNode("预备党员", false, false, null, null, 180, "支部审批通过后进入预备期", "等待支部审批"),
                        new PartyStageTimelineResponse.StageNode("正式党员", false, false, null, null, 365, "预备期满后转正", "转正完成")
                )
        );
    }

    public List<ReminderResponse> reminders() {
        return List.of(
                new ReminderResponse("思想汇报提醒", "积极分子培养期内按要求提交思想汇报，具体时间以学院通知为准。", LocalDate.of(2026, 5, 15), "HIGH", "积极分子", "培养期满 3 个月提交思想汇报", 51, false),
                new ReminderResponse("发展对象准备提醒", "下学期培养期满后，关注发展对象推优和答辩安排。", LocalDate.of(2026, 8, 28), "MEDIUM", "发展对象", "按学期节点完成推优与答辩", 156, false)
        );
    }

    public List<TargetedNoticeResponse> notices() {
        return List.of(
                new TargetedNoticeResponse(1L, "先锋奖章与奖学金咨询入口", "统一解答先锋奖章、吴玉章奖学金、优秀毕业生等标准问题。", List.of("奖助学金", "知识库"), "全体学生", "MEDIUM", List.of("全体学生"), List.of("IN_APP"), LocalDateTime.of(2026, 3, 20, 9, 0)),
                new TargetedNoticeResponse(2L, "入党入团流程说明更新", "已补充固定流程、时间线和常见问题入口。", List.of("党团事务", "流程"), "全体学生", "HIGH", List.of("全体学生", "党团事务标签"), List.of("IN_APP", "EMAIL"), LocalDateTime.of(2026, 3, 21, 11, 0)),
                new TargetedNoticeResponse(3L, "2023级计算机类就业信息汇总", "面向 2023 级计算机类学生的就业与实习通知。", List.of("就业", "实习", "计算机类"), "2023级/计算机类", "HIGH", List.of("年级匹配", "专业匹配", "就业标签"), List.of("IN_APP", "EMAIL", "WECHAT"), LocalDateTime.of(2026, 3, 22, 10, 30))
        );
    }

    public List<AcademicWarningResponse> academicWarnings() {
        return List.of(
                new AcademicWarningResponse("专业核心课", 18, 12, 6, 67, "数据结构、操作系统"),
                new AcademicWarningResponse("通识选修", 8, 4, 4, 50, "艺术鉴赏、社会研究方法")
        );
    }
}
