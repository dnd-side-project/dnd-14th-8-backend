package com.dnd.moyeolak.domain.location.entity;

import com.dnd.moyeolak.domain.participant.entity.Participant;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class LocationVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationVoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_poll_id", nullable = false)
    private LocationPoll locationPoll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(comment = "출발지 주소", nullable = false)
    private String departureLocation;

    @Column(comment = "출발지 위도", nullable = false)
    private String departureLat;

    @Column(comment = "출발지 경도", nullable = false)
    private String departureLng;
}
