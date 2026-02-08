package com.dnd.moyeolak.domain.schedule.controller;

import com.dnd.moyeolak.domain.schedule.dto.UpdateSchedulePollRequest;
import com.dnd.moyeolak.domain.schedule.service.SchedulePollService;
import com.dnd.moyeolak.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "일정 투표 옵션 수정",
            description = """
                    모임의 일정 투표 옵션(날짜 목록, 시작/종료 시간)을 수정합니다.

                    **제약 조건**
                    - `dateOptions`: 최소 1개 이상의 날짜 필수 (빈 배열 불가)
                    - `startTime`: HH:mm 형식, 30분 단위만 허용, `endTime`보다 작아야 함
                    - `endTime`: HH:mm 또는 24:00, 30분 단위만 허용, `startTime`보다 커야 함 (24:00 = 자정)
                    - 기존 참가자의 일정 투표(ScheduleVote)는 유지됩니다
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (dateOptions 비어있음 / startTime >= endTime)",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임 또는 일정 투표판을 찾을 수 없음",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    public ResponseEntity<ApiResponse<Void>> updateSchedulePoll(
            @Parameter(description = "모임 ID", example = "abc-123", required = true)
            @RequestParam String meetingId,
            @Valid @RequestBody UpdateSchedulePollRequest request) {
        schedulePollService.updateSchedulePoll(meetingId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
