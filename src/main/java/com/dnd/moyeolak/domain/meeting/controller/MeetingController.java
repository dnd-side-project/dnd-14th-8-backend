package com.dnd.moyeolak.domain.meeting.controller;

import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.dto.GetMeetingScheduleResponse;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Meeting", description = "모임 관련 API")
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping("/{id}/schedules")
    @Operation(summary = "모임 일정 조회", description = "특정 모임의 일정을 조회합니다.")
    public ResponseEntity<ApiResponse<GetMeetingScheduleResponse>> getMeetingSchedules(@PathVariable("id") String meetingId) {
        GetMeetingScheduleResponse meetingSchedules = meetingService.getMeetingSchedules(meetingId);
        return ResponseEntity.ok(ApiResponse.success(meetingSchedules));
    }

    @PostMapping
    @Operation(summary = "모임 생성", description = "새로운 모임을 생성합니다.")
    public ResponseEntity<ApiResponse<String>> createMeeting(@RequestBody CreateMeetingRequest request) {
        String meetingId = meetingService.createMeeting(request);
        return ResponseEntity.ok(ApiResponse.success(meetingId));
    }
}
