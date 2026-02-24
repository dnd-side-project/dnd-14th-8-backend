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
        summary = "시간 투표 확정",
        description = """
                    모임의 시간 투표 상태를 CONFIRMED로 변경합니다.

                    **주의사항**
                    - 확정 후에는 투표 옵션 수정이 불가합니다
                    - 모임에 일정 투표판(SchedulePoll)이 존재해야 합니다
                    """
)
@ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "확정 성공",
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
public @interface ConfirmSchedulePollApiDocs {
}
