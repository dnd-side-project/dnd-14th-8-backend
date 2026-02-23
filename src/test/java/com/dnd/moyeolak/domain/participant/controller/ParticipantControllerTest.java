package com.dnd.moyeolak.domain.participant.controller;

import com.dnd.moyeolak.domain.participant.dto.ListParticipantResponse;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.exception.GlobalExceptionAdvice;
import com.dnd.moyeolak.global.response.ErrorCode;
import com.dnd.moyeolak.global.response.SuccessCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ParticipantControllerTest {

    private static final String MEETING_ID = "meeting-id";

    @Mock
    private ParticipantService participantService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ParticipantController controller = new ParticipantController(participantService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionAdvice())
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("참여자 전체 조회 API는 200 상태와 참여자 목록을 반환한다")
    void listParticipants_returnsOkResponse() throws Exception {
        // Given
        ListParticipantResponse response = new ListParticipantResponse(
                List.of(
                        new ListParticipantResponse.ParticipantInfo(1L, "김철수", "session-key1", true),
                        new ListParticipantResponse.ParticipantInfo(2L, "이영희", "session-key2",false)
                ),
                2
        );
        when(participantService.listParticipants(MEETING_ID)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/participants")
                        .param("meetingId", MEETING_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.OK.getCode()))
                .andExpect(jsonPath("$.data.totalCount").value(2))
                .andExpect(jsonPath("$.data.participants[0].participantId").value(1))
                .andExpect(jsonPath("$.data.participants[0].name").value("김철수"))
                .andExpect(jsonPath("$.data.participants[0].localStorageKey").value("session-key1"))
                .andExpect(jsonPath("$.data.participants[0].isHost").value(true))
                .andExpect(jsonPath("$.data.participants[1].participantId").value(2))
                .andExpect(jsonPath("$.data.participants[1].name").value("이영희"))
                .andExpect(jsonPath("$.data.participants[1].localStorageKey").value("session-key2"))
                .andExpect(jsonPath("$.data.participants[1].isHost").value(false));

        verify(participantService).listParticipants(MEETING_ID);
    }

    @Test
    @DisplayName("참여자 전체 조회 API는 모임을 찾을 수 없으면 404를 반환한다")
    void listParticipants_returnsNotFound() throws Exception {
        // Given
        when(participantService.listParticipants(MEETING_ID))
                .thenThrow(new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        // When & Then
        mockMvc.perform(get("/api/participants")
                        .param("meetingId", MEETING_ID))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.MEETING_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEETING_NOT_FOUND.getMessage()));

        verify(participantService).listParticipants(MEETING_ID);
    }

}
