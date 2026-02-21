package com.dnd.moyeolak.domain.location.service.impl;

import com.dnd.moyeolak.domain.location.dto.*;
import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.repository.LocationVoteRepository;
import com.dnd.moyeolak.domain.location.service.MidpointRecommendationService;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.global.client.google.GoogleDistanceMatrixClient;
import com.dnd.moyeolak.global.client.google.GoogleDistanceMatrixClient.Coordinate;
import com.dnd.moyeolak.global.client.google.dto.GoogleDistanceMatrixResponse;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import com.dnd.moyeolak.global.station.entity.Station;
import com.dnd.moyeolak.global.station.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MidpointRecommendationServiceImpl implements MidpointRecommendationService {

    private final MeetingService meetingService;
    private final LocationVoteRepository locationVoteRepository;
    private final StationRepository stationRepository;
    private final GoogleDistanceMatrixClient googleDistanceMatrixClient;

    private static final int SEARCH_RADIUS_METERS = 5000;
    private static final int MAX_CANDIDATE_STATIONS = 10;
    private static final int TOP_RECOMMENDATIONS = 3;
    private static final int MAX_ELEMENTS_PER_REQUEST = 100;

    @Override
    @Cacheable(value = "midpointRecommendations", key = "#meetingId + '_' + #departureTime")
    public MidpointRecommendationResponse calculateMidpointRecommendations(String meetingId, LocalDateTime departureTime) {
        // 1. 출발지 데이터 조회
        Meeting meeting = meetingService.get(meetingId);
        LocationPoll locationPoll = meeting.getLocationPoll();
        if (locationPoll == null) {
            throw new BusinessException(ErrorCode.LOCATION_POLL_NOT_FOUND);
        }

        List<LocationVote> votes = locationVoteRepository.findByLocationPoll_Id(locationPoll.getId());
        if (votes.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_LOCATION_VOTES);
        }

        // 2. PostGIS로 무게중심 계산
        CenterPointDto centerPoint = calculateCentroid(votes);
        log.info("무게중심 계산 완료: lat={}, lng={}", centerPoint.latitude(), centerPoint.longitude());

        // 3. PostGIS로 근처 지하철역 검색
        List<Station> candidateStations = stationRepository.findNearbyStations(
                centerPoint.latitude(),
                centerPoint.longitude(),
                SEARCH_RADIUS_METERS,
                MAX_CANDIDATE_STATIONS
        );
        if (candidateStations.isEmpty()) {
            throw new BusinessException(ErrorCode.NO_NEARBY_STATIONS);
        }
        log.info("후보 지하철역 {}개 검색 완료", candidateStations.size());

        // 4. Google Distance Matrix API 배치 호출 (transit + driving)
        List<Coordinate> origins = votes.stream()
                .map(v -> new Coordinate(v.getDepartureLat().doubleValue(), v.getDepartureLng().doubleValue()))
                .toList();

        List<Coordinate> destinations = candidateStations.stream()
                .map(s -> new Coordinate(s.getLatitude(), s.getLongitude()))
                .toList();

        Long departureTimeEpoch = departureTime != null
                ? departureTime.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond()
                : null;

        GoogleDistanceMatrixResponse transitResponse =
                calculateDistanceMatrixInChunks(origins, destinations, "transit", departureTimeEpoch);
        GoogleDistanceMatrixResponse drivingResponse =
                calculateDistanceMatrixInChunks(origins, destinations, "driving", departureTimeEpoch);

        if (transitResponse == null && drivingResponse == null) {
            throw new BusinessException(ErrorCode.GOOGLE_API_ERROR);
        }

        // 5. 역별 평가 및 순위 산정
        List<StationRecommendationDto> recommendations = evaluateStations(
                votes, candidateStations, transitResponse, drivingResponse, centerPoint
        );

        return new MidpointRecommendationResponse(centerPoint, recommendations, departureTime);
    }

    /**
     * Google Distance Matrix API의 요청당 최대 100 elements(origins × destinations) 제한을 처리하기 위해
     * origins를 청크로 나눠 복수 호출한 뒤 rows를 병합하여 반환한다.
     */
    private GoogleDistanceMatrixResponse calculateDistanceMatrixInChunks(
            List<Coordinate> origins,
            List<Coordinate> destinations,
            String mode,
            Long departureTime
    ) {
        int chunkSize = Math.max(1, MAX_ELEMENTS_PER_REQUEST / destinations.size());

        if (origins.size() <= chunkSize) {
            return googleDistanceMatrixClient.calculateDistanceMatrix(origins, destinations, mode, departureTime);
        }

        log.info("Google Distance Matrix 청크 분할: origins={}, chunkSize={}, mode={}", origins.size(), chunkSize, mode);

        List<GoogleDistanceMatrixResponse.Row> allRows = new ArrayList<>();
        for (int i = 0; i < origins.size(); i += chunkSize) {
            List<Coordinate> chunk = origins.subList(i, Math.min(i + chunkSize, origins.size()));
            GoogleDistanceMatrixResponse chunkResponse =
                    googleDistanceMatrixClient.calculateDistanceMatrix(chunk, destinations, mode, departureTime);
            if (chunkResponse != null && chunkResponse.rows() != null) {
                allRows.addAll(chunkResponse.rows());
            }
        }

        if (allRows.isEmpty()) {
            return null;
        }
        return new GoogleDistanceMatrixResponse(null, null, allRows, "OK");
    }

    private CenterPointDto calculateCentroid(List<LocationVote> votes) {
        String[] wktPoints = votes.stream()
                .map(v -> "SRID=4326;POINT(" +
                        v.getDepartureLng().doubleValue() + " " +
                        v.getDepartureLat().doubleValue() + ")")
                .toArray(String[]::new);

        try {
            Object[] result = stationRepository.calculateCentroid(wktPoints);
            if (result != null && result.length > 0) {
                Object[] row = (Object[]) result[0];
                double lat = ((Number) row[0]).doubleValue();
                double lng = ((Number) row[1]).doubleValue();
                return new CenterPointDto(lat, lng);
            }
        } catch (Exception e) {
            log.warn("PostGIS 무게중심 계산 실패, 산술 평균으로 대체: {}", e.getMessage());
        }

        // PostGIS 실패 시 산술 평균으로 대체
        double avgLat = votes.stream()
                .mapToDouble(v -> v.getDepartureLat().doubleValue())
                .average()
                .orElse(0);
        double avgLng = votes.stream()
                .mapToDouble(v -> v.getDepartureLng().doubleValue())
                .average()
                .orElse(0);
        return new CenterPointDto(avgLat, avgLng);
    }

    private List<StationRecommendationDto> evaluateStations(
            List<LocationVote> votes,
            List<Station> stations,
            GoogleDistanceMatrixResponse transitResponse,
            GoogleDistanceMatrixResponse drivingResponse,
            CenterPointDto centerPoint
    ) {
        List<StationRecommendationDto> results = new ArrayList<>();

        for (int stationIdx = 0; stationIdx < stations.size(); stationIdx++) {
            Station station = stations.get(stationIdx);
            List<RouteDto> routes = new ArrayList<>();

            for (int voteIdx = 0; voteIdx < votes.size(); voteIdx++) {
                LocationVote vote = votes.get(voteIdx);

                int transitDuration = 999;
                int transitDistance = 0;
                int drivingDuration = 999;
                int drivingDistance = 0;

                if (transitResponse != null) {
                    GoogleDistanceMatrixResponse.Element transitElement =
                            getElement(transitResponse, voteIdx, stationIdx);
                    if (transitElement != null && "OK".equals(transitElement.status())) {
                        transitDuration = transitElement.duration().value() / 60;
                        transitDistance = transitElement.distance().value();
                    }
                }

                if (drivingResponse != null) {
                    GoogleDistanceMatrixResponse.Element drivingElement =
                            getElement(drivingResponse, voteIdx, stationIdx);
                    if (drivingElement != null && "OK".equals(drivingElement.status())) {
                        drivingDuration = drivingElement.duration().value() / 60;
                        drivingDistance = drivingElement.distance().value();
                    }
                }

                String departureName = resolveDepartureName(vote);

                Long participantId = vote.getParticipant() != null ? vote.getParticipant().getId() : null;

                routes.add(RouteDto.builder()
                        .participantId(participantId)
                        .departureName(departureName)
                        .departureAddress(vote.getDepartureLocation())
                        .transitDuration(transitDuration)
                        .transitDistance(transitDistance)
                        .drivingDuration(drivingDuration)
                        .drivingDistance(drivingDistance)
                        .build());
            }

            double avgTransitDuration = routes.stream()
                    .mapToInt(RouteDto::transitDuration)
                    .average()
                    .orElse(999.0);

            int distanceFromCenter = haversineDistance(
                    centerPoint.latitude(), centerPoint.longitude(),
                    station.getLatitude(), station.getLongitude()
            );

            results.add(StationRecommendationDto.builder()
                    .rank(0)
                    .stationId(station.getId())
                    .stationName(station.getName())
                    .line(station.getLine())
                    .latitude(station.getLatitude())
                    .longitude(station.getLongitude())
                    .distanceFromCenter(distanceFromCenter)
                    .avgTransitDuration(avgTransitDuration)
                    .routes(routes)
                    .build());
        }

        // avgTransitDuration 오름차순 정렬 → Top 3 선정 → 순위 부여
        List<StationRecommendationDto> sorted = results.stream()
                .sorted(Comparator.comparingDouble(StationRecommendationDto::avgTransitDuration))
                .limit(TOP_RECOMMENDATIONS)
                .toList();

        return IntStream.range(0, sorted.size())
                .mapToObj(i -> StationRecommendationDto.builder()
                        .rank(i + 1)
                        .stationId(sorted.get(i).stationId())
                        .stationName(sorted.get(i).stationName())
                        .line(sorted.get(i).line())
                        .latitude(sorted.get(i).latitude())
                        .longitude(sorted.get(i).longitude())
                        .distanceFromCenter(sorted.get(i).distanceFromCenter())
                        .avgTransitDuration(sorted.get(i).avgTransitDuration())
                        .routes(sorted.get(i).routes())
                        .build())
                .toList();
    }

    private GoogleDistanceMatrixResponse.Element getElement(
            GoogleDistanceMatrixResponse response, int originIdx, int destIdx
    ) {
        if (response.rows() == null || originIdx >= response.rows().size()) {
            return null;
        }
        GoogleDistanceMatrixResponse.Row row = response.rows().get(originIdx);
        if (row.elements() == null || destIdx >= row.elements().size()) {
            return null;
        }
        return row.elements().get(destIdx);
    }

    private String resolveDepartureName(LocationVote vote) {
        if (vote.getDepartureName() != null && !vote.getDepartureName().isBlank()) {
            return vote.getDepartureName();
        }
        if (vote.getParticipant() != null && vote.getParticipant().getName() != null) {
            return vote.getParticipant().getName();
        }
        return "알 수 없음";
    }

    private int haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371e3;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (R * c);
    }
}