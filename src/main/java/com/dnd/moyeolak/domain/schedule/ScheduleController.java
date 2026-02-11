package com.dnd.moyeolak.domain.schedule;

import com.dnd.moyeolak.domain.schedule.docs.CreateScheduleVoteApiDocs;
import com.dnd.moyeolak.domain.schedule.dto.CreateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.service.ScheduleService;
import com.dnd.moyeolak.global.response.ApiResponse;
import com.dnd.moyeolak.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Schedules", description = "시간 투표 관련 API")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/vote")
    @CreateScheduleVoteApiDocs
    public ResponseEntity<ApiResponse<Void>> createScheduleVote(
            @RequestParam String meetingId,
            @Valid @RequestBody CreateScheduleVoteRequest request
    ) {
        scheduleService.createParticipantVote(meetingId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success());
    }

    @PutMapping("/vote/{scheduleVoteId}")
    @Operation(summary = "시간 투표 수정", description = "특정 시간 투표를 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updateScheduleVote(
            @PathVariable Long scheduleVoteId,
            @RequestBody UpdateScheduleVoteRequest request
    ) {
        scheduleService.updateParticipantVote(scheduleVoteId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
