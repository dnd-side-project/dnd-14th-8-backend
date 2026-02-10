package com.dnd.moyeolak.domain.schedule.entity;

import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.global.converter.LocalDateTimeListConverter;
import com.dnd.moyeolak.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleVoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_poll_id", nullable = false)
    private SchedulePoll schedulePoll;

    @Builder.Default
    @Convert(converter = LocalDateTimeListConverter.class)
    @Column(columnDefinition = "TEXT", comment = "투표한 날짜", nullable = false)
    private List<LocalDateTime> votedDate = new ArrayList<>();

    public static ScheduleVote of(Participant participant, SchedulePoll schedulePoll, LocalDateTime votedDate) {
        return ScheduleVote.builder()
                .participant(participant)
                .schedulePoll(schedulePoll)
                //.votedDate(votedDate) TODO : 추후 수정
                .build();
    }

    public static ScheduleVote of(Participant participant, SchedulePoll schedulePoll, List<LocalDateTime> localDateTimes) {
        return ScheduleVote.builder()
                .participant(participant)
                .schedulePoll(schedulePoll)
                .votedDate(localDateTimes)
                .build();
    }

    public void updateDateTimeOption(List<LocalDateTime> newDateTimes) {
        this.votedDate.clear();
        this.votedDate.addAll(newDateTimes);
    }
}
