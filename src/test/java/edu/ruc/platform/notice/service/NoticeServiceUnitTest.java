package edu.ruc.platform.notice.service;

import edu.ruc.platform.notice.domain.Notice;
import edu.ruc.platform.notice.dto.TargetedNoticeResponse;
import edu.ruc.platform.notice.repository.NoticeRepository;
import edu.ruc.platform.student.domain.StudentProfile;
import edu.ruc.platform.student.repository.StudentProfileRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoticeServiceUnitTest {

    @Test
    void nonMockNoticeTagsAreSplitLikeMockContract() {
        NoticeRepository noticeRepository = mock(NoticeRepository.class);
        StudentProfileRepository studentProfileRepository = mock(StudentProfileRepository.class);
        NoticeService service = new NoticeService(noticeRepository, studentProfileRepository);

        StudentProfile profile = new StudentProfile();
        profile.setId(10001L);
        profile.setGrade("2023级");
        profile.setMajor("计算机类");

        Notice notice = new Notice();
        notice.setId(1L);
        notice.setTitle("就业与流程提醒");
        notice.setSummary("用于测试标签拆分");
        notice.setTag("就业, 实习, 流程");
        notice.setTargetGrade("2023级");
        notice.setTargetMajor("计算机类");
        notice.setPublishTime(LocalDateTime.now());

        when(studentProfileRepository.findById(10001L)).thenReturn(Optional.of(profile));
        when(noticeRepository.findAllByOrderByPublishTimeDesc()).thenReturn(List.of(notice));

        List<TargetedNoticeResponse> responses = service.listTargetedNotices(10001L);

        assertEquals(1, responses.size());
        assertEquals(List.of("就业", "实习", "流程"), responses.get(0).tags());
        assertTrue(responses.get(0).matchedRules().contains("年级匹配"));
    }
}
