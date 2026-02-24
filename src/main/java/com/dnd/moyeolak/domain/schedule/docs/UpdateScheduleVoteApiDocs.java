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
        summary = "시간 투표 수정",
        description = """
                    참여자의 시간 투표를 수정합니다.

                    **주의사항**
                    - `votedDates`는 30분 단위로 전송 (예: 09:00, 09:30, 10:00)
                    - 시간은 ISO-8601 형식 + Asia/Seoul 기준
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
                responseCode = "404",
                description = "참여자 또는 시간 투표를 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        examples = {
                                @ExampleObject(
                                        name = "참여자 없음",
                                        value = """
                                                {
                                                  "code": "E414",
                                                  "message": "참여자가 존재하지 않습니다."
                                                }
                                                """
                                ),
                                @ExampleObject(
                                        name = "시간 투표 없음",
                                        value = """
                                                {
                                                  "code": "E416",
                                                  "message": "시간 응답이 존재하지 않습니다."
                                                }
                                                """
                                )
                        }
                )
        )
})
public @interface UpdateScheduleVoteApiDocs {
}
