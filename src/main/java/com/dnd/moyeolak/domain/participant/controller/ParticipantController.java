package com.dnd.moyeolak.domain.participant.controller;

import com.dnd.moyeolak.domain.participant.dto.GetParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.global.response.ApiResponse;
import com.dnd.moyeolak.global.response.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "참여자 전체 조회", description = """
            모임에 참여한 모든 참여자 목록을 조회합니다.

            ### 사용 시점
            - 모임 상세 페이지에서 참여자 목록을 표시할 때
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "참여자 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ListParticipantResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "code": "S100",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "participants": [
                                                  {
                                                    "participantId": 1,
                                                    "name": "김철수",
                                                    "isHost": true
                                                  },
                                                  {
                                                    "participantId": 2,
                                                    "name": "이영희",
                                                    "isHost": false
                                                  }
                                                ],
                                                "totalCount": 2
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "모임 없음",
                                    value = """
                                            {
                                              "code": "E410",
                                              "message": "모임이 존재하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<ListParticipantResponse>> listParticipants(
            @Parameter(description = "조회할 모임 ID", example = "abc123", required = true)
            @RequestParam String meetingId) {
        ListParticipantResponse response = participantService.listParticipants(meetingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{participantId}")
    @Operation(summary = "참여자 단건 조회", description = """
            특정 참여자의 정보를 조회합니다.

            ### 참여자 ID 취득 방법
            - `/api/participants/join-with-schedule` 또는 `/api/participants/join-with-location` 성공 응답의 `data.participantId`
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "참여자 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetParticipantResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "code": "S001",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "participantId": 1,
                                                "name": "김철수",
                                                "isHost": false
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "참여자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "참여자 없음",
                                    value = """
                                            {
                                              "code": "E414",
                                              "message": "참여자가 존재하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<GetParticipantResponse>> getParticipant(
            @Parameter(description = "조회할 참여자 ID", required = true)
            @PathVariable Long participantId) {
        GetParticipantResponse response = participantService.getParticipant(participantId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.OK, response));
    }
}
