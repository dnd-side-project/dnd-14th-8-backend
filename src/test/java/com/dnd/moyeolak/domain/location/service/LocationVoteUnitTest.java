package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.repository.LocationPollRepository;
import com.dnd.moyeolak.domain.location.repository.LocationVoteRepository;
import com.dnd.moyeolak.domain.location.service.impl.LocationVoteServiceImpl;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationVoteUnitTest {

    @Mock
    private ParticipantService participantService;

    @Mock
    private LocationPollRepository locationPollRepository;

    @Mock
    private LocationVoteRepository locationVoteRepository;

    @InjectMocks
    private LocationVoteServiceImpl locationService;

    @Test
    @DisplayName("수동 추가 시 LocationVote만 저장된다")
    void createLocationVote_manualAdd_savesLocationVoteOnly() {
        // given
        CreateLocationVoteRequest request = new CreateLocationVoteRequest(
                "meeting-id-123",
                "1",
                null,  // localStorageKey가 null → 수동 추가
                "홍길동",
                "서울시 강남구",
                "37.4979502",
                "127.0276368"
        );

        // when
        locationService.createLocationVote(request);

        // then
        ArgumentCaptor<LocationVote> captor = ArgumentCaptor.forClass(LocationVote.class);
        verify(locationVoteRepository).save(captor.capture());

        LocationVote savedVote = captor.getValue();
        assertThat(savedVote.getDepartureName()).isEqualTo("홍길동");
        assertThat(savedVote.getDepartureLocation()).isEqualTo("서울시 강남구");
        assertThat(savedVote.getDepartureLat()).isEqualByComparingTo(new BigDecimal("37.4979502"));
        assertThat(savedVote.getDepartureLng()).isEqualByComparingTo(new BigDecimal("127.0276368"));

        verify(participantService, never()).save(any());
    }

    @Test
    @DisplayName("수동 추가 시 빈 문자열 localStorageKey도 수동 추가로 처리된다")
    void createLocationVote_emptyLocalStorageKey_savesLocationVoteOnly() {
        // given
        CreateLocationVoteRequest request = new CreateLocationVoteRequest(
                "meeting-id-123",
                "1",
                "",  // 빈 문자열 → 수동 추가
                "김철수",
                "서울시 홍대입구",
                "37.5571010",
                "126.9236450"
        );

        // when
        locationService.createLocationVote(request);

        // then
        verify(locationVoteRepository).save(any(LocationVote.class));
        verify(participantService, never()).save(any());
    }

    @Test
    @DisplayName("실 참여자 추가 시 Participant와 LocationVote가 함께 저장된다")
    void createLocationVote_participantAdd_savesParticipantWithLocationVote() {
        // given
        CreateLocationVoteRequest request = new CreateLocationVoteRequest(
                "meeting-id-123",
                "1",
                "local-storage-key-abc",  // localStorageKey 존재 → 실 참여자
                "이영희",
                "서울시 왕십리",
                "37.5614080",
                "127.0379670"
        );

        // when
        locationService.createLocationVote(request);

        // then
        ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);
        verify(participantService).save(captor.capture());

        Participant savedParticipant = captor.getValue();
        assertThat(savedParticipant.getName()).isEqualTo("이영희");
        assertThat(savedParticipant.getLocalStorageKey()).isEqualTo("local-storage-key-abc");
        assertThat(savedParticipant.getLocationVotes()).hasSize(1);

        LocationVote locationVote = savedParticipant.getLocationVotes().get(0);
        assertThat(locationVote.getDepartureLocation()).isEqualTo("서울시 왕십리");
        assertThat(locationVote.getDepartureLat()).isEqualByComparingTo(new BigDecimal("37.5614080"));
        assertThat(locationVote.getDepartureLng()).isEqualByComparingTo(new BigDecimal("127.0379670"));

        verify(locationVoteRepository, never()).save(any());
    }

    @Test
    @DisplayName("실 참여자 추가 시 Meeting ID가 올바르게 세팅된다")
    void createLocationVote_participantAdd_setsMeetingId() {
        // given
        CreateLocationVoteRequest request = new CreateLocationVoteRequest(
                "meeting-id-456",
                "1",
                "local-storage-key-xyz",
                "박민수",
                "서울시 서초구",
                "37.4837121",
                "127.0324112"
        );

        // when
        locationService.createLocationVote(request);

        // then
        ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);
        verify(participantService).save(captor.capture());

        Participant savedParticipant = captor.getValue();
        assertThat(savedParticipant.getMeeting().getId()).isEqualTo("meeting-id-456");
    }
}
