package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.domain.schedule.dto.CreateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.domain.schedule.repository.ScheduleVoteRepository;
import com.dnd.moyeolak.domain.schedule.service.impl.ScheduleVoteServiceImpl;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleVoteServiceUnitTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private ParticipantService participantService;

    @Mock
    private ScheduleVoteRepository scheduleVoteRepository;

    @InjectMocks
    private ScheduleVoteServiceImpl scheduleService;

    @Nested
    @DisplayName("일정 투표 생성")
    class CreateParticipantVote {

        @Test
        @DisplayName("참가자가 모임에 추가된다")
        void createsParticipantInMeeting() {
            // given
            String meetingId = "test-meeting";
            Meeting meeting = Meeting.ofId(meetingId);
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            meeting.addPolls(schedulePoll, null);

            LocalDate today = LocalDate.now();
            List<LocalDateTime> votedDates = List.of(
                    today.atTime(9, 0),
                    today.atTime(9, 30)
            );

            CreateScheduleVoteRequest request = new CreateScheduleVoteRequest(
                    "홍길동", "local-key-123", votedDates, true
            );

            when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

            // when
            scheduleService.createParticipantVote(meetingId, request);

            // then
            assertThat(meeting.getParticipants()).hasSize(1);

            Participant createdParticipant = meeting.getParticipants().get(0);
            assertThat(createdParticipant.getName()).isEqualTo("홍길동");
            assertThat(createdParticipant.getLocalStorageKey()).isEqualTo("local-key-123");
        }

        @Test
        @DisplayName("참가자에 시간 투표가 연결된다")
        void createsScheduleVoteLinkedToParticipant() {
            // given
            String meetingId = "test-meeting";
            Meeting meeting = Meeting.ofId(meetingId);
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            meeting.addPolls(schedulePoll, null);

            LocalDate today = LocalDate.now();
            List<LocalDateTime> votedDates = List.of(
                    today.atTime(9, 0),
                    today.atTime(9, 30)
            );

            CreateScheduleVoteRequest request = new CreateScheduleVoteRequest(
                    "홍길동", "local-key-123", votedDates, true
            );

            when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

            // when
            scheduleService.createParticipantVote(meetingId, request);

            // then
            Participant createdParticipant = meeting.getParticipants().get(0);
            assertThat(createdParticipant.getScheduleVotes()).hasSize(1);

            ScheduleVote createdVote = createdParticipant.getScheduleVotes().get(0);
            assertThat(createdVote.getVotedDate()).isEqualTo(votedDates);
            assertThat(createdVote.getParticipant()).isEqualTo(createdParticipant);
            assertThat(createdVote.getSchedulePoll()).isEqualTo(schedulePoll);
        }

        @Test
        @DisplayName("localStorageKey 중복 검증이 수행된다")
        void validatesLocalStorageKeyUniqueness() {
            // given
            String meetingId = "test-meeting";
            Meeting meeting = Meeting.ofId(meetingId);
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            meeting.addPolls(schedulePoll, null);

            CreateScheduleVoteRequest request = new CreateScheduleVoteRequest(
                    "홍길동", "local-key-123", List.of(LocalDate.now().atTime(9, 0)), true
            );

            when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

            // when
            scheduleService.createParticipantVote(meetingId, request);

            // then
            verify(participantService).validateLocalStorageKeyUnique(meeting, "local-key-123");
        }

        @Test
        @DisplayName("중복 localStorageKey로 요청 시 예외가 발생한다")
        void duplicateLocalStorageKey_throwsException() {
            // given
            String meetingId = "test-meeting";
            Meeting meeting = Meeting.ofId(meetingId);
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            meeting.addPolls(schedulePoll, null);

            CreateScheduleVoteRequest request = new CreateScheduleVoteRequest(
                    "홍길동", "duplicate-key", List.of(LocalDate.now().atTime(9, 0)), true
            );

            when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));
            doThrow(new BusinessException(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY))
                    .when(participantService).validateLocalStorageKeyUnique(meeting, "duplicate-key");

            // when & then
            assertThatThrownBy(() -> scheduleService.createParticipantVote(meetingId, request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("존재하지 않는 모임 ID로 요청 시 예외가 발생한다")
        void meetingNotFound_throwsException() {
            // given
            String meetingId = "non-existent";
            CreateScheduleVoteRequest request = new CreateScheduleVoteRequest(
                    "홍길동", "local-key", List.of(LocalDate.now().atTime(9, 0)), true
            );

            when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.createParticipantVote(meetingId, request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("일정 투표판이 없으면 예외가 발생한다")
        void schedulePollNotFound_throwsException() {
            // given
            String meetingId = "test-meeting";
            Meeting meeting = Meeting.ofId(meetingId);
            // addPolls 호출 안 함 → schedulePoll이 null

            CreateScheduleVoteRequest request = new CreateScheduleVoteRequest(
                    "홍길동", "local-key", List.of(LocalDate.now().atTime(9, 0)), true
            );

            when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

            // when & then
            assertThatThrownBy(() -> scheduleService.createParticipantVote(meetingId, request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("일정 투표 수정")
    class UpdateParticipantVote {

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

            UpdateScheduleVoteRequest request = new UpdateScheduleVoteRequest(
                    1L, "홍길동", votedDates, true
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(1L)).thenReturn(Optional.of(scheduleVote));

            // when
            scheduleService.updateParticipantVote(1L, request);

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

            UpdateScheduleVoteRequest request = new UpdateScheduleVoteRequest(
                    1L, "홍길동", unavailableDates, false
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(1L)).thenReturn(Optional.of(scheduleVote));

            // when
            scheduleService.updateParticipantVote(1L, request);

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

            UpdateScheduleVoteRequest request = new UpdateScheduleVoteRequest(
                    1L, "홍길동", List.of(), false
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(1L)).thenReturn(Optional.of(scheduleVote));

            // when
            scheduleService.updateParticipantVote(1L, request);

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

            UpdateScheduleVoteRequest request = new UpdateScheduleVoteRequest(
                    1L, "새이름", List.of(), true
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(1L)).thenReturn(Optional.of(scheduleVote));

            // when
            scheduleService.updateParticipantVote(1L, request);

            // then
            assertThat(participant.getName()).isEqualTo("새이름");
        }

        @Test
        @DisplayName("존재하지 않는 scheduleVoteId로 요청 시 예외가 발생한다")
        void scheduleVoteNotFound_throwsException() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            Participant participant = Participant.of(meeting, "local-key", "홍길동");

            UpdateScheduleVoteRequest request = new UpdateScheduleVoteRequest(
                    1L, "홍길동", List.of(), true
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.updateParticipantVote(999L, request))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
