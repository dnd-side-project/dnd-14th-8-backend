package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.domain.schedule.repository.SchedulePollRepository;
import com.dnd.moyeolak.domain.schedule.service.impl.ScheduleVoteServiceImpl;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleVoteServiceImplTest {

    @Mock
    private SchedulePollRepository schedulePollRepository;

    @InjectMocks
    private ScheduleVoteServiceImpl scheduleVoteService;

    @Test
    @DisplayName("일정 투표 생성 성공 시 투표 목록을 반환한다")
    void createVotes_success() {
        Meeting meeting = Meeting.of(10);
        SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
        Participant participant = Participant.of(meeting, "local-key", "홍길동");
        List<LocalDateTime> schedules = List.of(
                LocalDateTime.of(2025, 2, 10, 9, 0),
                LocalDateTime.of(2025, 2, 10, 10, 0)
        );

        when(schedulePollRepository.findByMeeting(meeting)).thenReturn(Optional.of(schedulePoll));

        List<ScheduleVote> result = scheduleVoteService.createVotes(meeting, participant, schedules);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getParticipant()).isEqualTo(participant);
        assertThat(result.get(0).getSchedulePoll()).isEqualTo(schedulePoll);
    }

    @Test
    @DisplayName("일정 투표판이 없으면 예외를 던진다")
    void createVotes_throwsExceptionWhenSchedulePollNotFound() {
        Meeting meeting = Meeting.of(10);
        Participant participant = Participant.of(meeting, "local-key", "홍길동");
        List<LocalDateTime> schedules = List.of(LocalDateTime.of(2025, 2, 10, 9, 0));

        when(schedulePollRepository.findByMeeting(meeting)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleVoteService.createVotes(meeting, participant, schedules))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SCHEDULE_POLL_NOT_FOUND);
    }
}
