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
@Table(
        name = "participant",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_participant_meeting_local_storage_key",
                columnNames = {"meeting_id", "local_storage_key"}
        )
)
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

    @Column(name = "local_storage_key", comment = "고유키")
    private String localStorageKey;

    @Column(comment = "이름", nullable = false)
    private String name;

    @Column(name = "is_host", nullable = false)
    private boolean host;

    @Builder.Default
    @BatchSize(size = 15)
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleVote> scheduleVotes = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 15)
    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocationVote> locationVotes = new ArrayList<>();

    public void updateName(String name) {
        this.name = name;
    }

    private static Participant createParticipant(Meeting meeting, String localStorageKey, String name, boolean host) {
        return Participant.builder()
                .meeting(meeting)
                .localStorageKey(localStorageKey)
                .name(name)
                .host(host)
                .build();
    }

    public static Participant of(Meeting meeting, String localStorageKey, String name) {
        return createParticipant(meeting, localStorageKey, name, false);
    }

    public static Participant hostOf(Meeting meeting, String localStorageKey, String name) {
        return createParticipant(meeting, localStorageKey, name, true);
    }

    public static Participant of(Meeting meeting, String localStorageKey, String name, ScheduleVote scheduleVote) {
        Participant participant = Participant.of(meeting, localStorageKey, name);
        participant.addScheduleVote(scheduleVote);
        return participant;
    }

    public static Participant of(Meeting meeting, String localStorageKey, String name, LocationVote locationVote) {
        Participant participant = Participant.of(meeting, localStorageKey, name);
        participant.addLocationVote(locationVote);
        return participant;
    }

    private void addScheduleVote(ScheduleVote scheduleVote) {
        this.scheduleVotes.add(scheduleVote);
        scheduleVote.assignParticipant(this);
    }

    private void addLocationVote(LocationVote locationVote) {
        this.locationVotes.add(locationVote);
        locationVote.assignParticipant(this);
    }
}
