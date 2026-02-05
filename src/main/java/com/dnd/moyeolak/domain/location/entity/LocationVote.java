package com.dnd.moyeolak.domain.location.entity;

import com.dnd.moyeolak.domain.participant.entity.Participant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationVoteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_poll_id", nullable = false)
    private LocationPoll locationPoll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @Column(comment = "출발지 등록자 이름. 참가자 없이 수동 입력된 출발지일 경우 사용")
    private String departureName;

    @Column(comment = "출발지 주소", nullable = false)
    private String departureLocation;

    @Column(comment = "출발지 위도", nullable = false, precision = 10, scale = 7)
    private BigDecimal departureLat;

    @Column(comment = "출발지 경도", nullable = false, precision = 10, scale = 7)
    private BigDecimal departureLng;

    public static LocationVote of(LocationPoll locationPoll, Participant participant,
                                  String departureLocation, BigDecimal departureLat, BigDecimal departureLng) {
        return LocationVote.builder()
                .locationPoll(locationPoll)
                .participant(participant)
                .departureLocation(departureLocation)
                .departureLat(departureLat)
                .departureLng(departureLng)
                .build();
    }
}
