package edu.ruc.platform.party.domain;

import edu.ruc.platform.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "party_reminder_task")
public class LatestPartyReminderTask extends BaseEntity {

    @Column(nullable = false)
    private Long progressId;

    @Column(nullable = false)
    private Long nodeId;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(nullable = false, length = 32)
    private String status;

    @Column
    private LocalDateTime sentAt;
}
