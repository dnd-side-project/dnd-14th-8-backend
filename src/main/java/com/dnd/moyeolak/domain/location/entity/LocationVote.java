package com.dnd.moyeolak.domain.location.entity;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.meeting.dto.UpdateLocationVoteRequest;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_vote_id")
    private Long id;

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

    public void assignParticipant(Participant participant) {
        this.participant = participant;
    }

    public void update(UpdateLocationVoteRequest request) {
        this.departureName = request.participantName();
        this.departureLocation = request.departureLocation();
        this.departureLat = new BigDecimal(request.departureLat());
        this.departureLng = new BigDecimal(request.departureLng());
    }

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

    /**
     * Participant 없이 수동으로 출발지 입력하는 경우
     */
    public static LocationVote of(LocationPoll locationPoll, String departureName,
                                  String departureLocation, BigDecimal departureLat, BigDecimal departureLng) {
        return LocationVote.builder()
                .locationPoll(locationPoll)
                .departureName(departureName)
                .departureLocation(departureLocation)
                .departureLat(departureLat)
                .departureLng(departureLng)
                .build();
    }

    public static LocationVote fromByCreateLocationVoteRequest(LocationPoll locationPoll, CreateLocationVoteRequest request) {
        return LocationVote.builder()
                .locationPoll(locationPoll)
                .departureName(request.participantName())
                .departureLocation(request.departureLocation())
                .departureLat(new BigDecimal(request.departureLat()))
                .departureLng(new BigDecimal(request.departureLng()))
                .build();
    }
}
