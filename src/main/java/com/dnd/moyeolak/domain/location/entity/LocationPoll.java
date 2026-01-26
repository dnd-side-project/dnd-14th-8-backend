package com.dnd.moyeolak.domain.location.entity;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.global.entity.BaseEntity;
import com.dnd.moyeolak.global.enums.PollStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
public class LocationPoll extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationPollId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(comment = "확정된 장소")
    private String confirmed_location;

    @Column(comment = "확정된 장소 위도")
    private String confirmed_lat;

    @Column(comment = "확정된 장소 경도")
    private String confirmed_lng;

    @Column(comment = "투표 상태", nullable = false)
    @Enumerated(EnumType.STRING)
    private PollStatus pollStatus;

    @OneToMany(mappedBy = "locationPoll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocationVote> listLocationVote;
}
