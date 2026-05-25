package edu.ruc.platform.honor.repository;

import edu.ruc.platform.honor.domain.HonorRecipientMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HonorRecipientMemberRepository extends JpaRepository<HonorRecipientMember, Long> {

    List<HonorRecipientMember> findByRecipientIdOrderByDisplayOrderAscCreatedAtAsc(Long recipientId);

    List<HonorRecipientMember> findByRecipientIdInOrderByDisplayOrderAscCreatedAtAsc(Collection<Long> recipientIds);

    void deleteByRecipientId(Long recipientId);
}
