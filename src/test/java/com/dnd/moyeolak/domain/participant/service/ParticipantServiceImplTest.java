package com.dnd.moyeolak.domain.participant.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.repository.ParticipantRepository;
import com.dnd.moyeolak.domain.participant.service.impl.ParticipantServiceImpl;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceImplTest {

    private static final String MEETING_ID = "meeting-id";
    private static final String LOCAL_STORAGE_KEY = "local-storage-key";

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    @Test
    @DisplayName("참여자 전체 조회 성공 시 참여자 목록을 반환한다")
    void listParticipants_success() {
        // Given
        Meeting meeting = Meeting.of(10);
        Participant host = Participant.hostOf(meeting, "host-key", "김방장");
        Participant member = Participant.of(meeting, "member-key", "이참여");
        meeting.addParticipant(host);
        meeting.addParticipant(member);

        when(meetingRepository.findByIdWithAllAssociations(MEETING_ID)).thenReturn(Optional.of(meeting));

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
        when(meetingRepository.findByIdWithAllAssociations(MEETING_ID)).thenReturn(Optional.of(meeting));

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
        when(meetingRepository.findByIdWithAllAssociations(MEETING_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> participantService.listParticipants(MEETING_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEETING_NOT_FOUND);
    }

}
