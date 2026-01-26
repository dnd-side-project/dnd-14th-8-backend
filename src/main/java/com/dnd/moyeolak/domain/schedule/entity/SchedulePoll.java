package com.dnd.moyeolak.domain.schedule.entity;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.global.entity.BaseEntity;
import com.dnd.moyeolak.global.enums.PollStatus;
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
public class SchedulePoll extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schedulePollId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(length = 1000, comment = "날짜 범위", nullable = false)
    private String dateOption;

    @Column(comment = "시작 시간", nullable = false)
    private int startTime;

    @Column(comment = "종료 시간", nullable = false)
    private int endTime;

    @Column(columnDefinition = "DATETIME(0)", comment = "확정 시작 시간")
    private LocalDateTime confirmedStartTime;

    @Column(columnDefinition = "DATETIME(0)", comment = "확정 종료 시간")
    private LocalDateTime confirmedEndTime;

    @Column(comment = "투표 상태", nullable = false)
    @Enumerated(EnumType.STRING)
    private PollStatus pollStatus;

    @OneToMany(mappedBy = "schedulePoll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleVote> scheduleVotes = new ArrayList<>();
}
