package com.dnd.moyeolak.domain.participant.entity;

import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(comment = "고유키")
    private String localStorageKey;

    @Column(comment = "이름", nullable = false)
    private String name;

    @Builder.Default
    @BatchSize(size = 15)
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleVote> scheduleVotes = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 15)
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocationVote> locationVotes = new ArrayList<>();

    public static Participant of(Meeting meeting, String localStorageKey, String name) {
        return Participant.builder()
                .meeting(meeting)
                .localStorageKey(localStorageKey)
                .name(name)
                .build();
    }

    public static Participant of(Meeting meeting, String localStorageKey, String name, ScheduleVote scheduleVote) {
        Participant participant = Participant.of(meeting, localStorageKey, name);
        participant.getScheduleVotes().add(scheduleVote);
        return participant;
    }

    public static Participant of(Meeting meeting, String localStorageKey, String name, LocationVote locationVote) {
        Participant participant = Participant.of(meeting, localStorageKey, name);
        participant.getLocationVotes().add(locationVote);
        return participant;
    }
}
