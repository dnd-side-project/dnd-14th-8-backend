package com.dnd.moyeolak.domain.schedule.docs;

import io.swagger.v3.oas.annotations.Operation;
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
        summary = "일정 투표 옵션 수정",
        description = """
                    모임의 일정 투표 옵션(날짜 목록, 시작/종료 시간)을 수정합니다.

                    **제약 조건**
                    - `dateOptions`: 최소 1개 이상의 날짜 필수 (빈 배열 불가)
                    - `startTime`: HH:mm 형식, 30분 단위만 허용, 24:00 불가, 종료 시간보다 빨라야 함
                    - `endTime`: HH:mm 또는 24:00, 30분 단위만 허용, 24:00 = 자정

                    **투표 옵션 변경 시 자동 처리**
                    - 새 범위를 벗어난 기존 투표 슬롯은 자동 삭제됩니다
                    - **팀원(비호스트)**: 모든 투표가 무효화되어도 참여자는 유지됩니다 (이름, 출발지 정보 포함)
                      → 동일한 localStorageKey로 재투표 가능합니다
                    - **모임장**: 모든 투표가 무효화되어도 참여자는 유지됩니다
                      → 새 옵션으로 재투표 가능합니다
                    """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "수정 성공",
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
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "날짜 목록 비어있음",
                                        summary = "dateOptions 빈 배열",
                                        value = """
                                                {
                                                  "code": "E103",
                                                  "message": "유효성 검증 실패",
                                                  "data": {
                                                    "dateOptions": "날짜 목록은 최소 1개 이상이어야 합니다"
                                                  }
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "시간 범위 오류",
                                        summary = "startTime >= endTime",
                                        value = """
                                                {
                                                  "code": "E426",
                                                  "message": "시작 시간은 종료 시간보다 빨라야 합니다."
                                                }
                                                """
                                )
                        }
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "모임 또는 일정 투표판을 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "모임 없음",
                                        value = """
                                                {
                                                  "code": "E410",
                                                  "message": "모임이 존재하지 않습니다."
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "일정 투표판 없음",
                                        value = """
                                                {
                                                  "code": "E411",
                                                  "message": "일정 투표판이 존재하지 않습니다."
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface UpdateSchedulePollApiDocs {
}
