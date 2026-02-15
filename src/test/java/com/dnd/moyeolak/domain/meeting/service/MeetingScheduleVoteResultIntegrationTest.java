package com.dnd.moyeolak.domain.meeting.service;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.meeting.dto.GetMeetingScheduleVoteResultResponse;
import com.dnd.moyeolak.domain.meeting.dto.GetMeetingScheduleVoteResultResponse.ScheduleVoteResult;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
class MeetingScheduleVoteResultIntegrationTest {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private EntityManager em;

    private Meeting meeting;

    // 참가자 이름 (data.sql 기준)
    private static final String 김민준 = "김민준";
    private static final String 이서연 = "이서연";
    private static final String 박도윤 = "박도윤";
    private static final String 최하은 = "최하은";
    private static final String 백도현 = "백도현";
    private static final String 홍길동 = "홍길동";
    private static final String 백무식 = "백무식";
    private static final String 차은지 = "차은지";
    private static final String 강재현 = "강재현"; // 미투표

    @BeforeEach
    void setUp() {
        // 모임 생성 (정원 10명, 9명 참가 — 1명 미참가)
        meeting = Meeting.of(10);
        meeting.addPolls(SchedulePoll.defaultOf(meeting), LocationPoll.defaultOf(meeting));

        // 참가자 9명 추가 (8명 투표, 1명 미투표)
        Participant p1 = Participant.hostOf(meeting, "key-1", 김민준);
        Participant p2 = Participant.of(meeting, "key-2", 이서연);
        Participant p3 = Participant.of(meeting, "key-3", 박도윤);
        Participant p4 = Participant.of(meeting, "key-4", 최하은);
        Participant p5 = Participant.of(meeting, "key-5", 백도현);
        Participant p6 = Participant.of(meeting, "key-6", 홍길동);
        Participant p7 = Participant.of(meeting, "key-7", 백무식);
        Participant p8 = Participant.of(meeting, "key-8", 차은지);
        Participant p9 = Participant.of(meeting, "key-9", 강재현);

        meeting.addParticipant(p1);
        meeting.addParticipant(p2);
        meeting.addParticipant(p3);
        meeting.addParticipant(p4);
        meeting.addParticipant(p5);
        meeting.addParticipant(p6);
        meeting.addParticipant(p7);
        meeting.addParticipant(p8);
        meeting.addParticipant(p9);

        meetingRepository.save(meeting);

        SchedulePoll schedulePoll = meeting.getSchedulePoll();

        // data.sql 기준 투표 데이터 삽입 (8명 투표, 1명 미투표)
        // 1번 김민준 - 수요일·목요일 저녁 + 주말 오후~저녁
        createVote(schedulePoll, p1, List.of(
                dt(2025, 2, 5, 19, 0), dt(2025, 2, 5, 19, 30), dt(2025, 2, 5, 20, 0),
                dt(2025, 2, 6, 19, 0), dt(2025, 2, 6, 19, 30), dt(2025, 2, 6, 20, 0),
                dt(2025, 2, 8, 14, 0), dt(2025, 2, 8, 14, 30), dt(2025, 2, 8, 15, 0),
                dt(2025, 2, 8, 18, 0), dt(2025, 2, 8, 18, 30), dt(2025, 2, 8, 19, 0), dt(2025, 2, 8, 19, 30),
                dt(2025, 2, 9, 14, 0), dt(2025, 2, 9, 14, 30), dt(2025, 2, 9, 15, 0), dt(2025, 2, 9, 15, 30),
                dt(2025, 2, 12, 19, 0), dt(2025, 2, 12, 19, 30), dt(2025, 2, 12, 20, 0)
        ));

        // 2번 이서연 - 화요일·수요일 저녁 + 토요일 저녁, 일요일 오후
        createVote(schedulePoll, p2, List.of(
                dt(2025, 2, 5, 19, 0), dt(2025, 2, 5, 19, 30), dt(2025, 2, 5, 20, 0), dt(2025, 2, 5, 20, 30),
                dt(2025, 2, 11, 19, 30), dt(2025, 2, 11, 20, 0), dt(2025, 2, 11, 20, 30),
                dt(2025, 2, 8, 17, 0), dt(2025, 2, 8, 17, 30), dt(2025, 2, 8, 18, 0), dt(2025, 2, 8, 18, 30),
                dt(2025, 2, 8, 19, 0), dt(2025, 2, 8, 19, 30), dt(2025, 2, 8, 20, 0),
                dt(2025, 2, 9, 13, 0), dt(2025, 2, 9, 13, 30), dt(2025, 2, 9, 14, 0), dt(2025, 2, 9, 14, 30),
                dt(2025, 2, 9, 15, 0)
        ));

        // 3번 박도윤 - 수요일·금요일 저녁 + 토요일 저녁 집중
        createVote(schedulePoll, p3, List.of(
                dt(2025, 2, 5, 18, 30), dt(2025, 2, 5, 19, 0), dt(2025, 2, 5, 19, 30),
                dt(2025, 2, 7, 19, 0), dt(2025, 2, 7, 19, 30), dt(2025, 2, 7, 20, 0),
                dt(2025, 2, 12, 19, 0), dt(2025, 2, 12, 19, 30), dt(2025, 2, 12, 20, 0),
                dt(2025, 2, 8, 18, 0), dt(2025, 2, 8, 18, 30), dt(2025, 2, 8, 19, 0), dt(2025, 2, 8, 19, 30),
                dt(2025, 2, 8, 20, 0), dt(2025, 2, 8, 20, 30),
                dt(2025, 2, 9, 14, 0), dt(2025, 2, 9, 14, 30)
        ));

        // 4번 최하은 - 목요일 저녁 + 주말 넓게 투표
        createVote(schedulePoll, p4, List.of(
                dt(2025, 2, 6, 18, 30), dt(2025, 2, 6, 19, 0), dt(2025, 2, 6, 19, 30), dt(2025, 2, 6, 20, 0),
                dt(2025, 2, 13, 19, 0), dt(2025, 2, 13, 19, 30), dt(2025, 2, 13, 20, 0),
                dt(2025, 2, 8, 12, 0), dt(2025, 2, 8, 12, 30), dt(2025, 2, 8, 13, 0),
                dt(2025, 2, 8, 18, 0), dt(2025, 2, 8, 18, 30), dt(2025, 2, 8, 19, 0),
                dt(2025, 2, 9, 14, 0), dt(2025, 2, 9, 14, 30), dt(2025, 2, 9, 15, 0), dt(2025, 2, 9, 15, 30),
                dt(2025, 2, 9, 16, 0), dt(2025, 2, 9, 16, 30)
        ));

        // 5번 백도현 - 월요일·수요일 저녁 + 토요일 저녁 강하게
        createVote(schedulePoll, p5, List.of(
                dt(2025, 2, 10, 19, 0), dt(2025, 2, 10, 19, 30), dt(2025, 2, 10, 20, 0),
                dt(2025, 2, 5, 19, 0), dt(2025, 2, 5, 19, 30),
                dt(2025, 2, 12, 19, 0), dt(2025, 2, 12, 19, 30), dt(2025, 2, 12, 20, 0),
                dt(2025, 2, 8, 17, 30), dt(2025, 2, 8, 18, 0), dt(2025, 2, 8, 18, 30),
                dt(2025, 2, 8, 19, 0), dt(2025, 2, 8, 19, 30), dt(2025, 2, 8, 20, 0),
                dt(2025, 2, 8, 20, 30), dt(2025, 2, 8, 21, 0)
        ));

        // 6번 홍길동 - 화요일·목요일 저녁 + 일요일 오후 집중, 토요일 저녁 일부
        createVote(schedulePoll, p6, List.of(
                dt(2025, 2, 11, 19, 0), dt(2025, 2, 11, 19, 30), dt(2025, 2, 11, 20, 0),
                dt(2025, 2, 6, 19, 0), dt(2025, 2, 6, 19, 30), dt(2025, 2, 6, 20, 0),
                dt(2025, 2, 13, 19, 0), dt(2025, 2, 13, 19, 30),
                dt(2025, 2, 8, 18, 30), dt(2025, 2, 8, 19, 0), dt(2025, 2, 8, 19, 30),
                dt(2025, 2, 9, 12, 0), dt(2025, 2, 9, 12, 30), dt(2025, 2, 9, 13, 0), dt(2025, 2, 9, 13, 30),
                dt(2025, 2, 9, 14, 0), dt(2025, 2, 9, 14, 30), dt(2025, 2, 9, 15, 0), dt(2025, 2, 9, 15, 30)
        ));

        // 7번 백무식 - 수요일·금요일 저녁 + 금토 저녁
        createVote(schedulePoll, p7, List.of(
                dt(2025, 2, 5, 19, 0), dt(2025, 2, 5, 19, 30), dt(2025, 2, 5, 20, 0), dt(2025, 2, 5, 20, 30),
                dt(2025, 2, 7, 18, 0), dt(2025, 2, 7, 18, 30), dt(2025, 2, 7, 19, 0),
                dt(2025, 2, 12, 18, 30), dt(2025, 2, 12, 19, 0), dt(2025, 2, 12, 19, 30),
                dt(2025, 2, 14, 19, 0), dt(2025, 2, 14, 19, 30), dt(2025, 2, 14, 20, 0),
                dt(2025, 2, 8, 18, 0), dt(2025, 2, 8, 18, 30), dt(2025, 2, 8, 19, 0), dt(2025, 2, 8, 19, 30),
                dt(2025, 2, 8, 20, 0),
                dt(2025, 2, 9, 15, 0), dt(2025, 2, 9, 15, 30)
        ));

        // 8번 차은지 - 목요일·금요일 저녁 + 토요일 오후~저녁 넓게
        createVote(schedulePoll, p8, List.of(
                dt(2025, 2, 6, 19, 0), dt(2025, 2, 6, 19, 30), dt(2025, 2, 6, 20, 0),
                dt(2025, 2, 7, 19, 0), dt(2025, 2, 7, 19, 30), dt(2025, 2, 7, 20, 0),
                dt(2025, 2, 13, 19, 0), dt(2025, 2, 13, 19, 30), dt(2025, 2, 13, 20, 0),
                dt(2025, 2, 8, 15, 0), dt(2025, 2, 8, 15, 30), dt(2025, 2, 8, 16, 0), dt(2025, 2, 8, 16, 30),
                dt(2025, 2, 8, 17, 0), dt(2025, 2, 8, 17, 30), dt(2025, 2, 8, 18, 0), dt(2025, 2, 8, 18, 30),
                dt(2025, 2, 8, 19, 0),
                dt(2025, 2, 9, 14, 0), dt(2025, 2, 9, 14, 30), dt(2025, 2, 9, 15, 0)
        ));

        // 9번 강재현 - 미투표

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("일정 투표 결과 조회")
    class GetScheduleVoteResults {

        private GetMeetingScheduleVoteResultResponse getResponse() {
            return meetingService.getMeetingScheduleVoteResults(meeting.getId());
        }

        @Test
        @DisplayName("투표 결과가 정상적으로 조회된다")
        void returnsVoteResults() {
            // when
            GetMeetingScheduleVoteResultResponse response = getResponse();

            // then
            assertThat(response).isNotNull();
            assertThat(response.participantCount()).isEqualTo(10);
            assertThat(response.scheduleVoteResult()).isNotEmpty();
        }

        @Test
        @DisplayName("1순위는 2/8 토요일 18:30~19:30 (8명, 투표자 전원)")
        void firstRank_is_saturday_1830_to_1930_with_8_voters() {
            // when
            GetMeetingScheduleVoteResultResponse response = getResponse();
            ScheduleVoteResult first = response.scheduleVoteResult().getFirst();

            // then
            assertThat(first.scheduleDate()).isEqualTo(LocalDate.of(2025, 2, 8));
            assertThat(first.scheduleDayOfWeek()).isEqualTo("토요일");
            assertThat(first.startTime()).isEqualTo("18:30");
            assertThat(first.endTime()).isEqualTo("19:30");
            assertThat(first.voteCount()).isEqualTo(8);
            assertThat(first.availableParticipantNames())
                    .containsExactlyInAnyOrder(김민준, 이서연, 박도윤, 최하은, 백도현, 홍길동, 백무식, 차은지);
            assertThat(first.unavailableParticipantNames())
                    .containsExactlyInAnyOrder(강재현);
        }

        @Test
        @DisplayName("2순위는 2/9 일요일 14:00~15:00 (6명)")
        void secondRank_is_sunday_1400_to_1500_with_6_voters() {
            // when
            GetMeetingScheduleVoteResultResponse response = getResponse();
            ScheduleVoteResult second = response.scheduleVoteResult().get(1);

            // then
            assertThat(second.scheduleDate()).isEqualTo(LocalDate.of(2025, 2, 9));
            assertThat(second.startTime()).isEqualTo("14:00");
            assertThat(second.endTime()).isEqualTo("15:00");
            assertThat(second.voteCount()).isEqualTo(6);
            assertThat(second.availableParticipantNames())
                    .containsExactlyInAnyOrder(김민준, 이서연, 박도윤, 최하은, 홍길동, 차은지);
        }

        @Test
        @DisplayName("6명 투표 결과는 최소 1시간 이상인 범위만 포함된다")
        void sixVoterResults_onlyHourOrLonger() {
            // when
            GetMeetingScheduleVoteResultResponse response = getResponse();
            List<ScheduleVoteResult> results = response.scheduleVoteResult();

            // 6명 결과 필터 - 30분짜리(19:30~20:00, 15:00~15:30) 제거 후 1개만 남음
            List<ScheduleVoteResult> sixVoterResults = results.stream()
                    .filter(r -> r.voteCount() == 6)
                    .toList();

            // then - 2/9 14:00~15:00 (1시간)만 남음
            assertThat(sixVoterResults).hasSize(1);
            assertThat(sixVoterResults.getFirst().scheduleDate()).isEqualTo(LocalDate.of(2025, 2, 9));
            assertThat(sixVoterResults.getFirst().startTime()).isEqualTo("14:00");
            assertThat(sixVoterResults.getFirst().endTime()).isEqualTo("15:00");
        }

        @Test
        @DisplayName("연속된 같은 투표자의 슬롯이 하나의 범위로 병합된다")
        void consecutiveSlots_withSameVoters_areMerged() {
            // when
            GetMeetingScheduleVoteResultResponse response = getResponse();
            List<ScheduleVoteResult> results = response.scheduleVoteResult();

            // then - 2/5 19:00~20:00 (5명: 김민준, 이서연, 박도윤, 백도현, 백무식)
            // 19:00과 19:30이 같은 투표자이므로 하나의 범위 19:00~20:00으로 병합
            ScheduleVoteResult merged = results.stream()
                    .filter(r -> r.scheduleDate().equals(LocalDate.of(2025, 2, 5))
                            && r.startTime().equals("19:00")
                            && r.voteCount() == 5)
                    .findFirst()
                    .orElseThrow();

            assertThat(merged.endTime()).isEqualTo("20:00");
            assertThat(merged.availableParticipantNames())
                    .containsExactlyInAnyOrder(김민준, 이서연, 박도윤, 백도현, 백무식);
        }

        @Test
        @DisplayName("투표자가 달라지면 연속 슬롯이라도 별도 범위로 분리되고, 30분 미만은 제거된다")
        void consecutiveSlots_withDifferentVoters_areSeparated_andShortRangesRemoved() {
            // when
            GetMeetingScheduleVoteResultResponse response = getResponse();
            List<ScheduleVoteResult> results = response.scheduleVoteResult();

            // then - 2/5에는 19:00~20:00(5명, 1시간)만 남고, 20:00~20:30(3명), 20:30~21:00(2명)은 30분이라 제거
            List<ScheduleVoteResult> feb5Results = results.stream()
                    .filter(r -> r.scheduleDate().equals(LocalDate.of(2025, 2, 5)))
                    .toList();

            assertThat(feb5Results).hasSize(1);
            assertThat(feb5Results.getFirst().voteCount()).isEqualTo(5);
            assertThat(feb5Results.getFirst().startTime()).isEqualTo("19:00");
            assertThat(feb5Results.getFirst().endTime()).isEqualTo("20:00");
        }

        @Test
        @DisplayName("2명 미만 슬롯은 결과에 포함되지 않는다")
        void slotsWithLessThan2Voters_areExcluded() {
            // when
            GetMeetingScheduleVoteResultResponse response = getResponse();
            List<ScheduleVoteResult> results = response.scheduleVoteResult();

            // then - 모든 결과의 voteCount가 2 이상
            assertThat(results).allSatisfy(r ->
                    assertThat(r.voteCount()).isGreaterThanOrEqualTo(2)
            );

            // 2/10은 백도현 혼자만 투표 → 결과에 없어야 함
            assertThat(results.stream()
                    .filter(r -> r.scheduleDate().equals(LocalDate.of(2025, 2, 10)))
                    .toList()
            ).isEmpty();
        }

        @Test
        @DisplayName("미투표 참가자는 모든 결과에서 불참자로 표시된다")
        void nonVoters_areAlwaysUnavailable() {
            // when
            GetMeetingScheduleVoteResultResponse response = getResponse();
            List<ScheduleVoteResult> results = response.scheduleVoteResult();

            // then - 강재현은 모든 결과에서 unavailable
            assertThat(results).allSatisfy(r ->
                    assertThat(r.unavailableParticipantNames()).contains(강재현)
            );
        }

        @Test
        @DisplayName("전체 결과 수가 올바르게 반환된다")
        void resultCount_matchesActualResults() {
            // when
            GetMeetingScheduleVoteResultResponse response = getResponse();

            // then
            assertThat(response.resultCount()).isEqualTo(response.scheduleVoteResult().size());
        }
    }

    private void createVote(SchedulePoll schedulePoll, Participant participant, List<LocalDateTime> votedDates) {
        ScheduleVote vote = ScheduleVote.of(participant, schedulePoll, votedDates);
        vote.assignParticipant(participant);
        em.persist(vote);
    }

    private static LocalDateTime dt(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(year, month, day, hour, minute);
    }
}
