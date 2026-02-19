package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.global.enums.PollStatus;
import com.dnd.moyeolak.global.exception.BusinessException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
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
class SchedulePollServiceIntegrationTest {

    @Autowired
    private SchedulePollService schedulePollService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("일정 투표 확정 시 상태가 CONFIRMED로 변경된다")
    void confirmSchedulePoll_changesStatusToConfirmed() {
        // given
        String meetingId = createTestMeeting();
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow();

        // when
        schedulePollService.confirmSchedulePoll(meetingId);
        em.flush();
        em.clear();

        // then
        SchedulePoll confirmed = em.find(SchedulePoll.class, meeting.getSchedulePoll().getId());
        assertThat(confirmed.getPollStatus()).isEqualTo(PollStatus.CONFIRMED);
    }

    @Test
    @DisplayName("존재하지 않는 일정 투표판 확정 시 예외가 발생한다")
    void confirmSchedulePoll_throwsException_whenNotFound() {
        // given
        String nonMeetingId = "sadasdeikn";

        // when & then
        assertThatThrownBy(() -> schedulePollService.confirmSchedulePoll(nonMeetingId))
                .isInstanceOf(BusinessException.class);
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
