package com.dnd.moyeolak.domain.location.controller;

import com.dnd.moyeolak.domain.location.docs.CreateLocationVoteApiDocs;
import com.dnd.moyeolak.domain.location.docs.GetMidpointRecommendationsApiDocs;
import com.dnd.moyeolak.domain.location.docs.ListLocationVoteApiDocs;
import com.dnd.moyeolak.domain.location.docs.UpdateLocationVoteApiDocs;
import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.dto.LocationVoteResponse;
import com.dnd.moyeolak.domain.location.dto.MidpointRecommendationResponse;
import com.dnd.moyeolak.domain.location.service.LocationService;
import com.dnd.moyeolak.domain.location.service.MidpointRecommendationService;
import com.dnd.moyeolak.domain.meeting.dto.UpdateLocationVoteRequest;
import com.dnd.moyeolak.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final MidpointRecommendationService midpointRecommendationService;

    @GetMapping("/midpoint-recommendations")
    @GetMidpointRecommendationsApiDocs
    public ResponseEntity<ApiResponse<MidpointRecommendationResponse>> getMidpointRecommendations(
            @Parameter(description = "모임 ID", example = "test-meeting-001", required = true)
            @RequestParam String meetingId
    ) {
        MidpointRecommendationResponse response =
                midpointRecommendationService.calculateMidpointRecommendations(meetingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/poll/{locationPollId}/votes")
    @ListLocationVoteApiDocs
    public ResponseEntity<ApiResponse<?>> listLocationVote(
            @Parameter(description = "위치 투표판 ID", example = "1", required = true)
            @PathVariable Long locationPollId) {
        List<LocationVoteResponse> listLocationVote = locationService.listLocationVote(locationPollId);
        return ResponseEntity.ok(ApiResponse.success(listLocationVote));
    }

    @PutMapping("/vote/{locationVoteId}")
    @UpdateLocationVoteApiDocs
    public ResponseEntity<ApiResponse<Void>> updateLocationVote(
            @Parameter(description = "수정할 출발지 투표 ID", example = "1", required = true)
            @PathVariable Long locationVoteId,
            @Valid @RequestBody UpdateLocationVoteRequest request
    ) {
        locationService.updateLocationVote(locationVoteId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/vote")
    @CreateLocationVoteApiDocs
    public ResponseEntity<ApiResponse<Void>> createLocationVote(@Valid @RequestBody CreateLocationVoteRequest request) {
        locationService.createLocationVote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @DeleteMapping("/vote/{locationVoteId}")
    @Operation(summary = "출발지 삭제", description = "등록된 출발지를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteLocationVote(
            @Parameter(description = "삭제할 출발지 투표 ID", example = "1", required = true)
            @PathVariable Long locationVoteId) {
        locationService.deleteLocationVote(locationVoteId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
