package com.dnd.moyeolak.domain.meeting.service;

import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MeetingServiceIntegrationTest {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Test
    @DisplayName("모임 생성 시 meetingId가 반환된다")
    void createMeeting_returnsMeetingId() {
        // given
        CreateMeetingRequest request = new CreateMeetingRequest(
                5,
                "local-storage-key-123",
                "홍길동"
        );

        // when
        String meetingId = meetingService.createMeeting(request);

        // then
        assertThat(meetingId).isNotNull();
        assertThat(meetingId).isNotEmpty();
    }

    @Test
    @DisplayName("모임 생성 시 참가자 수가 올바르게 저장된다")
    void createMeeting_savesParticipantCount() {
        // given
        int expectedParticipantCount = 5;
        CreateMeetingRequest request = new CreateMeetingRequest(
                expectedParticipantCount,
                "local-storage-key-123",
                "홍길동"
        );

        // when
        String meetingId = meetingService.createMeeting(request);

        // then
        Meeting saved = meetingRepository.findById(meetingId).orElseThrow();
        assertThat(saved.getParticipantCount()).isEqualTo(expectedParticipantCount);
    }

    @Test
    @DisplayName("모임 생성 시 참가자가 추가된다")
    void createMeeting_addsParticipant() {
        // given
        String expectedName = "홍길동";
        String expectedLocalStorageKey = "local-storage-key-123";
        CreateMeetingRequest request = new CreateMeetingRequest(
                5,
                expectedLocalStorageKey,
                expectedName
        );

        // when
        String meetingId = meetingService.createMeeting(request);

        // then
        Meeting saved = meetingRepository.findById(meetingId).orElseThrow();
        assertThat(saved.getParticipants()).hasSize(1);
        assertThat(saved.getParticipants().getFirst().getName()).isEqualTo(expectedName);
        assertThat(saved.getParticipants().getFirst().getLocalStorageKey()).isEqualTo(expectedLocalStorageKey);
    }

    @Test
    @DisplayName("모임 생성 시 일정 투표가 생성된다")
    void createMeeting_createsSchedulePoll() {
        // given
        CreateMeetingRequest request = new CreateMeetingRequest(
                5,
                "local-storage-key-123",
                "홍길동"
        );

        // when
        String meetingId = meetingService.createMeeting(request);

        // then
        Meeting saved = meetingRepository.findById(meetingId).orElseThrow();
        assertThat(saved.getSchedulePoll()).isNotNull();
    }

    @Test
    @DisplayName("모임 생성 시 장소 투표가 생성된다")
    void createMeeting_createsLocationPoll() {
        // given
        CreateMeetingRequest request = new CreateMeetingRequest(
                5,
                "local-storage-key-123",
                "홍길동"
        );

        // when
        String meetingId = meetingService.createMeeting(request);

        // then
        Meeting saved = meetingRepository.findById(meetingId).orElseThrow();
        assertThat(saved.getLocationPoll()).isNotNull();
    }
}
