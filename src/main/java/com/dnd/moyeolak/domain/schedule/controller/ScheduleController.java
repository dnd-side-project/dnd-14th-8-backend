package com.dnd.moyeolak.domain.schedule.controller;

import com.dnd.moyeolak.domain.schedule.docs.CreateScheduleVoteApiDocs;
import com.dnd.moyeolak.domain.schedule.docs.UpdateSchedulePollApiDocs;
import com.dnd.moyeolak.domain.schedule.dto.CreateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.dto.UpdateSchedulePollRequest;
import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVoteRequest;
import com.dnd.moyeolak.domain.schedule.service.SchedulePollService;
import com.dnd.moyeolak.domain.schedule.service.ScheduleVoteService;
import com.dnd.moyeolak.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Schedules", description = "일정 관련 API")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final SchedulePollService schedulePollService;
    private final ScheduleVoteService scheduleVoteService;

    @PutMapping("/poll")
    @UpdateSchedulePollApiDocs
    public ResponseEntity<ApiResponse<Void>> updateSchedulePoll(
            @Parameter(description = "모임 ID", example = "abc-123", required = true)
            @RequestParam String meetingId,
            @Valid @RequestBody UpdateSchedulePollRequest request) {
        schedulePollService.updateSchedulePoll(meetingId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/poll/{schedulePollId}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmSchedulePoll(@PathVariable Long schedulePollId) {
        schedulePollService.confirmSchedulePoll(schedulePollId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/vote")
    @CreateScheduleVoteApiDocs
    public ResponseEntity<ApiResponse<Long>> createScheduleVote(
            @RequestParam String meetingId,
            @Valid @RequestBody CreateScheduleVoteRequest request
    ) {
        Long scheduleVoteId = scheduleVoteService.createParticipantVote(meetingId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(scheduleVoteId));
    }

    @PutMapping("/vote/{scheduleVoteId}")
    @Operation(summary = "시간 투표 수정", description = "특정 시간 투표를 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updateScheduleVote(
            @PathVariable Long scheduleVoteId,
            @RequestBody UpdateScheduleVoteRequest request
    ) {
        scheduleVoteService.updateParticipantVote(scheduleVoteId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
