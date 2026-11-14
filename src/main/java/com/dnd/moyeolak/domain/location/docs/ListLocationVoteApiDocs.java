package com.dnd.moyeolak.domain.location.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "출발지 투표 조회",
        description = """
                    장소 조율 시 등록된 출발지 투표 목록을 조회합니다. 모임 ID를 쿼리 파라미터(`meetingId`)로 전달하면 자동으로 해당 모임의 위치 투표판을 찾아줍니다.

                    ### 사용 시점
                    - 장소 투표 현황 페이지에서 참가자들의 출발지를 표시할 때
                    - 중간지점 추천 전 등록된 출발지를 확인할 때

                    ### 출발지 주소
                    - `departureLocation`: 검색으로 고르면 장소 이름, 현 위치나 주소로 고르면 주소
                    - `departureAddress`: 장소 이름으로 고른 경우 고를 때 본 주소. 그 밖의 경우와 이 값이 생기기 전에 등록된 출발지는 `null`
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
                                                  "departureAddress": null,
                                                  "departureLat": 37.2994,
                                                  "departureLng": 127.0085
                                                },
                                                {
                                                  "locationVoteId": 2,
                                                  "participantName": "이영희",
                                                  "departureLocation": "서대문역 5호선",
                                                  "departureAddress": "서울 종로구 통일로 지하 126",
                                                  "departureLat": 37.5657,
                                                  "departureLng": 126.9666
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
public @interface ListLocationVoteApiDocs {
}
