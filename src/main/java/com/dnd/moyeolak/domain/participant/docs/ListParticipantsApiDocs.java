package com.dnd.moyeolak.domain.participant.docs;

import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "참여자 전체 조회", description = """
            모임에 참여한 모든 참여자 목록을 조회합니다.

            ### 사용 시점
            - 모임 상세 페이지에서 참여자 목록을 표시할 때
            """)
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "참여자 목록 조회 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ListParticipantResponse.class),
                        examples = @ExampleObject(
                                name = "성공 응답",
                                value = """
                                            {
                                              "code": "S100",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "participants": [
                                                  {
                                                    "participantId": 1,
                                                    "name": "김철수",
                                                    "isHost": true
                                                  },
                                                  {
                                                    "participantId": 2,
                                                    "name": "이영희",
                                                    "isHost": false
                                                  }
                                                ],
                                                "totalCount": 2
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
public @interface ListParticipantsApiDocs {
}
