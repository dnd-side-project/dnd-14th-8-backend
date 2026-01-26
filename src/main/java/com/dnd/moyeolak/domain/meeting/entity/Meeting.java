package com.dnd.moyeolak.domain.meeting.entity;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Entity
public class Meeting extends BaseEntity {

    @Id
    @Column(length = 36, comment = "회의 ID")
    private String meetingId;

    @Column(length = 500, comment = "링크")
    private String link;

    @Column(comment = "참여자 수")
    private int participantCount;

    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL)
    private SchedulePoll schedulePoll;

    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL)
    private LocationPoll locationPoll;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> listParticipant;

    // save() 호출 시 자동 실행되어 UUID 생성
    @PrePersist
    public void generateMeetingId() {
        if (meetingId == null) {
            meetingId = UUID.randomUUID().toString();
        }
    }
}
