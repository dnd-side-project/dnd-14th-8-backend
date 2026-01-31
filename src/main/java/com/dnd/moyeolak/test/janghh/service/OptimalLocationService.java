package com.dnd.moyeolak.test.janghh.service;

import com.dnd.moyeolak.global.client.kakao.KakaoLocalClient;
import com.dnd.moyeolak.global.client.odsay.OdsayClient;
import com.dnd.moyeolak.global.client.odsay.dto.OdsayPathInfo;
import com.dnd.moyeolak.test.janghh.dto.request.OptimalLocationRequest;
import com.dnd.moyeolak.test.janghh.dto.request.OptimalLocationRequest.ParticipantInfo;
import com.dnd.moyeolak.test.janghh.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimalLocationService {

    private final KakaoLocalClient kakaoLocalClient;
    private final OdsayClient odsayClient;
    private final GeographicCalculator geographicCalculator;

    private static final int SEARCH_RADIUS = 5000;
    private static final int CANDIDATE_COUNT = 5;
    private static final int RECOMMENDATION_COUNT = 3;

    public OptimalLocationResponse findOptimalLocations(OptimalLocationRequest request) {
        List<ParticipantInfo> participants = request.participants();

        double[] center = geographicCalculator.calculateGeographicCenter(participants);
        log.info("중간지점 계산 완료: 위도={}, 경도={}", center[0], center[1]);

        List<SubwayStation> stations = kakaoLocalClient.findNearestSubwayStations(
            center[0],
            center[1],
            SEARCH_RADIUS,
            CANDIDATE_COUNT
        );

        if (stations.isEmpty()) {
            throw new IllegalStateException("주변에 지하철역이 없습니다.");
        }

        log.info("지하철역 검색 완료: {}개", stations.size());

        List<EvaluatedPlace> evaluatedPlaces = evaluatePlaces(participants, stations);

        List<EvaluatedPlace> recommendations = evaluatedPlaces.stream()
            .limit(RECOMMENDATION_COUNT)
            .toList();

        return new OptimalLocationResponse(
            new CenterPoint(center[0], center[1]),
            recommendations
        );
    }

    private List<EvaluatedPlace> evaluatePlaces(
        List<ParticipantInfo> participants,
        List<SubwayStation> stations
    ) {
        List<EvaluatedPlace> evaluated = new ArrayList<>();

        for (SubwayStation station : stations) {
            List<Integer> durations = new ArrayList<>();
            List<RouteDetail> routes = new ArrayList<>();

            for (ParticipantInfo participant : participants) {
                try {
                    OdsayPathInfo pathInfo = odsayClient.searchRoute(
                        participant.latitude(),
                        participant.longitude(),
                        station.latitude(),
                        station.longitude()
                    );

                    durations.add(pathInfo.safeTotal());

                    routes.add(new RouteDetail(
                        participant.name(),
                        pathInfo.safeTotal(),
                        pathInfo.safeTotalDistance(),
                        pathInfo.safePayment(),
                        pathInfo.safeBusTransit() + pathInfo.safeSubwayTransit()
                    ));

                    Thread.sleep(100);

                } catch (Exception e) {
                    log.error("경로 탐색 실패: {} -> {}", participant.name(), station.name(), e);
                    durations.add(999);
                }
            }

            int minSum = durations.stream().mapToInt(Integer::intValue).sum();
            int minMax = durations.stream().mapToInt(Integer::intValue).max().orElse(0);
            double avgDuration = durations.stream().mapToInt(Integer::intValue).average().orElse(0);

            evaluated.add(new EvaluatedPlace(
                0,
                station.name(),
                "지하철역",
                station.address(),
                station.latitude(),
                station.longitude(),
                station.distanceFromCenter(),
                minSum,
                minMax,
                avgDuration,
                routes
            ));
        }

        evaluated.sort(Comparator.comparingInt(EvaluatedPlace::minSum));

        List<EvaluatedPlace> rankedPlaces = new ArrayList<>();
        for (int i = 0; i < evaluated.size(); i++) {
            EvaluatedPlace place = evaluated.get(i);
            rankedPlaces.add(new EvaluatedPlace(
                i + 1,
                place.name(),
                place.category(),
                place.address(),
                place.latitude(),
                place.longitude(),
                place.distanceFromCenter(),
                place.minSum(),
                place.minMax(),
                place.avgDuration(),
                place.routes()
            ));
        }

        return rankedPlaces;
    }
}
