package com.dnd.moyeolak.domain.schedule.entity;

import com.dnd.moyeolak.domain.participant.entity.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleVoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_poll_id", nullable = false)
    private SchedulePoll schedulePoll;

    @Column(columnDefinition = "DATETIME(0)", comment = "투표한 날짜", nullable = false)
    private LocalDateTime votedDate;

    public static ScheduleVote of(Participant participant, SchedulePoll schedulePoll, LocalDateTime votedDate) {
        return ScheduleVote.builder()
                .participant(participant)
                .schedulePoll(schedulePoll)
                .votedDate(votedDate)
                .build();
    }
}
