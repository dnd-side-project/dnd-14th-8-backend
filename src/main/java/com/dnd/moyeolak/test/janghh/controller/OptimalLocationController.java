package com.dnd.moyeolak.test.janghh.controller;

import com.dnd.moyeolak.global.response.ApiResponse;
import com.dnd.moyeolak.test.janghh.dto.request.OptimalLocationRequest;
import com.dnd.moyeolak.test.janghh.dto.response.OptimalLocationResponse;
import com.dnd.moyeolak.test.janghh.service.OptimalLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Optimal Location", description = "참가자 위치를 바탕으로 최적 만남 장소를 추천하는 API")
@RestController
@RequestMapping("/api/test/janghh/optimal-location")
@RequiredArgsConstructor
public class OptimalLocationController {

    private final OptimalLocationService optimalLocationService;

    @PostMapping
    @Operation(
        summary = "최적 만남 장소 추천",
        description = "참여자의 위도/경도 좌표를 받아 모임 중심점을 계산하고, 카카오/ODsay 탐색 결과를 기반으로 최적 장소 3곳을 추천합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "추천 계산 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiResponse.class),
            examples = @ExampleObject(
                name = "성공 응답 예시",
                value = """
                        {
                          "code": "S100",
                          "data": {
                            "centerPoint": {
                              "latitude": 37.54217301784178,
                              "longitude": 126.93569223930636
                            },
                            "recommendations": [
                              {
                                "rank": 1,
                                "name": "마포역 5호선",
                                "category": "지하철역",
                                "address": "서울 마포구 도화동 160",
                                "latitude": 37.53955402908912,
                                "longitude": 126.94586257221307,
                                "distanceFromCenter": 944,
                                "minSum": 82,
                                "minMax": 35,
                                "avgDuration": 27.333333333333332,
                                "routes": [
                                  {
                                    "participantName": "참여자1",
                                    "duration": 28,
                                    "distance": 16128,
                                    "payment": 1750,
                                    "transitCount": 2
                                  },
                                  {
                                    "participantName": "참여자2",
                                    "duration": 35,
                                    "distance": 13922,
                                    "payment": 2350,
                                    "transitCount": 3
                                  },
                                  {
                                    "participantName": "참여자3",
                                    "duration": 19,
                                    "distance": 5217,
                                    "payment": 1550,
                                    "transitCount": 1
                                  }
                                ]
                              },
                              {
                                "rank": 2,
                                "name": "서강대역 경의중앙선",
                                "category": "지하철역",
                                "address": "서울 마포구 노고산동 112-5",
                                "latitude": 37.5521384864879,
                                "longitude": 126.935507621173,
                                "distanceFromCenter": 1106,
                                "minSum": 91,
                                "minMax": 44,
                                "avgDuration": 30.333333333333332,
                                "routes": [
                                  {
                                    "participantName": "참여자1",
                                    "duration": 27,
                                    "distance": 15233,
                                    "payment": 1650,
                                    "transitCount": 2
                                  },
                                  {
                                    "participantName": "참여자2",
                                    "duration": 44,
                                    "distance": 12033,
                                    "payment": 1500,
                                    "transitCount": 1
                                  },
                                  {
                                    "participantName": "참여자3",
                                    "duration": 20,
                                    "distance": 4341,
                                    "payment": 1550,
                                    "transitCount": 1
                                  }
                                ]
                              },
                              {
                                "rank": 3,
                                "name": "대흥역 6호선",
                                "category": "지하철역",
                                "address": "서울 마포구 대흥동 128-1",
                                "latitude": 37.5476479056751,
                                "longitude": 126.942473188734,
                                "distanceFromCenter": 853,
                                "minSum": 100,
                                "minMax": 39,
                                "avgDuration": 33.333333333333336,
                                "routes": [
                                  {
                                    "participantName": "참여자1",
                                    "duration": 33,
                                    "distance": 18028,
                                    "payment": 1750,
                                    "transitCount": 2
                                  },
                                  {
                                    "participantName": "참여자2",
                                    "duration": 39,
                                    "distance": 11222,
                                    "payment": 1500,
                                    "transitCount": 1
                                  },
                                  {
                                    "participantName": "참여자3",
                                    "duration": 28,
                                    "distance": 5589,
                                    "payment": 1500,
                                    "transitCount": 1
                                  }
                                ]
                              }
                            ]
                          },
                          "message": "요청이 성공적으로 처리되었습니다."
                        }
                        """
            )
        )
    )
    public ApiResponse<OptimalLocationResponse> findOptimalLocations(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "참가자들의 위도/경도 좌표 목록",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OptimalLocationRequest.class),
                examples = @ExampleObject(
                    name = "요청 예시",
                    value = """
                            {
                              "participants": [
                                { "name": "참여자1", "latitude": 37.5621, "longitude": 126.8015 },
                                { "name": "참여자2", "latitude": 37.4980, "longitude": 127.0276 },
                                { "name": "참여자3", "latitude": 37.5663, "longitude": 126.9779 }
                              ]
                            }
                            """
                )
            )
        )
        @Valid @RequestBody OptimalLocationRequest request
    ) {
        log.info("최적 만남 장소 추천 요청: 참가자 {}명", request.participants().size());

        OptimalLocationResponse response = optimalLocationService.findOptimalLocations(request);

        log.info("최적 만남 장소 추천 완료: {}개 장소", response.recommendations().size());

        return ApiResponse.success(response);
    }
}
