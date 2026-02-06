package com.dnd.moyeolak.domain.participant.controller;

import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
import com.dnd.moyeolak.domain.participant.facade.ParticipantFacade;
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

    private final ParticipantFacade participantFacade;

    @PostMapping("/join-with-schedule")
    @Operation(
            summary = "일정 투표와 함께 참여",
            description = """
                    참여자 생성과 동시에 가능한 시간대에 투표합니다.

                    ### 사용 시점
                    - 모임 생성자가 일정 투표를 시작한 후
                    - 참여자가 자신의 가능한 시간대를 선택할 때

                    ### 주의사항
                    - `availableSchedules`는 30분 단위로 전송 (예: 09:00, 09:30, 10:00)
                    - 시간은 ISO-8601 형식 + Asia/Seoul 기준
                    - `localStorageKey`는 브라우저별 고유값 (재참여 방지용)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "참여자 생성 및 일정 투표 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateParticipantResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    summary = "일정 투표 참여 성공",
                                    value = """
                                            {
                                              "code": "S101",
                                              "message": "리소스가 성공적으로 생성되었습니다.",
                                              "data": {
                                                "participantId": 15,
                                                "name": "김철수",
                                                "scheduleVoteCount": 4,
                                                "hasLocation": false,
                                                "createdAt": "2025-02-06T14:30:00"
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
                                            name = "필수값 누락",
                                            summary = "이름 미입력",
                                            value = """
                                                    {
                                                      "code": "E103",
                                                      "message": "유효성 검증 실패",
                                                      "data": {
                                                        "name": "이름은 필수입니다"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "빈 일정 목록",
                                            summary = "가능한 시간 미선택",
                                            value = """
                                                    {
                                                      "code": "E103",
                                                      "message": "유효성 검증 실패",
                                                      "data": {
                                                        "availableSchedules": "가능한 시간 정보는 필수입니다"
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
    public ResponseEntity<ApiResponse<CreateParticipantResponse>> joinWithSchedule(
            @Parameter(
                    description = "참여할 모임 ID",
                    example = "abc123",
                    required = true
            )
            @RequestParam String meetingId,
            @Valid @RequestBody CreateParticipantWithScheduleRequest request) {

        CreateParticipantResponse response = participantFacade.createWithSchedule(meetingId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.RESOURCE_CREATED, response));
    }

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

        CreateParticipantResponse response = participantFacade.createWithLocation(meetingId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessCode.RESOURCE_CREATED, response));
    }
}