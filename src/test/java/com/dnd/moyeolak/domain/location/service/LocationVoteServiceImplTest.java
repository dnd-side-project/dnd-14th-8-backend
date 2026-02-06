package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.repository.LocationPollRepository;
import com.dnd.moyeolak.domain.location.service.impl.LocationVoteServiceImpl;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationVoteServiceImplTest {

    @Mock
    private LocationPollRepository locationPollRepository;

    @InjectMocks
    private LocationVoteServiceImpl locationVoteService;

    @Test
    @DisplayName("위치 투표 생성 성공 시 투표를 반환한다")
    void createVote_success() {
        Meeting meeting = Meeting.of(10);
        LocationPoll locationPoll = LocationPoll.defaultOf(meeting);
        Participant participant = Participant.of(meeting, "local-key", "홍길동");
        String address = "서울시 중구 명동";
        BigDecimal latitude = new BigDecimal("37.5665");
        BigDecimal longitude = new BigDecimal("126.9780");

        when(locationPollRepository.findByMeeting(meeting)).thenReturn(Optional.of(locationPoll));

        LocationVote result = locationVoteService.createVote(meeting, participant, address, latitude, longitude);

        assertThat(result.getParticipant()).isEqualTo(participant);
        assertThat(result.getLocationPoll()).isEqualTo(locationPoll);
        assertThat(result.getDepartureLocation()).isEqualTo(address);
        assertThat(result.getDepartureLat()).isEqualTo(latitude);
        assertThat(result.getDepartureLng()).isEqualTo(longitude);
    }

    @Test
    @DisplayName("위치 투표판이 없으면 예외를 던진다")
    void createVote_throwsExceptionWhenLocationPollNotFound() {
        Meeting meeting = Meeting.of(10);
        Participant participant = Participant.of(meeting, "local-key", "홍길동");

        when(locationPollRepository.findByMeeting(meeting)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationVoteService.createVote(
                meeting, participant, "서울시 중구", new BigDecimal("37.5665"), new BigDecimal("126.9780")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOCATION_POLL_NOT_FOUND);
    }
}
