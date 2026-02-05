package com.dnd.moyeolak.domain.participant.controller;

import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.global.response.ApiResponse;
import com.dnd.moyeolak.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Participant", description = "참여자 관련 API")
@RestController
@RequestMapping("/api/meetings/{meetingId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping("/schedule")
    @Operation(summary = "일정 기반 참여자 생성", description = "참여자와 가능한 시간대 투표를 함께 생성합니다.")
    public ResponseEntity<ApiResponse<CreateParticipantResponse>> createWithSchedule(
            @PathVariable String meetingId,
            @Valid @RequestBody CreateParticipantWithScheduleRequest request) {

        CreateParticipantResponse response = participantService.createWithSchedule(meetingId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.RESOURCE_CREATED, response));
    }

    @PostMapping("/location")
    @Operation(summary = "위치 기반 참여자 생성", description = "참여자와 출발지 위치 투표를 함께 생성합니다.")
    public ResponseEntity<ApiResponse<CreateParticipantResponse>> createWithLocation(
            @PathVariable String meetingId,
            @Valid @RequestBody CreateParticipantWithLocationRequest request) {

        CreateParticipantResponse response = participantService.createWithLocation(meetingId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.RESOURCE_CREATED, response));
    }
}
