package com.dnd.moyeolak.domain.schedule.entity;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.global.converter.LocalDateListConverter;
import com.dnd.moyeolak.global.entity.BaseEntity;
import com.dnd.moyeolak.global.enums.PollStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Builder.Default
    @Convert(converter = LocalDateListConverter.class)
    @Column(length = 1000, comment = "날짜 범위", nullable = false)
    private List<LocalDate> dateOption = new ArrayList<>();

    @Builder.Default
    @Column(comment = "시작 시간", nullable = false)
    private int startTime = 7;

    @Builder.Default
    @Column(comment = "종료 시간", nullable = false)
    private int endTime = 24;

    @Column(columnDefinition = "DATETIME(0)", comment = "확정 시작 시간")
    private LocalDateTime confirmedStartTime;

    @Column(columnDefinition = "DATETIME(0)", comment = "확정 종료 시간")
    private LocalDateTime confirmedEndTime;

    @Builder.Default
    @Column(comment = "투표 상태", nullable = false)
    @Enumerated(EnumType.STRING)
    private PollStatus pollStatus = PollStatus.INACTIVE;

    @OneToMany(mappedBy = "schedulePoll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleVote> scheduleVotes = new ArrayList<>();

    public static SchedulePoll defaultOf(Meeting meeting) {
        LocalDate nowDate = LocalDate.now();
        List<LocalDate> defaultDates = nowDate
                .datesUntil(nowDate.plusDays(14))
                .collect(Collectors.toCollection(ArrayList::new));

        return SchedulePoll.builder()
                .meeting(meeting)
                .dateOption(defaultDates)
                .build();
    }

    public void addDate(LocalDate date) {
        if (!this.dateOption.contains(date)) {
            this.dateOption.add(date);
            this.dateOption.sort(Comparator.naturalOrder());
        }
    }

    public void removeDate(LocalDate date) {
        this.dateOption.remove(date);
    }

    public void updateDateOption(List<LocalDate> newDates) {
        this.dateOption.clear();
        this.dateOption.addAll(newDates);
        this.dateOption.sort(Comparator.naturalOrder());
    }
}
