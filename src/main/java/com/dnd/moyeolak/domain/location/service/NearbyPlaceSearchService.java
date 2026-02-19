package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.dto.NearbyPlaceSearchResponse;

public interface NearbyPlaceSearchService {

    NearbyPlaceSearchResponse nearbyPlaceSearch(String latitude, String longitude);

}
