package com.dnd.moyeolak.domain.participant.docs;

import com.dnd.moyeolak.domain.participant.dto.GetParticipantResponse;
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
@Operation(summary = "참여자 단건 조회", description = """
            특정 참여자의 정보를 조회합니다.

            ### 참여자 ID 취득 방법
            - `/api/participants/join-with-schedule` 또는 `/api/participants/join-with-location` 성공 응답의 `data.participantId`
            """)
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "참여자 조회 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = GetParticipantResponse.class),
                        examples = @ExampleObject(
                                name = "성공 응답",
                                value = """
                                            {
                                              "code": "S001",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "participantId": 1,
                                                "name": "김철수",
                                                "isHost": false
                                              }
                                            }
                                            """
                        )
                )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "참여자를 찾을 수 없음",
                content = @Content(
                        mediaType = "application/json",
                        examples = @ExampleObject(
                                name = "참여자 없음",
                                value = """
                                            {
                                              "code": "E414",
                                              "message": "참여자가 존재하지 않습니다."
                                            }
                                            """
                        )
                )
        )
})
public @interface GetParticipantApiDocs {
}
