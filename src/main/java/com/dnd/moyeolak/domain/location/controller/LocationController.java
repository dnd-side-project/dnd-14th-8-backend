package com.dnd.moyeolak.domain.location.controller;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.service.LocationService;
import com.dnd.moyeolak.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "LocationVote", description = "위치 투표 관련 API")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/vote")
    @Operation(summary = "출발지 추가", description = "장소 조율을 위해 출발지를 추가하는 API입니다.")
    public ResponseEntity<ApiResponse<Void>> createLocationVote(@RequestBody CreateLocationVoteRequest createLocationVoteRequest) {
        locationService.createLocationVote(createLocationVoteRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
