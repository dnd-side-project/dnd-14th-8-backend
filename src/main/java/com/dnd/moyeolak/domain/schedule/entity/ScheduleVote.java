package com.dnd.moyeolak.domain.schedule.entity;

import com.dnd.moyeolak.domain.participant.entity.Participant;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
public class ScheduleVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_poll_id", nullable = false)
    private SchedulePoll schedulePoll;

    @Column(columnDefinition = "DATETIME(0)", comment = "투표한 날짜", nullable = false)
    private LocalDateTime votedDate;
}
