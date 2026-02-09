package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVotesRequest;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.domain.schedule.repository.ScheduleVoteRepository;
import com.dnd.moyeolak.domain.schedule.service.impl.ScheduleServiceImpl;
import com.dnd.moyeolak.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceUnitTest {

    @Mock
    private ParticipantService participantService;

    @Mock
    private ScheduleVoteRepository scheduleVoteRepository;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Nested
    @DisplayName("일정 투표 수정")
    class UpdateScheduleVotes {

        @Test
        @DisplayName("가능한 시간 선택 시 votedDates가 그대로 저장된다")
        void selectingAvailable_savesVotedDatesDirectly() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            Participant participant = Participant.of(meeting, "local-key", "홍길동");
            ScheduleVote scheduleVote = ScheduleVote.of(participant, schedulePoll, new ArrayList<>());

            LocalDate today = LocalDate.now();
            List<LocalDateTime> votedDates = List.of(
                    today.atTime(9, 0),
                    today.atTime(9, 30),
                    today.atTime(10, 0)
            );

            UpdateScheduleVotesRequest request = new UpdateScheduleVotesRequest(
                    1L, "홍길동", votedDates, true
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(1L)).thenReturn(Optional.of(scheduleVote));

            // when
            scheduleService.updateScheduleVotes(1L, request);

            // then
            assertThat(scheduleVote.getVotedDate()).isEqualTo(votedDates);
        }

        @Test
        @DisplayName("불가능한 시간 선택 시 전체 슬롯에서 제외한 나머지가 저장된다")
        void selectingUnavailable_savesRemainingSlots() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            Participant participant = Participant.of(meeting, "local-key", "홍길동");
            ScheduleVote scheduleVote = ScheduleVote.of(participant, schedulePoll, new ArrayList<>());

            LocalDate today = LocalDate.now();
            List<LocalDateTime> unavailableDates = List.of(
                    today.atTime(7, 30),
                    today.atTime(8, 0)
            );

            UpdateScheduleVotesRequest request = new UpdateScheduleVotesRequest(
                    1L, "홍길동", unavailableDates, false
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(1L)).thenReturn(Optional.of(scheduleVote));

            // when
            scheduleService.updateScheduleVotes(1L, request);

            // then
            List<LocalDateTime> allSlots = schedulePoll.generateAllTimeSlots();
            List<LocalDateTime> expectedAvailable = allSlots.stream()
                    .filter(slot -> !unavailableDates.contains(slot))
                    .toList();
            assertThat(scheduleVote.getVotedDate()).isEqualTo(expectedAvailable);
        }

        @Test
        @DisplayName("불가능한 시간이 없으면 전체 슬롯이 저장된다")
        void selectingUnavailable_emptyUnavailable_savesAllSlots() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            Participant participant = Participant.of(meeting, "local-key", "홍길동");
            ScheduleVote scheduleVote = ScheduleVote.of(participant, schedulePoll, new ArrayList<>());

            UpdateScheduleVotesRequest request = new UpdateScheduleVotesRequest(
                    1L, "홍길동", List.of(), false
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(1L)).thenReturn(Optional.of(scheduleVote));

            // when
            scheduleService.updateScheduleVotes(1L, request);

            // then
            List<LocalDateTime> allSlots = schedulePoll.generateAllTimeSlots();
            assertThat(scheduleVote.getVotedDate()).isEqualTo(allSlots);
        }

        @Test
        @DisplayName("참여자 이름이 업데이트된다")
        void updatesParticipantName() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            Participant participant = Participant.of(meeting, "local-key", "기존이름");
            ScheduleVote scheduleVote = ScheduleVote.of(participant, schedulePoll, new ArrayList<>());

            UpdateScheduleVotesRequest request = new UpdateScheduleVotesRequest(
                    1L, "새이름", List.of(), true
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(1L)).thenReturn(Optional.of(scheduleVote));

            // when
            scheduleService.updateScheduleVotes(1L, request);

            // then
            assertThat(participant.getName()).isEqualTo("새이름");
        }

        @Test
        @DisplayName("존재하지 않는 scheduleId로 요청 시 예외가 발생한다")
        void scheduleVoteNotFound_throwsException() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            Participant participant = Participant.of(meeting, "local-key", "홍길동");

            UpdateScheduleVotesRequest request = new UpdateScheduleVotesRequest(
                    1L, "홍길동", List.of(), true
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.updateScheduleVotes(999L, request))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
