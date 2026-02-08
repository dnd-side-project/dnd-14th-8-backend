package com.dnd.moyeolak.domain.participant.service;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
import com.dnd.moyeolak.domain.participant.dto.GetParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.repository.ParticipantRepository;
import com.dnd.moyeolak.domain.participant.service.impl.ParticipantServiceImpl;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceImplTest {

    private static final String MEETING_ID = "meeting-id";
    private static final String LOCAL_STORAGE_KEY = "local-storage-key";

    @Mock
    private MeetingService meetingService;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    @Test
    @DisplayName("일정 투표 기반 참여자 생성 성공 시 투표 개수와 저장 여부를 반환한다")
    void createWithSchedule_success() {
        Meeting meeting = Meeting.of(10);
        SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
        meeting.addPolls(schedulePoll, null);

        List<LocalDateTime> schedules = List.of(
                LocalDateTime.of(2025, 2, 10, 9, 0),
                LocalDateTime.of(2025, 2, 10, 10, 0)
        );
        CreateParticipantWithScheduleRequest request = new CreateParticipantWithScheduleRequest(
                "홍길동", LOCAL_STORAGE_KEY, schedules
        );

        when(meetingService.get(MEETING_ID)).thenReturn(meeting);
        when(participantRepository.existsByMeetingAndLocalStorageKey(any(), anyString())).thenReturn(false);

        CreateParticipantResponse response = participantService.createWithSchedule(MEETING_ID, request);

        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.scheduleVoteCount()).isEqualTo(2);
        assertThat(response.hasLocation()).isFalse();
        assertThat(meeting.getParticipants()).hasSize(1);
    }

    @Test
    @DisplayName("일정 투표 기반 참여자 생성 시 모임을 찾지 못하면 예외를 던진다")
    void createWithSchedule_meetingNotFound() {
        CreateParticipantWithScheduleRequest request = new CreateParticipantWithScheduleRequest(
                "홍길동", LOCAL_STORAGE_KEY, List.of(LocalDateTime.of(2025, 2, 10, 9, 0))
        );
        when(meetingService.get(MEETING_ID)).thenThrow(new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        assertThatThrownBy(() -> participantService.createWithSchedule(MEETING_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEETING_NOT_FOUND);
    }

    @Test
    @DisplayName("일정 투표 기반 참여자 생성 시 localStorageKey 중복이면 예외를 던진다")
    void createWithSchedule_duplicateLocalStorageKey() {
        Meeting meeting = Meeting.of(10);
        CreateParticipantWithScheduleRequest request = new CreateParticipantWithScheduleRequest(
                "홍길동", LOCAL_STORAGE_KEY, List.of(LocalDateTime.of(2025, 2, 10, 9, 0))
        );

        when(meetingService.get(MEETING_ID)).thenReturn(meeting);
        when(participantRepository.existsByMeetingAndLocalStorageKey(meeting, LOCAL_STORAGE_KEY)).thenReturn(true);

        assertThatThrownBy(() -> participantService.createWithSchedule(MEETING_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY);
    }

    @Test
    @DisplayName("일정 투표 기반 참여자 생성 시 일정 투표판이 없으면 예외를 던진다")
    void createWithSchedule_schedulePollNotFound() {
        Meeting meeting = Meeting.of(10);
        CreateParticipantWithScheduleRequest request = new CreateParticipantWithScheduleRequest(
                "홍길동", LOCAL_STORAGE_KEY, List.of(LocalDateTime.of(2025, 2, 10, 9, 0))
        );

        when(meetingService.get(MEETING_ID)).thenReturn(meeting);
        when(participantRepository.existsByMeetingAndLocalStorageKey(any(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> participantService.createWithSchedule(MEETING_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SCHEDULE_POLL_NOT_FOUND);
    }

    @Test
    @DisplayName("위치 투표 기반 참여자 생성 성공 시 위치 투표 정보가 저장된다")
    void createWithLocation_success() {
        Meeting meeting = Meeting.of(10);
        LocationPoll locationPoll = LocationPoll.defaultOf(meeting);
        meeting.addPolls(null, locationPoll);

        CreateParticipantWithLocationRequest request = new CreateParticipantWithLocationRequest(
                "이영희", LOCAL_STORAGE_KEY,
                new CreateParticipantWithLocationRequest.LocationInput(
                        new BigDecimal("37.5665"), new BigDecimal("126.9780"), "서울시 중구 명동"
                )
        );

        when(meetingService.get(MEETING_ID)).thenReturn(meeting);
        when(participantRepository.existsByMeetingAndLocalStorageKey(any(), anyString())).thenReturn(false);

        CreateParticipantResponse response = participantService.createWithLocation(MEETING_ID, request);

        assertThat(response.name()).isEqualTo("이영희");
        assertThat(response.hasLocation()).isTrue();
        assertThat(response.scheduleVoteCount()).isNull();
        assertThat(meeting.getParticipants()).hasSize(1);
    }

    @Test
    @DisplayName("위치 투표 기반 참여자 생성 시 localStorageKey 중복이면 예외를 던진다")
    void createWithLocation_duplicateLocalStorageKey() {
        Meeting meeting = Meeting.of(10);
        CreateParticipantWithLocationRequest request = new CreateParticipantWithLocationRequest(
                "이영희", LOCAL_STORAGE_KEY,
                new CreateParticipantWithLocationRequest.LocationInput(
                        new BigDecimal("37.5665"), new BigDecimal("126.9780"), "서울시 중구 명동"
                )
        );

        when(meetingService.get(MEETING_ID)).thenReturn(meeting);
        when(participantRepository.existsByMeetingAndLocalStorageKey(meeting, LOCAL_STORAGE_KEY)).thenReturn(true);

        assertThatThrownBy(() -> participantService.createWithLocation(MEETING_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY);
    }

    @Test
    @DisplayName("위치 투표 기반 참여자 생성 시 위치 투표판이 없으면 예외를 던진다")
    void createWithLocation_locationPollNotFound() {
        Meeting meeting = Meeting.of(10);
        CreateParticipantWithLocationRequest request = new CreateParticipantWithLocationRequest(
                "이영희", LOCAL_STORAGE_KEY,
                new CreateParticipantWithLocationRequest.LocationInput(
                        new BigDecimal("37.5665"), new BigDecimal("126.9780"), "서울시 중구 명동"
                )
        );

        when(meetingService.get(MEETING_ID)).thenReturn(meeting);
        when(participantRepository.existsByMeetingAndLocalStorageKey(any(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> participantService.createWithLocation(MEETING_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOCATION_POLL_NOT_FOUND);
    }

    @Test
    @DisplayName("참여자 조회 성공 시 참여자 정보를 반환한다")
    void getParticipant_success() {
        // Given
        Long participantId = 1L;
        Participant participant = Participant.of(Meeting.of(10), "some-key", "김철수");
        when(participantRepository.findById(participantId)).thenReturn(java.util.Optional.of(participant));

        // When
        GetParticipantResponse response = participantService.getParticipant(participantId);

        // Then
        assertThat(response.participantId()).isEqualTo(participant.getParticipantId());
        assertThat(response.name()).isEqualTo("김철수");
        assertThat(response.isHost()).isFalse();
    }

    @Test
    @DisplayName("방장인 참여자 조회 성공 시 isHost가 true로 반환된다")
    void getParticipant_hostSuccess() {
        // Given
        Long participantId = 2L;
        Participant hostParticipant = Participant.hostOf(Meeting.of(10), "host-key", "이방장");
        when(participantRepository.findById(participantId)).thenReturn(java.util.Optional.of(hostParticipant));

        // When
        GetParticipantResponse response = participantService.getParticipant(participantId);

        // Then
        assertThat(response.participantId()).isEqualTo(hostParticipant.getParticipantId());
        assertThat(response.name()).isEqualTo("이방장");
        assertThat(response.isHost()).isTrue();
    }

    @Test
    @DisplayName("참여자 전체 조회 성공 시 참여자 목록을 반환한다")
    void listParticipants_success() {
        // Given
        Meeting meeting = Meeting.of(10);
        Participant host = Participant.hostOf(meeting, "host-key", "김방장");
        Participant member = Participant.of(meeting, "member-key", "이참여");
        meeting.addParticipant(host);
        meeting.addParticipant(member);

        when(meetingService.get(MEETING_ID)).thenReturn(meeting);

        // When
        ListParticipantResponse response = participantService.listParticipants(MEETING_ID);

        // Then
        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.participants()).hasSize(2);
        assertThat(response.participants().get(0).name()).isEqualTo("김방장");
        assertThat(response.participants().get(0).isHost()).isTrue();
        assertThat(response.participants().get(1).name()).isEqualTo("이참여");
        assertThat(response.participants().get(1).isHost()).isFalse();
    }

    @Test
    @DisplayName("참여자가 없는 모임 전체 조회 시 빈 목록을 반환한다")
    void listParticipants_emptyList() {
        // Given
        Meeting meeting = Meeting.of(10);
        when(meetingService.get(MEETING_ID)).thenReturn(meeting);

        // When
        ListParticipantResponse response = participantService.listParticipants(MEETING_ID);

        // Then
        assertThat(response.totalCount()).isZero();
        assertThat(response.participants()).isEmpty();
    }

    @Test
    @DisplayName("참여자 전체 조회 시 모임을 찾지 못하면 예외를 던진다")
    void listParticipants_meetingNotFound() {
        // Given
        when(meetingService.get(MEETING_ID)).thenThrow(new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> participantService.listParticipants(MEETING_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEETING_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 참여자 조회 시 예외를 던진다")
    void getParticipant_notFound() {
        // Given
        Long participantId = 99L;
        when(participantRepository.findById(participantId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> participantService.getParticipant(participantId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PARTICIPANT_NOT_FOUND);
    }
}
