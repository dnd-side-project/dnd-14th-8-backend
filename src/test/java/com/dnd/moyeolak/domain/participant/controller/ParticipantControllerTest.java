package com.dnd.moyeolak.domain.participant.controller;

import com.dnd.moyeolak.domain.participant.dto.CreateParticipantResponse;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithLocationRequest;
import com.dnd.moyeolak.domain.participant.dto.CreateParticipantWithScheduleRequest;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("일정 투표와 함께 참여 API 는 201 상태와 응답 본문을 반환한다")
    void joinWithSchedule_returnsCreatedResponse() throws Exception {
        CreateParticipantWithScheduleRequest request = new CreateParticipantWithScheduleRequest(
                "홍길동",
                "local-key",
                List.of(
                        LocalDateTime.of(2025, 2, 10, 9, 0),
                        LocalDateTime.of(2025, 2, 10, 10, 0)
                )
        );
        CreateParticipantResponse response = new CreateParticipantResponse(
                1L,
                request.name(),
                request.availableSchedules().size(),
                false,
                LocalDateTime.now()
        );
        when(participantService.createWithSchedule(eq(MEETING_ID), any(CreateParticipantWithScheduleRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/participants/join-with-schedule")
                        .param("meetingId", MEETING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(SuccessCode.RESOURCE_CREATED.getCode()))
                .andExpect(jsonPath("$.data.participantId").value(response.participantId()))
                .andExpect(jsonPath("$.data.scheduleVoteCount").value(response.scheduleVoteCount()))
                .andExpect(jsonPath("$.data.hasLocation").value(false));

        ArgumentCaptor<CreateParticipantWithScheduleRequest> captor =
                ArgumentCaptor.forClass(CreateParticipantWithScheduleRequest.class);
        verify(participantService).createWithSchedule(eq(MEETING_ID), captor.capture());
        assertThat(captor.getValue()).isEqualTo(request);
    }

    @Test
    @DisplayName("위치 투표와 함께 참여 API 는 201 상태와 응답 본문을 반환한다")
    void joinWithLocation_returnsCreatedResponse() throws Exception {
        CreateParticipantWithLocationRequest request = new CreateParticipantWithLocationRequest(
                "이영희",
                "local-key",
                new CreateParticipantWithLocationRequest.LocationInput(
                        new BigDecimal("37.5665"),
                        new BigDecimal("126.9780"),
                        "서울시 중구 명동"
                )
        );
        CreateParticipantResponse response = new CreateParticipantResponse(
                2L,
                request.name(),
                null,
                true,
                LocalDateTime.now()
        );
        when(participantService.createWithLocation(eq(MEETING_ID), any(CreateParticipantWithLocationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/participants/join-with-location")
                        .param("meetingId", MEETING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(SuccessCode.RESOURCE_CREATED.getCode()))
                .andExpect(jsonPath("$.data.participantId").value(response.participantId()))
                .andExpect(jsonPath("$.data.hasLocation").value(true))
                .andExpect(jsonPath("$.data.scheduleVoteCount").doesNotExist());

        ArgumentCaptor<CreateParticipantWithLocationRequest> captor =
                ArgumentCaptor.forClass(CreateParticipantWithLocationRequest.class);
        verify(participantService).createWithLocation(eq(MEETING_ID), captor.capture());
        assertThat(captor.getValue()).isEqualTo(request);
    }

    @Test
    @DisplayName("일정 투표와 함께 참여 API 는 요청 검증 실패 시 400을 반환한다")
    void joinWithSchedule_returnsBadRequestOnValidationError() throws Exception {
        String invalidPayload = """
                {
                  "name": "",
                  "localStorageKey": "",
                  "availableSchedules": []
                }
                """;

        mockMvc.perform(post("/api/participants/join-with-schedule")
                        .param("meetingId", MEETING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_PARAMETER.getCode()))
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.localStorageKey").exists())
                .andExpect(jsonPath("$.data.availableSchedules").exists());

        verifyNoInteractions(participantService);
    }

    @Test
    @DisplayName("위치 투표와 함께 참여 API 는 비즈니스 예외 발생 시 에러 응답을 반환한다")
    void joinWithLocation_returnsErrorResponseWhenFacadeThrowsBusinessException() throws Exception {
        CreateParticipantWithLocationRequest request = new CreateParticipantWithLocationRequest(
                "이영희",
                "local-key",
                new CreateParticipantWithLocationRequest.LocationInput(
                        new BigDecimal("37.5665"),
                        new BigDecimal("126.9780"),
                        "서울시 중구 명동"
                )
        );
        when(participantService.createWithLocation(eq(MEETING_ID), any(CreateParticipantWithLocationRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.LOCATION_POLL_NOT_FOUND));

        mockMvc.perform(post("/api/participants/join-with-location")
                        .param("meetingId", MEETING_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(ErrorCode.LOCATION_POLL_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.code").value(ErrorCode.LOCATION_POLL_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.LOCATION_POLL_NOT_FOUND.getMessage()));

        verify(participantService).createWithLocation(eq(MEETING_ID), any(CreateParticipantWithLocationRequest.class));
    }
}