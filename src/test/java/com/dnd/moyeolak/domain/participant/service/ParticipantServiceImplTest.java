package com.dnd.moyeolak.domain.participant.service;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.repository.ParticipantRepository;
import com.dnd.moyeolak.domain.participant.service.impl.ParticipantServiceImpl;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceImplTest {

    private static final String LOCAL_STORAGE_KEY = "local-storage-key";

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    @Test
    @DisplayName("참여자 생성 성공 시 저장된 참여자를 반환한다")
    void create_success() {
        Meeting meeting = Meeting.of(10);
        String name = "홍길동";

        when(participantRepository.save(any(Participant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Participant participant = participantService.create(meeting, name, LOCAL_STORAGE_KEY);

        assertThat(participant.getName()).isEqualTo(name);
        assertThat(participant.getLocalStorageKey()).isEqualTo(LOCAL_STORAGE_KEY);
        assertThat(participant.getMeeting()).isEqualTo(meeting);

        ArgumentCaptor<Participant> captor = ArgumentCaptor.forClass(Participant.class);
        verify(participantRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("localStorageKey 중복 검증 시 중복이 없으면 예외를 던지지 않는다")
    void validateLocalStorageKeyUnique_success() {
        Meeting meeting = Meeting.of(10);

        when(participantRepository.existsByMeetingAndLocalStorageKey(meeting, LOCAL_STORAGE_KEY))
                .thenReturn(false);

        participantService.validateLocalStorageKeyUnique(meeting, LOCAL_STORAGE_KEY);

        verify(participantRepository).existsByMeetingAndLocalStorageKey(meeting, LOCAL_STORAGE_KEY);
    }

    @Test
    @DisplayName("localStorageKey 중복 검증 시 중복이 있으면 예외를 던진다")
    void validateLocalStorageKeyUnique_throwsExceptionWhenDuplicate() {
        Meeting meeting = Meeting.of(10);

        when(participantRepository.existsByMeetingAndLocalStorageKey(meeting, LOCAL_STORAGE_KEY))
                .thenReturn(true);

        assertThatThrownBy(() -> participantService.validateLocalStorageKeyUnique(meeting, LOCAL_STORAGE_KEY))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY);
    }
}