package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.schedule.dto.UpdateSchedulePollRequest;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.service.ScheduleVoteService;
import com.dnd.moyeolak.domain.schedule.service.impl.SchedulePollServiceImpl;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulePollServiceImplTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private ScheduleVoteService scheduleVoteService;

    @InjectMocks
    private SchedulePollServiceImpl schedulePollService;

    @Test
    @DisplayName("일정 투표 옵션 수정 시 HH:mm 입력이 30분 단위 분 값으로 변환된다")
    void updateSchedulePoll_convertsHalfHourStringsToMinutes() {
        // given
        String meetingId = "meeting-1";
        Meeting meeting = Meeting.of(5);
        SchedulePoll schedulePoll = spy(SchedulePoll.defaultOf(meeting));
        meeting.addPolls(schedulePoll, null);
        when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

        UpdateSchedulePollRequest request = new UpdateSchedulePollRequest(
                List.of(LocalDate.of(2025, 2, 10)),
                "07:30",
                "24:00"
        );

        // when
        schedulePollService.updateSchedulePoll(meetingId, request);

        // then
        verify(schedulePoll).updateOptions(eq(request.dateOptions()), eq(450), eq(1440));
        verify(scheduleVoteService).deleteOutOfRangeVotes(schedulePoll);
    }

    @Test
    @DisplayName("시작 시간을 24:00으로 전달하면 예외가 발생한다")
    void updateSchedulePoll_throwsWhenStartIsMidnight() {
        // given
        String meetingId = "meeting-1";
        Meeting meeting = Meeting.of(5);
        SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
        meeting.addPolls(schedulePoll, null);
        when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

        UpdateSchedulePollRequest request = new UpdateSchedulePollRequest(
                List.of(LocalDate.of(2025, 2, 10)),
                "24:00",
                "24:00"
        );

        // when & then
        assertThatThrownBy(() -> schedulePollService.updateSchedulePoll(meetingId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_FORMAT.getMessage());
    }

    @Test
    @DisplayName("시작 시간과 종료 시간이 동일하면 예외가 발생한다")
    void updateSchedulePoll_throwsWhenStartEqualsEnd() {
        // given
        String meetingId = "meeting-1";
        Meeting meeting = Meeting.of(5);
        SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
        meeting.addPolls(schedulePoll, null);
        when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

        UpdateSchedulePollRequest request = new UpdateSchedulePollRequest(
                List.of(LocalDate.of(2025, 2, 10)),
                "22:30",
                "22:30"
        );

        // when & then
        assertThatThrownBy(() -> schedulePollService.updateSchedulePoll(meetingId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_TIME_RANGE.getMessage());
    }

    @Test
    @DisplayName("투표 옵션 변경 후 모든 투표가 무효화되어도 비호스트 참여자는 삭제되지 않는다")
    void updateSchedulePoll_doesNotDeleteNonHostParticipantWhenAllVotesInvalidated() {
        // given
        String meetingId = "meeting-1";
        Meeting meeting = Meeting.of(5);
        SchedulePoll schedulePoll = spy(SchedulePoll.defaultOf(meeting));
        meeting.addPolls(schedulePoll, null);

        Participant nonHost = Participant.of(meeting, "non-host-key", "일반참여자");
        meeting.addParticipant(nonHost);

        when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

        UpdateSchedulePollRequest request = new UpdateSchedulePollRequest(
                List.of(LocalDate.of(2025, 2, 10)),
                "07:30",
                "24:00"
        );

        // when
        schedulePollService.updateSchedulePoll(meetingId, request);

        // then
        assertThat(meeting.getParticipants()).contains(nonHost);
    }

    @Test
    @DisplayName("시작 시간이 종료 시간보다 늦으면 예외가 발생한다")
    void updateSchedulePoll_throwsWhenStartIsAfterEnd() {
        // given
        String meetingId = "meeting-1";
        Meeting meeting = Meeting.of(5);
        SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
        meeting.addPolls(schedulePoll, null);
        when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

        UpdateSchedulePollRequest request = new UpdateSchedulePollRequest(
                List.of(LocalDate.of(2025, 2, 10)),
                "23:30",
                "06:00"
        );

        // when & then
        assertThatThrownBy(() -> schedulePollService.updateSchedulePoll(meetingId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_TIME_RANGE.getMessage());
    }
}
