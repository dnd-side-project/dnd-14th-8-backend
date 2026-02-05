package com.dnd.moyeolak.domain.participant.service;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.repository.ParticipantRepository;
import com.dnd.moyeolak.domain.participant.service.impl.ParticipantServiceImpl;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.repository.SchedulePollRepository;
import com.dnd.moyeolak.domain.location.repository.LocationPollRepository;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceImplTest {

    private static final String MEETING_ID = "meeting-id";
    private static final String LOCAL_STORAGE_KEY = "local-storage-key";

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private SchedulePollRepository schedulePollRepository;

    @Mock
    private LocationPollRepository locationPollRepository;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    @Test
    @DisplayName("일정 투표 기반 참여자 생성 성공 시 투표 개수와 저장 여부를 반환한다")
    void createWithSchedule_success() {
        Meeting meeting = Meeting.of(10);
        SchedulePoll schedulePoll = SchedulePoll.defaultOf(meeting);
        CreateParticipantWithScheduleRequest request = new CreateParticipantWithScheduleRequest(
                "홍길동",
                LOCAL_STORAGE_KEY,
                List.of(
                        LocalDateTime.of(2025, 2, 10, 9, 0),
                        LocalDateTime.of(2025, 2, 10, 10, 0)
                )
        );

        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.of(meeting));
        when(participantRepository.existsByMeetingAndLocalStorageKey(meeting, LOCAL_STORAGE_KEY)).thenReturn(false);
        when(schedulePollRepository.findByMeeting(meeting)).thenReturn(Optional.of(schedulePoll));

        CreateParticipantResponse response = participantService.createWithSchedule(MEETING_ID, request);

        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.scheduleVoteCount()).isEqualTo(request.availableSchedules().size());
        assertThat(response.hasLocation()).isFalse();

        ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);
        verify(participantRepository).save(captor.capture());
        Participant savedParticipant = captor.getValue();
        assertThat(savedParticipant.getScheduleVotes()).hasSize(2);
        assertThat(savedParticipant.getScheduleVotes().get(0).getSchedulePoll()).isEqualTo(schedulePoll);
        assertThat(savedParticipant.getScheduleVotes().get(0).getParticipant()).isEqualTo(savedParticipant);
    }

    @Test
    @DisplayName("일정 투표 기반 참여자 생성 시 모임을 찾지 못하면 예외를 던진다")
    void createWithSchedule_meetingNotFound() {
        CreateParticipantWithScheduleRequest request = new CreateParticipantWithScheduleRequest(
                "홍길동",
                LOCAL_STORAGE_KEY,
                List.of(LocalDateTime.of(2025, 2, 10, 9, 0))
        );
        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.empty());

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
                "홍길동",
                LOCAL_STORAGE_KEY,
                List.of(LocalDateTime.of(2025, 2, 10, 9, 0))
        );

        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.of(meeting));
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
                "홍길동",
                LOCAL_STORAGE_KEY,
                List.of(LocalDateTime.of(2025, 2, 10, 9, 0))
        );

        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.of(meeting));
        when(participantRepository.existsByMeetingAndLocalStorageKey(meeting, LOCAL_STORAGE_KEY)).thenReturn(false);
        when(schedulePollRepository.findByMeeting(meeting)).thenReturn(Optional.empty());

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
        CreateParticipantWithLocationRequest request = new CreateParticipantWithLocationRequest(
                "이영희",
                LOCAL_STORAGE_KEY,
                new CreateParticipantWithLocationRequest.LocationInput(
                        new BigDecimal("37.5665"),
                        new BigDecimal("126.9780"),
                        "서울시 중구 명동"
                )
        );

        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.of(meeting));
        when(participantRepository.existsByMeetingAndLocalStorageKey(meeting, LOCAL_STORAGE_KEY)).thenReturn(false);
        when(locationPollRepository.findByMeeting(meeting)).thenReturn(Optional.of(locationPoll));

        CreateParticipantResponse response = participantService.createWithLocation(MEETING_ID, request);

        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.hasLocation()).isTrue();
        assertThat(response.scheduleVoteCount()).isNull();

        ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);
        verify(participantRepository).save(captor.capture());
        Participant savedParticipant = captor.getValue();
        assertThat(savedParticipant.getLocationVotes()).hasSize(1);
        assertThat(savedParticipant.getLocationVotes().get(0).getLocationPoll()).isEqualTo(locationPoll);
        assertThat(savedParticipant.getLocationVotes().get(0).getDepartureLat()).isEqualTo(request.location().latitude());
        assertThat(savedParticipant.getLocationVotes().get(0).getDepartureLng()).isEqualTo(request.location().longitude());
        assertThat(savedParticipant.getLocationVotes().get(0).getDepartureLocation()).isEqualTo(request.location().address());
    }

    @Test
    @DisplayName("위치 투표 기반 참여자 생성 시 localStorageKey 중복이면 예외를 던진다")
    void createWithLocation_duplicateLocalStorageKey() {
        Meeting meeting = Meeting.of(10);
        CreateParticipantWithLocationRequest request = new CreateParticipantWithLocationRequest(
                "이영희",
                LOCAL_STORAGE_KEY,
                new CreateParticipantWithLocationRequest.LocationInput(
                        new BigDecimal("37.5665"),
                        new BigDecimal("126.9780"),
                        "서울시 중구 명동"
                )
        );

        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.of(meeting));
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
                "이영희",
                LOCAL_STORAGE_KEY,
                new CreateParticipantWithLocationRequest.LocationInput(
                        new BigDecimal("37.5665"),
                        new BigDecimal("126.9780"),
                        "서울시 중구 명동"
                )
        );

        when(meetingRepository.findById(MEETING_ID)).thenReturn(Optional.of(meeting));
        when(participantRepository.existsByMeetingAndLocalStorageKey(meeting, LOCAL_STORAGE_KEY)).thenReturn(false);
        when(locationPollRepository.findByMeeting(meeting)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> participantService.createWithLocation(MEETING_ID, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOCATION_POLL_NOT_FOUND);
    }
}
