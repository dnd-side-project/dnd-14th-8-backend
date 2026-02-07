package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LocationVoteIntegrationTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("수동 추가 시 LocationVote가 DB에 저장된다")
    void createLocationVote_manualAdd_persistsToDb() {
        // given
        String meetingId = createTestMeeting();
        Meeting meeting = meetingRepository.findByIdWithAllAssociations(meetingId).orElseThrow();
        Long locationPollId = meeting.getLocationPoll().getLocationPollId();

        CreateLocationVoteRequest request = new CreateLocationVoteRequest(
                meetingId,
                locationPollId.toString(),
                null,
                "홍길동",
                "서울시 강남구",
                "37.4979502",
                "127.0276368"
        );

        // when
        locationService.createLocationVote(request);
        em.flush();
        em.clear();

        // then
        List<LocationVote> votes = em.createQuery(
                "SELECT lv FROM LocationVote lv WHERE lv.locationPoll.locationPollId = :id", LocationVote.class)
                .setParameter("id", locationPollId)
                .getResultList();

        assertThat(votes).hasSize(1);
        assertThat(votes.get(0).getDepartureName()).isEqualTo("홍길동");
        assertThat(votes.get(0).getDepartureLocation()).isEqualTo("서울시 강남구");
        assertThat(votes.get(0).getDepartureLat()).isEqualByComparingTo(new BigDecimal("37.4979502"));
        assertThat(votes.get(0).getDepartureLng()).isEqualByComparingTo(new BigDecimal("127.0276368"));
        assertThat(votes.get(0).getParticipant()).isNull();
    }

    @Test
    @DisplayName("실 참여자 추가 시 Participant와 LocationVote가 함께 DB에 저장된다")
    void createLocationVote_participantAdd_persistsBothToDb() {
        // given
        String meetingId = createTestMeeting();
        Meeting meeting = meetingRepository.findByIdWithAllAssociations(meetingId).orElseThrow();
        Long locationPollId = meeting.getLocationPoll().getLocationPollId();

        CreateLocationVoteRequest request = new CreateLocationVoteRequest(
                meetingId,
                locationPollId.toString(),
                "local-storage-key-new",
                "김철수",
                "서울시 홍대입구",
                "37.5571010",
                "126.9236450"
        );

        // when
        locationService.createLocationVote(request);
        em.flush();
        em.clear();

        // then - Participant 생성 검증
        List<Participant> participants = em.createQuery(
                "SELECT p FROM Participant p LEFT JOIN FETCH p.locationVotes " +
                "WHERE p.meeting.meetingId = :meetingId AND p.name = :name", Participant.class)
                .setParameter("meetingId", meetingId)
                .setParameter("name", "김철수")
                .getResultList();

        assertThat(participants).hasSize(1);
        assertThat(participants.get(0).getLocalStorageKey()).isEqualTo("local-storage-key-new");

        // then - 해당 Participant의 LocationVote 존재 검증
        List<LocationVote> locationVotes = participants.get(0).getLocationVotes();
        assertThat(locationVotes).hasSize(1);
        assertThat(locationVotes.get(0).getDepartureLocation()).isEqualTo("서울시 홍대입구");
        assertThat(locationVotes.get(0).getDepartureLat()).isEqualByComparingTo(new BigDecimal("37.5571010"));
        assertThat(locationVotes.get(0).getDepartureLng()).isEqualByComparingTo(new BigDecimal("126.9236450"));
    }

    @Test
    @DisplayName("실 참여자 추가 시 Cascade로 LocationVote도 함께 저장된다")
    void createLocationVote_participantAdd_cascadesSaveToLocationVote() {
        // given
        String meetingId = createTestMeeting();
        Meeting meeting = meetingRepository.findByIdWithAllAssociations(meetingId).orElseThrow();
        Long locationPollId = meeting.getLocationPoll().getLocationPollId();

        CreateLocationVoteRequest request = new CreateLocationVoteRequest(
                meetingId,
                locationPollId.toString(),
                "local-storage-key-cascade",
                "이영희",
                "서울시 왕십리",
                "37.5614080",
                "127.0379670"
        );

        // when
        locationService.createLocationVote(request);
        em.flush();
        em.clear();

        // then
        List<LocationVote> votes = em.createQuery(
                "SELECT lv FROM LocationVote lv WHERE lv.locationPoll.locationPollId = :id", LocationVote.class)
                .setParameter("id", locationPollId)
                .getResultList();

        assertThat(votes).hasSize(1);
        assertThat(votes.get(0).getDepartureLocation()).isEqualTo("서울시 왕십리");
        assertThat(votes.get(0).getDepartureLat()).isEqualByComparingTo(new BigDecimal("37.5614080"));
        assertThat(votes.get(0).getDepartureLng()).isEqualByComparingTo(new BigDecimal("127.0379670"));
    }

    @Test
    @DisplayName("수동 추가와 실 참여자 추가를 각각 수행하면 LocationVote가 2개 저장된다")
    void createLocationVote_mixedAdd_persistsBothVotes() {
        // given
        String meetingId = createTestMeeting();
        Meeting meeting = meetingRepository.findByIdWithAllAssociations(meetingId).orElseThrow();
        Long locationPollId = meeting.getLocationPoll().getLocationPollId();

        CreateLocationVoteRequest manualRequest = new CreateLocationVoteRequest(
                meetingId,
                locationPollId.toString(),
                null,
                "수동입력자",
                "서울시 서초구",
                "37.4837121",
                "127.0324112"
        );

        CreateLocationVoteRequest participantRequest = new CreateLocationVoteRequest(
                meetingId,
                locationPollId.toString(),
                "local-storage-key-mix",
                "박민수",
                "서울시 부평",
                "37.5074100",
                "126.7218400"
        );

        // when
        locationService.createLocationVote(manualRequest);
        locationService.createLocationVote(participantRequest);
        em.flush();
        em.clear();

        // then
        List<LocationVote> votes = em.createQuery(
                "SELECT lv FROM LocationVote lv WHERE lv.locationPoll.locationPollId = :id", LocationVote.class)
                .setParameter("id", locationPollId)
                .getResultList();

        assertThat(votes).hasSize(2);
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
