package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.dto.MidpointRecommendationResponse;
import com.dnd.moyeolak.domain.location.dto.StationRecommendationDto;
import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.repository.LocationVoteRepository;
import com.dnd.moyeolak.domain.location.service.impl.MidpointRecommendationServiceImpl;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.global.client.google.GoogleDistanceMatrixClient;
import com.dnd.moyeolak.global.client.google.dto.GoogleDistanceMatrixResponse;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import com.dnd.moyeolak.global.station.entity.Station;
import com.dnd.moyeolak.global.station.repository.StationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MidpointRecommendationServiceImplTest {

    @Mock
    private MeetingService meetingService;

    @Mock
    private LocationVoteRepository locationVoteRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private GoogleDistanceMatrixClient googleDistanceMatrixClient;

    @InjectMocks
    private MidpointRecommendationServiceImpl midpointRecommendationService;

    private static final String MEETING_ID = "meeting-123";

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("LocationPoll이 null이면 LOCATION_POLL_NOT_FOUND 예외가 발생한다")
        void throwsWhenLocationPollIsNull() {
            // given
            Meeting meeting = mock(Meeting.class);
            when(meetingService.get(MEETING_ID)).thenReturn(meeting);
            when(meeting.getLocationPoll()).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> midpointRecommendationService.calculateMidpointRecommendations(MEETING_ID, null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOCATION_POLL_NOT_FOUND);
        }

        @Test
        @DisplayName("출발지 투표가 없으면 NO_LOCATION_VOTES 예외가 발생한다")
        void throwsWhenNoLocationVotes() {
            // given
            Meeting meeting = mock(Meeting.class);
            LocationPoll locationPoll = mock(LocationPoll.class);
            when(meetingService.get(MEETING_ID)).thenReturn(meeting);
            when(meeting.getLocationPoll()).thenReturn(locationPoll);
            when(locationPoll.getId()).thenReturn(1L);
            when(locationVoteRepository.findByLocationPoll_Id(1L)).thenReturn(Collections.emptyList());

            // when & then
            assertThatThrownBy(() -> midpointRecommendationService.calculateMidpointRecommendations(MEETING_ID, null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_LOCATION_VOTES);
        }

        @Test
        @DisplayName("근처 지하철역이 없으면 NO_NEARBY_STATIONS 예외가 발생한다")
        void throwsWhenNoNearbyStations() {
            // given
            Meeting meeting = mock(Meeting.class);
            LocationPoll locationPoll = mock(LocationPoll.class);
            when(meetingService.get(MEETING_ID)).thenReturn(meeting);
            when(meeting.getLocationPoll()).thenReturn(locationPoll);
            when(locationPoll.getId()).thenReturn(1L);

            LocationVote vote = createMockVote("37.5000", "127.0000", "테스터", "서울시 강남구");
            when(locationVoteRepository.findByLocationPoll_Id(1L)).thenReturn(List.of(vote));

            // PostGIS 무게중심 계산 실패 → 산술 평균으로 대체
            lenient().when(stationRepository.calculateCentroid(any())).thenThrow(new RuntimeException("PostGIS 미지원"));
            when(stationRepository.findNearbyStations(anyDouble(), anyDouble(), anyInt(), anyInt()))
                    .thenReturn(Collections.emptyList());

            // when & then
            assertThatThrownBy(() -> midpointRecommendationService.calculateMidpointRecommendations(MEETING_ID, null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_NEARBY_STATIONS);
        }

        @Test
        @DisplayName("Google API 응답이 모두 null이면 GOOGLE_API_ERROR 예외가 발생한다")
        void throwsWhenGoogleApiReturnsNull() {
            // given
            Meeting meeting = mock(Meeting.class);
            LocationPoll locationPoll = mock(LocationPoll.class);
            when(meetingService.get(MEETING_ID)).thenReturn(meeting);
            when(meeting.getLocationPoll()).thenReturn(locationPoll);
            when(locationPoll.getId()).thenReturn(1L);

            LocationVote vote = createMockVote("37.5000", "127.0000", "테스터", "서울시 강남구");
            when(locationVoteRepository.findByLocationPoll_Id(1L)).thenReturn(List.of(vote));

            lenient().when(stationRepository.calculateCentroid(any())).thenThrow(new RuntimeException("PostGIS 미지원"));

            Station station = createMockStation("강남역", "2호선", 37.4979, 127.0276);
            when(stationRepository.findNearbyStations(anyDouble(), anyDouble(), anyInt(), anyInt()))
                    .thenReturn(List.of(station));

            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("transit"), isNull()))
                    .thenReturn(null);
            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("driving"), isNull()))
                    .thenReturn(null);

            // when & then
            assertThatThrownBy(() -> midpointRecommendationService.calculateMidpointRecommendations(MEETING_ID, null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GOOGLE_API_ERROR);
        }
    }

    @Nested
    @DisplayName("정상 케이스")
    class SuccessCases {

        @Test
        @DisplayName("출발지 2개, 후보역 1개일 때 추천 결과가 정상 반환된다")
        void returnsRecommendationWithSingleStation() {
            // given
            Meeting meeting = mock(Meeting.class);
            LocationPoll locationPoll = mock(LocationPoll.class);
            when(meetingService.get(MEETING_ID)).thenReturn(meeting);
            when(meeting.getLocationPoll()).thenReturn(locationPoll);
            when(locationPoll.getId()).thenReturn(1L);

            LocationVote vote1 = createMockVote("37.5000", "127.0000", "참가자A", "서울시 강남구");
            LocationVote vote2 = createMockVote("37.5500", "126.9500", "참가자B", "서울시 마포구");
            when(locationVoteRepository.findByLocationPoll_Id(1L)).thenReturn(List.of(vote1, vote2));

            when(stationRepository.calculateCentroid(any())).thenThrow(new RuntimeException("PostGIS 미지원"));

            Station station = createMockStation("합정역", "2호선", 37.5495, 126.9137);
            when(stationRepository.findNearbyStations(anyDouble(), anyDouble(), anyInt(), anyInt()))
                    .thenReturn(List.of(station));

            GoogleDistanceMatrixResponse transitResponse = createDistanceMatrixResponse(2, 1, 1800, 10000);
            GoogleDistanceMatrixResponse drivingResponse = createDistanceMatrixResponse(2, 1, 900, 8000);
            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("transit"), isNull()))
                    .thenReturn(transitResponse);
            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("driving"), isNull()))
                    .thenReturn(drivingResponse);

            // when
            MidpointRecommendationResponse response =
                    midpointRecommendationService.calculateMidpointRecommendations(MEETING_ID, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.centerPoint()).isNotNull();
            assertThat(response.centerPoint().latitude()).isEqualTo(37.525);
            assertThat(response.centerPoint().longitude()).isEqualTo(126.975);
            assertThat(response.recommendations()).hasSize(1);

            StationRecommendationDto rec = response.recommendations().get(0);
            assertThat(rec.rank()).isEqualTo(1);
            assertThat(rec.stationName()).isEqualTo("합정역");
            assertThat(rec.line()).isEqualTo("2호선");
            assertThat(rec.routes()).hasSize(2);
            assertThat(rec.avgTransitDuration()).isEqualTo(30.0); // 1800s / 60 = 30min
        }

        @Test
        @DisplayName("후보역이 4개일 때 상위 3개만 추천된다")
        void returnsTop3Recommendations() {
            // given
            Meeting meeting = mock(Meeting.class);
            LocationPoll locationPoll = mock(LocationPoll.class);
            when(meetingService.get(MEETING_ID)).thenReturn(meeting);
            when(meeting.getLocationPoll()).thenReturn(locationPoll);
            when(locationPoll.getId()).thenReturn(1L);

            LocationVote vote = createMockVote("37.5000", "127.0000", "참가자A", "서울시 강남구");
            when(locationVoteRepository.findByLocationPoll_Id(1L)).thenReturn(List.of(vote));

            when(stationRepository.calculateCentroid(any())).thenThrow(new RuntimeException("PostGIS 미지원"));

            Station station1 = createMockStation("역1", "1호선", 37.50, 127.01);
            Station station2 = createMockStation("역2", "2호선", 37.51, 127.02);
            Station station3 = createMockStation("역3", "3호선", 37.52, 127.03);
            Station station4 = createMockStation("역4", "4호선", 37.53, 127.04);
            when(stationRepository.findNearbyStations(anyDouble(), anyDouble(), anyInt(), anyInt()))
                    .thenReturn(List.of(station1, station2, station3, station4));

            // 역별로 다른 소요시간: 역1(10분) < 역2(20분) < 역3(30분) < 역4(40분)
            GoogleDistanceMatrixResponse transitResponse = createVariedDistanceMatrixResponse(
                    1, 4, new int[]{600, 1200, 1800, 2400}, new int[]{5000, 10000, 15000, 20000}
            );
            GoogleDistanceMatrixResponse drivingResponse = createVariedDistanceMatrixResponse(
                    1, 4, new int[]{300, 600, 900, 1200}, new int[]{4000, 8000, 12000, 16000}
            );
            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("transit"), isNull()))
                    .thenReturn(transitResponse);
            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("driving"), isNull()))
                    .thenReturn(drivingResponse);

            // when
            MidpointRecommendationResponse response =
                    midpointRecommendationService.calculateMidpointRecommendations(MEETING_ID, null);

            // then
            assertThat(response.recommendations()).hasSize(3);
            assertThat(response.recommendations().get(0).rank()).isEqualTo(1);
            assertThat(response.recommendations().get(0).stationName()).isEqualTo("역1");
            assertThat(response.recommendations().get(1).rank()).isEqualTo(2);
            assertThat(response.recommendations().get(1).stationName()).isEqualTo("역2");
            assertThat(response.recommendations().get(2).rank()).isEqualTo(3);
            assertThat(response.recommendations().get(2).stationName()).isEqualTo("역3");
        }

        @Test
        @DisplayName("transit 응답만 있고 driving이 null이면 transit 기준으로 추천한다")
        void returnsRecommendationWithOnlyTransit() {
            // given
            Meeting meeting = mock(Meeting.class);
            LocationPoll locationPoll = mock(LocationPoll.class);
            when(meetingService.get(MEETING_ID)).thenReturn(meeting);
            when(meeting.getLocationPoll()).thenReturn(locationPoll);
            when(locationPoll.getId()).thenReturn(1L);

            LocationVote vote = createMockVote("37.5000", "127.0000", "참가자A", "서울시 강남구");
            when(locationVoteRepository.findByLocationPoll_Id(1L)).thenReturn(List.of(vote));

            when(stationRepository.calculateCentroid(any())).thenThrow(new RuntimeException("PostGIS 미지원"));

            Station station = createMockStation("강남역", "2호선", 37.4979, 127.0276);
            when(stationRepository.findNearbyStations(anyDouble(), anyDouble(), anyInt(), anyInt()))
                    .thenReturn(List.of(station));

            GoogleDistanceMatrixResponse transitResponse = createDistanceMatrixResponse(1, 1, 2400, 15000);
            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("transit"), isNull()))
                    .thenReturn(transitResponse);
            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("driving"), isNull()))
                    .thenReturn(null);

            // when
            MidpointRecommendationResponse response =
                    midpointRecommendationService.calculateMidpointRecommendations(MEETING_ID, null);

            // then
            assertThat(response.recommendations()).hasSize(1);
            assertThat(response.recommendations().get(0).routes().get(0).transitDuration()).isEqualTo(40); // 2400/60
            assertThat(response.recommendations().get(0).routes().get(0).drivingDuration()).isEqualTo(999); // fallback
        }

        @Test
        @DisplayName("departureName이 없으면 participant 이름으로 대체한다")
        void usesParticipantNameWhenDepartureNameIsNull() {
            // given
            Meeting meeting = mock(Meeting.class);
            LocationPoll locationPoll = mock(LocationPoll.class);
            when(meetingService.get(MEETING_ID)).thenReturn(meeting);
            when(meeting.getLocationPoll()).thenReturn(locationPoll);
            when(locationPoll.getId()).thenReturn(1L);

            LocationVote vote = mock(LocationVote.class);
            when(vote.getDepartureLat()).thenReturn(new BigDecimal("37.5000"));
            when(vote.getDepartureLng()).thenReturn(new BigDecimal("127.0000"));
            when(vote.getDepartureName()).thenReturn(null);
            when(vote.getDepartureLocation()).thenReturn("서울시 강남구");
            Participant participant = mock(Participant.class);
            when(participant.getName()).thenReturn("김참가자");
            when(vote.getParticipant()).thenReturn(participant);

            when(locationVoteRepository.findByLocationPoll_Id(anyLong())).thenReturn(List.of(vote));
            when(stationRepository.calculateCentroid(any())).thenThrow(new RuntimeException("PostGIS 미지원"));

            Station station = createMockStation("역삼역", "2호선", 37.5006, 127.0366);
            when(stationRepository.findNearbyStations(anyDouble(), anyDouble(), anyInt(), anyInt()))
                    .thenReturn(List.of(station));

            GoogleDistanceMatrixResponse transitResponse = createDistanceMatrixResponse(1, 1, 600, 5000);
            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("transit"), isNull()))
                    .thenReturn(transitResponse);
            when(googleDistanceMatrixClient.calculateDistanceMatrix(anyList(), anyList(), eq("driving"), isNull()))
                    .thenReturn(null);

            // when
            MidpointRecommendationResponse response =
                    midpointRecommendationService.calculateMidpointRecommendations(MEETING_ID, null);

            // then
            assertThat(response.recommendations().get(0).routes().get(0).departureName()).isEqualTo("김참가자");
        }
    }

    // --- Helper Methods ---

    private LocationVote createMockVote(String lat, String lng, String name, String address) {
        LocationVote vote = mock(LocationVote.class);
        lenient().when(vote.getDepartureLat()).thenReturn(new BigDecimal(lat));
        lenient().when(vote.getDepartureLng()).thenReturn(new BigDecimal(lng));
        lenient().when(vote.getDepartureName()).thenReturn(name);
        lenient().when(vote.getDepartureLocation()).thenReturn(address);
        return vote;
    }

    private Station createMockStation(String name, String line, double lat, double lng) {
        Station station = mock(Station.class);
        lenient().when(station.getName()).thenReturn(name);
        lenient().when(station.getLine()).thenReturn(line);
        lenient().when(station.getLatitude()).thenReturn(lat);
        lenient().when(station.getLongitude()).thenReturn(lng);
        return station;
    }

    private GoogleDistanceMatrixResponse createDistanceMatrixResponse(
            int originCount, int destCount, int durationSeconds, int distanceMeters
    ) {
        GoogleDistanceMatrixResponse.Element element = new GoogleDistanceMatrixResponse.Element(
                new GoogleDistanceMatrixResponse.Distance("", distanceMeters),
                new GoogleDistanceMatrixResponse.Duration("", durationSeconds),
                "OK"
        );

        List<GoogleDistanceMatrixResponse.Row> rows = new java.util.ArrayList<>();
        for (int i = 0; i < originCount; i++) {
            List<GoogleDistanceMatrixResponse.Element> elements = new java.util.ArrayList<>();
            for (int j = 0; j < destCount; j++) {
                elements.add(element);
            }
            rows.add(new GoogleDistanceMatrixResponse.Row(elements));
        }

        return new GoogleDistanceMatrixResponse(List.of(), List.of(), rows, "OK");
    }

    private GoogleDistanceMatrixResponse createVariedDistanceMatrixResponse(
            int originCount, int destCount, int[] durations, int[] distances
    ) {
        List<GoogleDistanceMatrixResponse.Row> rows = new java.util.ArrayList<>();
        for (int i = 0; i < originCount; i++) {
            List<GoogleDistanceMatrixResponse.Element> elements = new java.util.ArrayList<>();
            for (int j = 0; j < destCount; j++) {
                elements.add(new GoogleDistanceMatrixResponse.Element(
                        new GoogleDistanceMatrixResponse.Distance("", distances[j]),
                        new GoogleDistanceMatrixResponse.Duration("", durations[j]),
                        "OK"
                ));
            }
            rows.add(new GoogleDistanceMatrixResponse.Row(elements));
        }
        return new GoogleDistanceMatrixResponse(List.of(), List.of(), rows, "OK");
    }
}