package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.dto.MidpointRecommendationResponse;

public interface MidpointRecommendationService {

    MidpointRecommendationResponse calculateMidpointRecommendations(String meetingId);
}
