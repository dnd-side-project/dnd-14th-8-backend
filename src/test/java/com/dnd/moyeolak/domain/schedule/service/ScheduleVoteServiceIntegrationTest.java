package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.schedule.dto.CreateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScheduleVoteServiceIntegrationTest {

    @Autowired
    private ScheduleVoteService scheduleVoteService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("일정 투표 생성 시 ScheduleVote ID가 반환된다")
    void createParticipantVote_returnsScheduleVoteId() {
        // given
        String meetingId = createTestMeeting();

        LocalDate today = LocalDate.now();
        List<LocalDateTime> votedDates = List.of(
                today.atTime(9, 0),
                today.atTime(9, 30)
        );

        CreateScheduleVoteRequest request = new CreateScheduleVoteRequest(
                "김철수", "new-local-key", votedDates, true
        );

        // when
        Long scheduleVoteId = scheduleVoteService.createParticipantVote(meetingId, request);

        // then
        assertThat(scheduleVoteId).isNotNull();

        ScheduleVote saved = em.find(ScheduleVote.class, scheduleVoteId);
        assertThat(saved).isNotNull();
        assertThat(saved.getVotedDate()).isEqualTo(votedDates);
    }

    private String createTestMeeting() {
        CreateMeetingRequest request = new CreateMeetingRequest(
                5,
                "local-storage-key-123",
                "방장"
        );
        String meetingId = meetingService.createMeeting(request);
        em.flush();
        em.clear();
        return meetingId;
    }
}
