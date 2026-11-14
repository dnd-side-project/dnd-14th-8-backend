package com.dnd.moyeolak.domain.location.controller;

import com.dnd.moyeolak.domain.location.dto.CenterPointDto;
import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.dto.LocationVoteResponse;
import com.dnd.moyeolak.domain.location.dto.MidpointRecommendationResponse;
import com.dnd.moyeolak.domain.location.enums.MidpointResultType;
import com.dnd.moyeolak.domain.location.service.LocationVoteService;
import com.dnd.moyeolak.domain.location.service.MidpointRecommendationService;
import com.dnd.moyeolak.domain.location.service.NearbyPlaceSearchService;
import com.dnd.moyeolak.domain.location.service.PersonalRouteQueryService;
import com.dnd.moyeolak.domain.meeting.dto.UpdateLocationVoteRequest;
import com.dnd.moyeolak.global.exception.GlobalExceptionAdvice;
import com.dnd.moyeolak.global.ratelimit.ClientIpResolver;
import com.dnd.moyeolak.global.response.SuccessCode;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    private static final String MEETING_ID = "meeting-123";

    @Mock
    private LocationVoteService locationVoteService;

    @Mock
    private MidpointRecommendationService midpointRecommendationService;

    @Mock
    private PersonalRouteQueryService personalRouteQueryService;

    @Mock
    private NearbyPlaceSearchService nearbyPlaceSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocationController controller = new LocationController(
                locationVoteService,
                midpointRecommendationService,
                personalRouteQueryService,
                nearbyPlaceSearchService,
                new ClientIpResolver()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionAdvice())
                .build();
    }

    @Test
    @DisplayName("중간지점 추천 API는 X-Forwarded-For 첫 번째 IP를 서비스에 전달한다")
    void getMidpointRecommendations_passesResolvedClientIp() throws Exception {
        MidpointRecommendationResponse response = new MidpointRecommendationResponse(
                new CenterPointDto(37.5, 127.0),
                List.of(),
                null,
                2,
                5,
                MidpointResultType.NORMAL
        );
        when(midpointRecommendationService.calculateMidpointRecommendations(
                eq(MEETING_ID), eq(null), eq("203.0.113.10")))
                .thenReturn(response);

        mockMvc.perform(get("/api/locations/midpoint-recommendations")
                        .param("meetingId", MEETING_ID)
                        .header("X-Forwarded-For", "203.0.113.10, 10.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SuccessCode.OK.getCode()))
                .andExpect(jsonPath("$.data.registeredCount").value(2))
                .andExpect(jsonPath("$.data.totalCount").value(5));

        verify(midpointRecommendationService)
                .calculateMidpointRecommendations(MEETING_ID, null, "203.0.113.10");
    }

    @Test
    @DisplayName("출발지 목록 API는 departureAddress를 내려주고, 없으면 null로 내려준다")
    void listLocationVote_includesDepartureAddress() throws Exception {
        when(locationVoteService.listLocationVote(MEETING_ID)).thenReturn(List.of(
                new LocationVoteResponse(1L, "홍길동", "서대문역 5호선", "서울 종로구 통일로 지하 126",
                        new BigDecimal("37.5657"), new BigDecimal("126.9666")),
                new LocationVoteResponse(2L, "김철수", "서울 강남구 역삼동", null,
                        new BigDecimal("37.5007"), new BigDecimal("127.0365"))
        ));

        mockMvc.perform(get("/api/locations/vote").param("meetingId", MEETING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].departureLocation").value("서대문역 5호선"))
                .andExpect(jsonPath("$.data[0].departureAddress").value("서울 종로구 통일로 지하 126"))
                .andExpect(jsonPath("$.data[1].departureAddress").value(nullValue()));
    }

    @Test
    @DisplayName("출발지 추가 API는 요청 본문의 departureAddress를 서비스에 전달한다")
    void createLocationVote_bindsDepartureAddress() throws Exception {
        when(locationVoteService.createLocationVote(any())).thenReturn(1L);

        mockMvc.perform(post("/api/locations/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "meetingId": "%s",
                                  "participantName": "홍길동",
                                  "departureLocation": "서대문역 5호선",
                                  "departureAddress": "서울 종로구 통일로 지하 126",
                                  "departureLat": "37.5657",
                                  "departureLng": "126.9666"
                                }
                                """.formatted(MEETING_ID)))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateLocationVoteRequest> captor = ArgumentCaptor.forClass(CreateLocationVoteRequest.class);
        verify(locationVoteService).createLocationVote(captor.capture());
        assertThat(captor.getValue().departureAddress()).isEqualTo("서울 종로구 통일로 지하 126");
    }

    @Test
    @DisplayName("출발지 수정 API는 departureAddress가 없는 요청도 받아 null로 전달한다")
    void updateLocationVote_withoutDepartureAddress_bindsNull() throws Exception {
        mockMvc.perform(put("/api/locations/vote/{locationVoteId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantName": "홍길동",
                                  "departureLocation": "서울 마포구 합정동",
                                  "departureLat": "37.5495",
                                  "departureLng": "126.9139"
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<UpdateLocationVoteRequest> captor = ArgumentCaptor.forClass(UpdateLocationVoteRequest.class);
        verify(locationVoteService).updateLocationVote(eq(1L), captor.capture());
        assertThat(captor.getValue().departureAddress()).isNull();
    }

    @Test
    @DisplayName("출발지 추가 API는 255자를 넘는 departureAddress를 400으로 거절한다")
    void createLocationVote_rejectsTooLongDepartureAddress() throws Exception {
        mockMvc.perform(post("/api/locations/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "meetingId": "%s",
                                  "participantName": "홍길동",
                                  "departureLocation": "서대문역 5호선",
                                  "departureAddress": "%s",
                                  "departureLat": "37.5657",
                                  "departureLng": "126.9666"
                                }
                                """.formatted(MEETING_ID, "가".repeat(256))))
                .andExpect(status().isBadRequest());

        verify(locationVoteService, never()).createLocationVote(any());
    }

    @Test
    @DisplayName("출발지 수정 API는 255자를 넘는 departureAddress를 400으로 거절한다")
    void updateLocationVote_rejectsTooLongDepartureAddress() throws Exception {
        mockMvc.perform(put("/api/locations/vote/{locationVoteId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participantName": "홍길동",
                                  "departureLocation": "서대문역 5호선",
                                  "departureAddress": "%s",
                                  "departureLat": "37.5657",
                                  "departureLng": "126.9666"
                                }
                                """.formatted("가".repeat(256))))
                .andExpect(status().isBadRequest());

        verify(locationVoteService, never()).updateLocationVote(any(), any());
    }
}
