package com.dnd.moyeolak.domain.schedule.controller;

import com.dnd.moyeolak.domain.schedule.docs.UpdateSchedulePollApiDocs;
import com.dnd.moyeolak.domain.schedule.dto.UpdateSchedulePollRequest;
import com.dnd.moyeolak.domain.schedule.service.SchedulePollService;
import com.dnd.moyeolak.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "SchedulePoll", description = "일정 투표 관련 API")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class SchedulePollController {

    private final SchedulePollService schedulePollService;

    @PutMapping("/poll")
    @UpdateSchedulePollApiDocs
    public ResponseEntity<ApiResponse<Void>> updateSchedulePoll(
            @Parameter(description = "모임 ID", example = "abc-123", required = true)
            @RequestParam String meetingId,
            @Valid @RequestBody UpdateSchedulePollRequest request) {
        schedulePollService.updateSchedulePoll(meetingId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
