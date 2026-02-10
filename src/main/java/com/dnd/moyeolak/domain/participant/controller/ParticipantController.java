package com.dnd.moyeolak.domain.participant.controller;

import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Participant", description = "참여자 관련 API")
@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping("/join-with-location")
    @Operation(
            summary = "위치 투표와 함께 참여",
            description = """
                    참여자 생성과 동시에 출발지 위치를 등록합니다.

                    ### 사용 시점
                    - 일정이 확정된 후, 모임 장소를 정할 때
                    - 참여자가 자신의 출발 위치를 입력할 때

                    ### 좌표 입력 방법
                    - 프론트에서 Kakao/Google Maps API로 좌표 획득
                    - 위도(latitude): -90 ~ 90
                    - 경도(longitude): -180 ~ 180
                    - 주소(address): 선택 입력 (사용자 확인용)

                    ### 예시 좌표
                    - 서울역: 위도 37.5547, 경도 126.9707
                    - 강남역: 위도 37.4979, 경도 127.0276
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "참여자 생성 및 위치 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateParticipantResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    summary = "위치 투표 참여 성공",
                                    value = """
                                            {
                                              "code": "S101",
                                              "message": "리소스가 성공적으로 생성되었습니다.",
                                              "data": {
                                                "participantId": 16,
                                                "name": "이영희",
                                                "scheduleVoteCount": null,
                                                "hasLocation": true,
                                                "createdAt": "2025-02-06T14:35:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 데이터 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "좌표 범위 초과",
                                            summary = "위도가 90 초과",
                                            value = """
                                                    {
                                                      "code": "E103",
                                                      "message": "유효성 검증 실패",
                                                      "data": {
                                                        "location.latitude": "위도는 90 이하여야 합니다"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "위치 정보 누락",
                                            summary = "location 객체 미전송",
                                            value = """
                                                    {
                                                      "code": "E103",
                                                      "message": "유효성 검증 실패",
                                                      "data": {
                                                        "location": "위치 정보는 필수입니다"
                                                      }
                                                    }
                                                    """
                                    )
                            }
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
    public ResponseEntity<ApiResponse<CreateParticipantResponse>> joinWithLocation(
            @Parameter(
                    description = "참여할 모임 ID",
                    example = "abc123",
                    required = true
            )
            @RequestParam String meetingId,
            @Valid @RequestBody CreateParticipantWithLocationRequest request) {

        CreateParticipantResponse response = participantService.createWithLocation(meetingId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.RESOURCE_CREATED, response));
    }

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
