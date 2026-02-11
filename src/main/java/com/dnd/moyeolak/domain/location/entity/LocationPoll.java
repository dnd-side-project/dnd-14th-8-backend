package com.dnd.moyeolak.domain.location.entity;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.global.entity.BaseEntity;
import com.dnd.moyeolak.global.enums.PollStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationPoll extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationPollId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Builder.Default
    @Column(comment = "투표 상태", nullable = false)
    @Enumerated(EnumType.STRING)
    private PollStatus pollStatus = PollStatus.INACTIVE;

    @Builder.Default
    @OneToMany(mappedBy = "locationPoll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocationVote> locationVotes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "locationPoll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConfirmedLocation> confirmedLocations = new ArrayList<>();

    public static LocationPoll defaultOf(Meeting meeting) {
        return LocationPoll.builder()
                .meeting(meeting)
                .build();
    }

    public static LocationPoll ofId(Long locationPollId) {
        return LocationPoll.builder()
                .locationPollId(locationPollId)
                .build();
    }

}
