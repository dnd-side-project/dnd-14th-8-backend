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
        @DisplayName("참가자가 저장된다")
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
                    "홍길동", "local-key-123", votedDates
            );

            when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

            // when
            scheduleService.createParticipantVote(meetingId, request);

            // then
            verify(participantService).save(argThat(participant ->
                    participant.getName().equals("홍길동") &&
                    participant.getLocalStorageKey().equals("local-key-123")
            ));
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
                    "홍길동", "local-key-123", votedDates
            );

            when(meetingRepository.findByIdWithAllAssociations(meetingId)).thenReturn(Optional.of(meeting));

            // when
            scheduleService.createParticipantVote(meetingId, request);

            // then
            verify(participantService).save(argThat(participant -> {
                assertThat(participant.getScheduleVotes()).hasSize(1);
                ScheduleVote createdVote = participant.getScheduleVotes().get(0);
                assertThat(createdVote.getVotedDate()).isEqualTo(votedDates);
                assertThat(createdVote.getParticipant()).isEqualTo(participant);
                assertThat(createdVote.getSchedulePoll()).isEqualTo(schedulePoll);
                return true;
            }));
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
                    "홍길동", "local-key-123", List.of(LocalDate.now().atTime(9, 0))
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
                    "홍길동", "duplicate-key", List.of(LocalDate.now().atTime(9, 0))
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
                    "홍길동", "local-key", List.of(LocalDate.now().atTime(9, 0))
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
                    "홍길동", "local-key", List.of(LocalDate.now().atTime(9, 0))
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
        @DisplayName("votedDates가 그대로 저장된다")
        void savesVotedDatesDirectly() {
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
                    1L, "홍길동", votedDates
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(1L)).thenReturn(Optional.of(scheduleVote));

            // when
            scheduleService.updateParticipantVote(1L, request);

            // then
            assertThat(scheduleVote.getVotedDate()).isEqualTo(votedDates);
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
                    1L, "새이름", List.of()
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
                    1L, "홍길동", List.of()
            );

            when(participantService.getById(1L)).thenReturn(participant);
            when(scheduleVoteRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> scheduleService.updateParticipantVote(999L, request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("범위 밖 투표 삭제")
    class DeleteOutOfRangeVotes {

        @Test
        @DisplayName("날짜 범위 밖의 투표 항목이 제거된다")
        void removesVotesWithDateOutOfRange() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            // defaultOf: dateOptions = today ~ today+13, startTime=540, endTime=1440

            LocalDate validDate = LocalDate.now();
            LocalDate invalidDate = LocalDate.now().minusDays(1); // today-1은 범위 밖

            ScheduleVote vote = ScheduleVote.of(schedulePoll, new ArrayList<>(List.of(
                    validDate.atTime(9, 0),
                    invalidDate.atTime(9, 0)
            )));
            schedulePoll.getScheduleVotes().add(vote);

            // when
            scheduleService.deleteOutOfRangeVotes(schedulePoll);

            // then
            assertThat(vote.getVotedDate()).containsExactly(validDate.atTime(9, 0));
        }

        @Test
        @DisplayName("startTime 이전 시간 투표 항목이 제거되고, startTime 정각은 유지된다")
        void removesVotesBeforeStartTime_andKeepsStartTimeBoundary() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            // default startTime = 9 * 60 = 540분

            LocalDate today = LocalDate.now();
            ScheduleVote vote = ScheduleVote.of(schedulePoll, new ArrayList<>(List.of(
                    today.atTime(8, 30), // 510분 < 540분 → 제거
                    today.atTime(9, 0)   // 540분 = startTime → 경계값, 유지
            )));
            schedulePoll.getScheduleVotes().add(vote);

            // when
            scheduleService.deleteOutOfRangeVotes(schedulePoll);

            // then
            assertThat(vote.getVotedDate()).containsExactly(today.atTime(9, 0));
        }

        @Test
        @DisplayName("endTime 이상인 시간 투표 항목이 제거되고, endTime 직전은 유지된다")
        void removesVotesAtOrAfterEndTime_andKeepsBeforeEndTimeBoundary() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            LocalDate today = LocalDate.now();
            schedulePoll.updateOptions(List.of(today), 7 * 60, 10 * 60); // endTime = 600분(10:00)

            ScheduleVote vote = ScheduleVote.of(schedulePoll, new ArrayList<>(List.of(
                    today.atTime(9, 30), // 570분 < 600분 → 유지
                    today.atTime(10, 0)  // 600분 = endTime → 경계값, 제거
            )));
            schedulePoll.getScheduleVotes().add(vote);

            // when
            scheduleService.deleteOutOfRangeVotes(schedulePoll);

            // then
            assertThat(vote.getVotedDate()).containsExactly(today.atTime(9, 30));
        }

        @Test
        @DisplayName("모든 투표 항목이 범위 내이면 아무것도 제거되지 않는다")
        void keepsAllVotesWithinRange() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            LocalDate today = LocalDate.now();

            ScheduleVote vote = ScheduleVote.of(schedulePoll, new ArrayList<>(List.of(
                    today.atTime(9, 0),
                    today.atTime(9, 30)
            )));
            schedulePoll.getScheduleVotes().add(vote);

            // when
            scheduleService.deleteOutOfRangeVotes(schedulePoll);

            // then
            assertThat(vote.getVotedDate()).hasSize(2);
        }

        @Test
        @DisplayName("여러 참가자의 투표 항목이 독립적으로 필터링된다")
        void filtersEachVoteIndependently() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            LocalDate today = LocalDate.now();
            LocalDate invalidDate = LocalDate.now().minusDays(1);

            ScheduleVote vote1 = ScheduleVote.of(schedulePoll, new ArrayList<>(List.of(
                    today.atTime(9, 0),      // 유지
                    invalidDate.atTime(9, 0) // 날짜 범위 밖 → 제거
            )));
            ScheduleVote vote2 = ScheduleVote.of(schedulePoll, new ArrayList<>(List.of(
                    today.atTime(9, 30), // 유지
                    today.atTime(6, 0)   // startTime(07:00) 이전 → 제거
            )));
            schedulePoll.getScheduleVotes().add(vote1);
            schedulePoll.getScheduleVotes().add(vote2);

            // when
            scheduleService.deleteOutOfRangeVotes(schedulePoll);

            // then
            assertThat(vote1.getVotedDate()).containsExactly(today.atTime(9, 0));
            assertThat(vote2.getVotedDate()).containsExactly(today.atTime(9, 30));
        }

        @Test
        @DisplayName("자정을 넘기는 시간대 설정 시 허용 구간만 유지된다")
        void keepsVotesWithinOvernightWindow() {
            // given
            Meeting meeting = Meeting.ofId("test-meeting");
            SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
            LocalDate today = LocalDate.now();
            schedulePoll.updateOptions(List.of(today), 18 * 60, 6 * 60);

            ScheduleVote vote = ScheduleVote.of(schedulePoll, new ArrayList<>(List.of(
                    today.atTime(17, 30), // end(06:00)~start(18:00) 사이 → 제거
                    today.atTime(18, 0),  // start 경계 → 유지
                    today.atTime(23, 30), // start 이후 → 유지
                    today.atTime(1, 0),   // end 이전 (자정 이후) → 유지
                    today.atTime(7, 0)    // 06:00 이후 & start 이전 → 제거
            )));
            schedulePoll.getScheduleVotes().add(vote);

            // when
            scheduleService.deleteOutOfRangeVotes(schedulePoll);

            // then
            assertThat(vote.getVotedDate()).containsExactlyInAnyOrder(
                    today.atTime(18, 0),
                    today.atTime(23, 30),
                    today.atTime(1, 0)
            );
        }
    }
}
