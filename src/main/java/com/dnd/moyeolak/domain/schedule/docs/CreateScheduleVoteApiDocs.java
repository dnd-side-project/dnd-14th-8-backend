package com.dnd.moyeolak.domain.schedule.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "시간 투표 생성",
        description = """
                참여자 생성과 동시에 가능한 시간대에 투표합니다.

                ### 사용 시점
                - 모임 생성자가 일정 투표를 시작한 후
                - 참여자가 자신의 가능한 시간대를 선택할 때

                ### 주의사항
                - `votedDates`는 30분 단위로 전송 (예: 09:00, 09:30, 10:00)
                - 시간은 ISO-8601 형식 + Asia/Seoul 기준
                - `localStorageKey`는 브라우저별 고유값 (재참여 방지용)
                """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "시간 투표 생성 성공",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "성공 응답",
                                summary = "시간 투표 생성 성공",
                                value = """
                                        {
                                          "code": "S101",
                                          "message": "리소스가 성공적으로 생성되었습니다.",
                                          "data": null
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
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
                                                    "participantName": "이름은 필수입니다"
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
                                                    "votedDates": "가능한 시간 정보는 필수입니다"
                                                  }
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
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
        ),
        @ApiResponse(
                responseCode = "409",
                description = "이미 참여한 사용자",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "중복 참여",
                                value = """
                                        {
                                          "code": "E413",
                                          "message": "이미 참여한 사용자입니다."
                                        }
                                        """
                        )
                )
        )
})
public @interface CreateScheduleVoteApiDocs {
}
