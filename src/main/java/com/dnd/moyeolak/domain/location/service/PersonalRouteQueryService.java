package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.dto.PersonalRouteResponse;
import com.dnd.moyeolak.domain.location.enums.RouteMode;

import java.time.LocalDateTime;

public interface PersonalRouteQueryService {

    PersonalRouteResponse getPersonalRoute(
            String meetingId,
            Long stationId,
            Long participantId,
            LocalDateTime departureTime,
            RouteMode mode
    );
}
