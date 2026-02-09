package com.dnd.moyeolak.domain.schedule;

import com.dnd.moyeolak.domain.schedule.dto.UpdateScheduleVotesRequest;
import com.dnd.moyeolak.domain.schedule.service.ScheduleService;
import com.dnd.moyeolak.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Schedules", description = "시간 투표 관련 API")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PutMapping("/vote/{scheduleVoteId}")
    @Operation(summary = "모임 일정 투표 수정", description = "모임의 일정 투표를 수정하는 API입니다.")
    public ResponseEntity<ApiResponse<Void>> updateScheduleVotes(
            @PathVariable Long scheduleVoteId,
            @RequestBody UpdateScheduleVotesRequest request
    ) {
        scheduleService.updateScheduleVotes(scheduleVoteId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
