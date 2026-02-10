package com.dnd.moyeolak.domain.location.controller;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.dto.LocationVoteResponse;
import com.dnd.moyeolak.domain.location.dto.MidpointRecommendationResponse;
import com.dnd.moyeolak.domain.location.service.LocationService;
import com.dnd.moyeolak.domain.location.service.MidpointRecommendationService;
import com.dnd.moyeolak.domain.meeting.dto.UpdateLocationVoteRequest;
import com.dnd.moyeolak.global.response.ApiResponse;
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

import java.util.List;

@Tag(name = "LocationVote", description = "위치 투표 관련 API")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final MidpointRecommendationService midpointRecommendationService;

    @GetMapping("/midpoint-recommendations")
    @Operation(
            summary = "중간지점 추천",
            description = """
                    등록된 출발지를 기반으로 최적의 중간 만남 장소(지하철역)를 추천합니다.

                    ### 지원 범위
                    - **수도권 전용**: 서울, 경기, 인천 지역의 지하철역만 검색됩니다
                    - 지원 노선: 1~9호선, 경의중앙선, 수인분당선, 신분당선, 경춘선, 경강선, 인천1·2호선
                    - 수도권 외 지역의 출발지로 요청 시 반경 내 역이 없어 404 응답될 수 있습니다

                    ### 알고리즘
                    1. 모든 참가자의 출발지 좌표로 **무게중심(centroid)** 계산 (PostGIS)
                    2. 무게중심 반경 5km 내 **지하철역 최대 10개** 탐색
                    3. Google Distance Matrix API로 각 역까지의 **대중교통/자가용 이동시간** 평가
                    4. 평균 대중교통 이동시간 기준 **상위 3개역** 추천

                    ### 테스트용 더미 데이터
                    | meetingId | 시나리오 | 출발지 |
                    |-----------|---------|--------|
                    | `test-meeting-001` | 수도권 광역 | 수원, 강남, 일산, 인천 (무게중심: 홍대~신촌) |
                    | `test-meeting-002` | 서울 시내 | 강남, 홍대, 잠실, 노원 (무게중심: 종로~을지로) |
                    | `test-meeting-003` | 경기도 내 | 수원, 성남, 용인, 안양 (무게중심: 과천~의왕) |

                    ### 주의사항
                    - 출발지가 1개 이상 등록되어야 합니다
                    - 반경 5km 내 지하철역이 없으면 404 응답
                    - Google API 장애 시 500 응답
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "중간지점 추천 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MidpointRecommendationResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    summary = "3개 역 추천 결과",
                                    value = """
                                            {
                                              "code": "S100",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "centerPoint": {
                                                  "latitude": 37.5665,
                                                  "longitude": 126.9780
                                                },
                                                "recommendations": [
                                                  {
                                                    "rank": 1,
                                                    "stationName": "디지털미디어시티역",
                                                    "line": "경의중앙선",
                                                    "latitude": 37.5779,
                                                    "longitude": 126.8997,
                                                    "distanceFromCenter": 1200,
                                                    "avgTransitDuration": 42.5,
                                                    "routes": [
                                                      {
                                                        "departureName": "김철수",
                                                        "departureAddress": "수원시 장안구 영화동",
                                                        "transitDuration": 55,
                                                        "transitDistance": 21400,
                                                        "drivingDuration": 35,
                                                        "drivingDistance": 18200
                                                      },
                                                      {
                                                        "departureName": "이영희",
                                                        "departureAddress": "서울시 강남구 역삼동",
                                                        "transitDuration": 30,
                                                        "transitDistance": 12000,
                                                        "drivingDuration": 20,
                                                        "drivingDistance": 9500
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "rank": 2,
                                                    "stationName": "홍대입구역",
                                                    "line": "2호선",
                                                    "latitude": 37.5571,
                                                    "longitude": 126.9246,
                                                    "distanceFromCenter": 800,
                                                    "avgTransitDuration": 45.0,
                                                    "routes": [
                                                      {
                                                        "departureName": "김철수",
                                                        "departureAddress": "수원시 장안구 영화동",
                                                        "transitDuration": 50,
                                                        "transitDistance": 20000,
                                                        "drivingDuration": 32,
                                                        "drivingDistance": 17000
                                                      },
                                                      {
                                                        "departureName": "이영희",
                                                        "departureAddress": "서울시 강남구 역삼동",
                                                        "transitDuration": 40,
                                                        "transitDistance": 15000,
                                                        "drivingDuration": 25,
                                                        "drivingDistance": 11000
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "rank": 3,
                                                    "stationName": "합정역",
                                                    "line": "2호선",
                                                    "latitude": 37.5495,
                                                    "longitude": 126.9139,
                                                    "distanceFromCenter": 1500,
                                                    "avgTransitDuration": 48.0,
                                                    "routes": [
                                                      {
                                                        "departureName": "김철수",
                                                        "departureAddress": "수원시 장안구 영화동",
                                                        "transitDuration": 52,
                                                        "transitDistance": 20500,
                                                        "drivingDuration": 33,
                                                        "drivingDistance": 17500
                                                      },
                                                      {
                                                        "departureName": "이영희",
                                                        "departureAddress": "서울시 강남구 역삼동",
                                                        "transitDuration": 44,
                                                        "transitDistance": 16000,
                                                        "drivingDuration": 28,
                                                        "drivingDistance": 12500
                                                      }
                                                    ]
                                                  }
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "출발지 미등록",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "출발지 없음",
                                    summary = "출발지가 하나도 등록되지 않은 경우",
                                    value = """
                                            {
                                              "code": "E416",
                                              "message": "출발지가 등록되지 않았습니다."
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "모임 없음 또는 반경 내 지하철역 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "모임 없음",
                                            summary = "모임이 존재하지 않는 경우",
                                            value = """
                                                    {
                                                      "code": "E410",
                                                      "message": "모임이 존재하지 않습니다."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "역 없음",
                                            summary = "무게중심 반경 5km 내 지하철역이 없는 경우",
                                            value = """
                                                    {
                                                      "code": "E417",
                                                      "message": "반경 내 지하철역을 찾을 수 없습니다."
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "외부 API 호출 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "API 오류",
                                    summary = "Google Distance Matrix API 호출 실패",
                                    value = """
                                            {
                                              "code": "E418",
                                              "message": "외부 API 호출에 실패했습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<MidpointRecommendationResponse>> getMidpointRecommendations(
            @Parameter(description = "모임 ID", example = "test-meeting-001", required = true)
            @RequestParam String meetingId
    ) {
        MidpointRecommendationResponse response =
                midpointRecommendationService.calculateMidpointRecommendations(meetingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/poll/{locationPollId}/votes")
    @Operation(
            summary = "출발지 투표 조회",
            description = """
                    장소 조율 시 등록된 출발지 투표 목록을 조회합니다.

                    ### 사용 시점
                    - 장소 투표 현황 페이지에서 참가자들의 출발지를 표시할 때
                    - 중간지점 추천 전 등록된 출발지를 확인할 때
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "출발지 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    summary = "출발지 2건 조회",
                                    value = """
                                            {
                                              "code": "S100",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": [
                                                {
                                                  "locationVoteId": 1,
                                                  "participantName": "김철수",
                                                  "departureLocation": "수원시 장안구 영화동",
                                                  "departureLat": 37.2994,
                                                  "departureLng": 127.0085
                                                },
                                                {
                                                  "locationVoteId": 2,
                                                  "participantName": "이영희",
                                                  "departureLocation": "서울시 강남구 역삼동",
                                                  "departureLat": 37.5007,
                                                  "departureLng": 127.0365
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "위치 투표판을 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "투표판 없음",
                                    value = """
                                            {
                                              "code": "E412",
                                              "message": "위치 투표판이 존재하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<?>> listLocationVote(
            @Parameter(description = "위치 투표판 ID", example = "1", required = true)
            @PathVariable Long locationPollId) {
        List<LocationVoteResponse> listLocationVote = locationService.listLocationVote(locationPollId);
        return ResponseEntity.ok(ApiResponse.success(listLocationVote));
    }

    @PutMapping("/vote/{locationVoteId}")
    @Operation(
            summary = "출발지 수정",
            description = """
                    등록된 출발지 정보를 수정합니다.

                    ### 사용 시점
                    - 참가자가 출발지를 잘못 입력하여 수정할 때
                    - 출발 장소가 변경되었을 때
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "출발지 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "code": "S100",
                                              "message": "요청이 성공적으로 처리되었습니다."
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
                            examples = @ExampleObject(
                                    name = "필수값 누락",
                                    summary = "출발지 주소 미입력",
                                    value = """
                                            {
                                              "code": "E103",
                                              "message": "유효성 검증 실패",
                                              "data": {
                                                "departureLocation": "must not be blank"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "출발지 투표를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "투표 없음",
                                    value = """
                                            {
                                              "code": "E415",
                                              "message": "위치 응답이 존재하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> updateLocationVote(
            @Parameter(description = "수정할 출발지 투표 ID", example = "1", required = true)
            @PathVariable Long locationVoteId,
            @Valid @RequestBody UpdateLocationVoteRequest request
    ) {
        locationService.updateLocationVote(locationVoteId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/vote")
    @Operation(
            summary = "출발지 추가",
            description = """
                    장소 조율을 위해 출발지를 추가합니다.

                    ### 사용 시점
                    - 참가자가 모임 장소 투표에 참여할 때
                    - 출발지 좌표와 주소를 함께 등록

                    ### 좌표 입력
                    - 프론트에서 Kakao/Google Maps API로 좌표 획득
                    - `departureLat`, `departureLng`는 문자열로 전달
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "출발지 추가 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "code": "S100",
                                              "message": "요청이 성공적으로 처리되었습니다."
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
                            examples = @ExampleObject(
                                    name = "필수값 누락",
                                    summary = "모임 ID 미입력",
                                    value = """
                                            {
                                              "code": "E103",
                                              "message": "유효성 검증 실패",
                                              "data": {
                                                "meetingId": "must not be blank"
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
    public ResponseEntity<ApiResponse<Void>> createLocationVote(@Valid @RequestBody CreateLocationVoteRequest request) {
        locationService.createLocationVote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @DeleteMapping("/vote/{locationVoteId}")
    @Operation(
            summary = "출발지 삭제",
            description = "등록된 출발지를 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "출발지 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "code": "S100",
                                              "message": "요청이 성공적으로 처리되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "출발지 투표를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "투표 없음",
                                    value = """
                                            {
                                              "code": "E415",
                                              "message": "위치 응답이 존재하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteLocationVote(
            @Parameter(description = "삭제할 출발지 투표 ID", example = "1", required = true)
            @PathVariable Long locationVoteId) {
        locationService.deleteLocationVote(locationVoteId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
