package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.dto.LocationVoteResponse;

import java.util.List;

public interface LocationService {

    List<LocationVoteResponse> listLocationVote(Long locationPollId);

    void createLocationVote(CreateLocationVoteRequest request);

    void deleteLocationVote(Long locationVoteId);
}
