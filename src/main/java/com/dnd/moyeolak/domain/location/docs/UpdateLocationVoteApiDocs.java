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
public @interface UpdateLocationVoteApiDocs {
}
