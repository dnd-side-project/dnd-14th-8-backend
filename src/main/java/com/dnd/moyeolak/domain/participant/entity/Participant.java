package com.dnd.moyeolak.domain.participant.entity;

import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
public class Participant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long participantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @Column(comment = "고유키", nullable = false, unique = true)
    private String local_storage_key;

    @Column(comment = "이름", nullable = false)
    private String name;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleVote> listScheduleVote;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocationVote> listLocationVote;

}
