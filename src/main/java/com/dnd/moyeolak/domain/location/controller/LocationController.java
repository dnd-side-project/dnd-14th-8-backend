package com.dnd.moyeolak.domain.location.controller;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.dto.LocationVoteResponse;
import com.dnd.moyeolak.domain.location.service.LocationService;
import com.dnd.moyeolak.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "LocationVote", description = "위치 투표 관련 API")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/poll/{id}/votes")
    @Operation(summary = "출발지 투표 조회", description = "장소 조율 시 출발지 투표를 조회하는 API입니다.")
    public ResponseEntity<ApiResponse<?>> listLocationVote(@PathVariable Long id) {
        List<LocationVoteResponse> listLocationVote = locationService.listLocationVote(id);
        return ResponseEntity.ok(ApiResponse.success(listLocationVote));
    }

    @PostMapping("/vote")
    @Operation(summary = "출발지 추가", description = "장소 조율을 위해 출발지를 추가하는 API입니다.")
    public ResponseEntity<ApiResponse<Void>> createLocationVote(@Valid @RequestBody CreateLocationVoteRequest request) {
        locationService.createLocationVote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @DeleteMapping("/vote/{id}")
    @Operation(summary = "출발지 삭제", description = "장소 조율 시 출발지를 삭제하는 API입니다.")
    public ResponseEntity<ApiResponse<Void>> deleteLocationVote(@PathVariable Long id) {
        locationService.deleteLocationVote(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
