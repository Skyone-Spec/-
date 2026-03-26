package edu.ruc.platform.notice.service;

import edu.ruc.platform.notice.dto.TargetedNoticeResponse;

import java.util.List;

public interface NoticeApplicationService {

    List<TargetedNoticeResponse> listTargetedNotices(Long studentId);
}
