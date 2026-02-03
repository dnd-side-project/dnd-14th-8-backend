package com.dnd.moyeolak.domain.meeting.service;

import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.dto.GetMeetingScheduleResponse;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Nested
    @DisplayName("모임 일정 조회")
    class GetMeetingSchedules {

        @Test
        @DisplayName("생성된 모임의 일정 정보를 조회할 수 있다")
        void getMeetingSchedules_returnsScheduleInfo() {
            // given
            CreateMeetingRequest request = new CreateMeetingRequest(
                    5,
                    "local-storage-key-123",
                    "홍길동"
            );
            String meetingId = meetingService.createMeeting(request);

            // when
            GetMeetingScheduleResponse response = meetingService.getMeetingSchedules(meetingId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.meetingId()).isEqualTo(meetingId);
        }

        @Test
        @DisplayName("조회된 모임에 참가자 정보가 포함된다")
        void getMeetingSchedules_includesParticipants() {
            // given
            String participantName = "홍길동";
            CreateMeetingRequest request = new CreateMeetingRequest(
                    5,
                    "local-storage-key-123",
                    participantName
            );
            String meetingId = meetingService.createMeeting(request);

            // when
            GetMeetingScheduleResponse response = meetingService.getMeetingSchedules(meetingId);

            // then
            assertThat(response.participants()).hasSize(1);
            assertThat(response.participants().get(0).name()).isEqualTo(participantName);
        }

        @Test
        @DisplayName("조회된 모임에 일정 투표 설정 정보가 포함된다")
        void getMeetingSchedules_includesSchedulePollSettings() {
            // given
            CreateMeetingRequest request = new CreateMeetingRequest(
                    5,
                    "local-storage-key-123",
                    "홍길동"
            );
            String meetingId = meetingService.createMeeting(request);

            // when
            GetMeetingScheduleResponse response = meetingService.getMeetingSchedules(meetingId);

            // then
            assertThat(response.dateOptions()).isNotEmpty();
            assertThat(response.startTime()).isEqualTo(7);
            assertThat(response.endTime()).isEqualTo(24);
        }

        @Test
        @DisplayName("조회된 모임에 참가자 수와 투표한 참가자 수가 포함된다")
        void getMeetingSchedules_includesParticipantCounts() {
            // given
            CreateMeetingRequest request = new CreateMeetingRequest(
                    5,
                    "local-storage-key-123",
                    "홍길동"
            );
            String meetingId = meetingService.createMeeting(request);

            // when
            GetMeetingScheduleResponse response = meetingService.getMeetingSchedules(meetingId);

            // then
            assertThat(response.participantCount()).isEqualTo(5);
            assertThat(response.votedParticipantCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("존재하지 않는 모임 조회 시 예외가 발생한다")
        void getMeetingSchedules_throwsException_whenMeetingNotFound() {
            // given
            String nonExistentMeetingId = "non-existent-id";

            // when & then
            assertThatThrownBy(() -> meetingService.getMeetingSchedules(nonExistentMeetingId))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
