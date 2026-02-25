package com.dnd.moyeolak.domain.schedule.service;

import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.schedule.dto.CreateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.dto.UpdateSchedulePollRequest;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.global.enums.PollStatus;
import com.dnd.moyeolak.global.exception.BusinessException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SchedulePollServiceIntegrationTest {

    @Autowired
    private SchedulePollService schedulePollService;

    @Autowired
    private ScheduleVoteService scheduleVoteService;

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

    @Test
    @DisplayName("updateSchedulePoll - 날짜 범위 축소 시 범위 밖 투표 항목이 제거된다")
    void updateSchedulePoll_removesVotesOutsideDateRange() {
        // given
        String meetingId = createTestMeeting();
        // defaultOf: dateOptions = today ~ today+13

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1); // 새 dateOptions에서 제외될 날짜

        Long voteId = scheduleVoteService.createParticipantVote(meetingId, new CreateScheduleVoteRequest(
                "홍길동", "key-1", List.of(
                        today.atTime(9, 0),    // 유지
                        tomorrow.atTime(9, 0)  // 날짜 범위 밖 → 제거
                )
        ));
        em.flush();
        em.clear();

        // 오늘 날짜만 허용하도록 축소
        UpdateSchedulePollRequest request = new UpdateSchedulePollRequest(
                List.of(today), "07:00", "24:00"
        );

        // when
        schedulePollService.updateSchedulePoll(meetingId, request);
        em.flush();
        em.clear();

        // then
        ScheduleVote vote = em.find(ScheduleVote.class, voteId);
        assertThat(vote.getVotedDate()).containsExactly(today.atTime(9, 0));
        assertThat(vote.getVotedDate()).doesNotContain(tomorrow.atTime(9, 0));
    }

    @Test
    @DisplayName("updateSchedulePoll - 모든 투표가 무효화되어도 비호스트 참여자는 삭제되지 않는다")
    void updateSchedulePoll_doesNotDeleteNonHostParticipantWhenAllVotesInvalidated() {
        // given
        String meetingId = createTestMeeting();

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // 비호스트 참여자 생성 (내일 날짜로 투표)
        scheduleVoteService.createParticipantVote(meetingId, new CreateScheduleVoteRequest(
                "일반참여자", "participant-key", List.of(tomorrow.atTime(9, 0))
        ));
        em.flush();
        em.clear();

        int participantCountBefore = meetingRepository.findByIdWithAllAssociations(meetingId)
                .orElseThrow().getParticipants().size();
        em.clear();

        // 오늘 날짜만 허용하도록 축소 → 비호스트의 모든 투표가 무효화됨
        UpdateSchedulePollRequest request = new UpdateSchedulePollRequest(
                List.of(today), "07:00", "24:00"
        );

        // when
        schedulePollService.updateSchedulePoll(meetingId, request);
        em.flush();
        em.clear();

        // then
        Meeting meeting = meetingRepository.findByIdWithAllAssociations(meetingId).orElseThrow();
        assertThat(meeting.getParticipants()).hasSize(participantCountBefore);
        assertThat(meeting.getParticipants())
                .anyMatch(p -> p.getLocalStorageKey().equals("participant-key"));
    }

    @Test
    @DisplayName("updateSchedulePoll - 시간 범위 축소 시 범위 밖 투표 항목이 제거된다")
    void updateSchedulePoll_removesVotesOutsideTimeRange() {
        // given
        String meetingId = createTestMeeting();

        LocalDate today = LocalDate.now();
        Long voteId = scheduleVoteService.createParticipantVote(meetingId, new CreateScheduleVoteRequest(
                "홍길동", "key-1", List.of(
                        today.atTime(8, 0),   // 새 startTime(09:00) 이전 → 제거
                        today.atTime(9, 0),   // 유지
                        today.atTime(11, 30), // 유지
                        today.atTime(12, 0)   // 새 endTime(12:00) 경계값 → 제거
                )
        ));
        em.flush();
        em.clear();

        // 시간 범위를 09:00~12:00으로 축소
        UpdateSchedulePollRequest request = new UpdateSchedulePollRequest(
                List.of(today), "09:00", "12:00"
        );

        // when
        schedulePollService.updateSchedulePoll(meetingId, request);
        em.flush();
        em.clear();

        // then
        ScheduleVote vote = em.find(ScheduleVote.class, voteId);
        assertThat(vote.getVotedDate()).containsExactlyInAnyOrder(
                today.atTime(9, 0),
                today.atTime(11, 30)
        );
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
