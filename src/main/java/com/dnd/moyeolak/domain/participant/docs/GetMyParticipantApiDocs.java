package com.dnd.moyeolak.domain.participant.docs;

import com.dnd.moyeolak.domain.participant.dto.ParticipantResponse;
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
@Operation(summary = "본인 참여자 정보 조회", description = """
            모임 ID와 LocalStorageKey를 통해 본인의 참여자 정보를 조회합니다.

            ### 사용 시점
            - 모임 입장 시 본인의 참여자 ID를 아직 모를 때 (링크를 통해 모임 입장 시)
            - 이후 참여자 PK 기반 API 호출을 위해 참여자 관련 정보를 취득할 때 (추후 응답데이터 수정에 사용)
            """)
@ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "본인 참여자 조회 성공",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ParticipantResponse.class),
                        examples = @ExampleObject(
                                name = "성공 응답",
                                value = """
                                            {
                                              "code": "S100",
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "participantId": 1,
                                                "localStorageKey": "abc-def-123",
                                                "name": "김철수",
                                                "scheduleVoteId": 1,
                                                "locationVoteId": 1,
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
public @interface GetMyParticipantApiDocs {
}
