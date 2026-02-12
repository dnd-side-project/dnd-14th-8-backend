package com.dnd.moyeolak.domain.participant.controller;

import com.dnd.moyeolak.domain.participant.docs.GetParticipantApiDocs;
import com.dnd.moyeolak.domain.participant.docs.ListParticipantsApiDocs;
import com.dnd.moyeolak.domain.participant.dto.GetParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.global.response.ApiResponse;
import com.dnd.moyeolak.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Participant", description = "참여자 관련 API")
@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @GetMapping
    @ListParticipantsApiDocs
    public ResponseEntity<ApiResponse<ListParticipantResponse>> listParticipants(
            @Parameter(description = "조회할 모임 ID", example = "abc123", required = true)
            @RequestParam String meetingId) {
        ListParticipantResponse response = participantService.listParticipants(meetingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{participantId}")
    @GetParticipantApiDocs
    public ResponseEntity<ApiResponse<GetParticipantResponse>> getParticipant(
            @Parameter(description = "조회할 참여자 ID", required = true)
            @PathVariable Long participantId) {
        GetParticipantResponse response = participantService.getParticipant(participantId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
    }
}
