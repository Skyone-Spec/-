package edu.ruc.platform.honor.repository;

import edu.ruc.platform.honor.domain.HonorRecipientAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HonorRecipientAttachmentRepository extends JpaRepository<HonorRecipientAttachment, Long> {

    List<HonorRecipientAttachment> findByRecipientIdOrderByDisplayOrderAscCreatedAtAsc(Long recipientId);

    List<HonorRecipientAttachment> findByRecipientIdInOrderByDisplayOrderAscCreatedAtAsc(Collection<Long> recipientIds);

    void deleteByRecipientId(Long recipientId);
}
