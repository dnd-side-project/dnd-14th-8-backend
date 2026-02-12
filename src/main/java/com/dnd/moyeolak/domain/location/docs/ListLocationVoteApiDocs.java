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
public @interface ListLocationVoteApiDocs {
}
