package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.dto.LocationVoteResponse;
import com.dnd.moyeolak.domain.meeting.dto.UpdateLocationVoteRequest;

import java.util.List;

public interface LocationVoteService {

    List<LocationVoteResponse> listLocationVote(String meetingId);

    Long createLocationVote(CreateLocationVoteRequest createLocationVoteRequest);

    void updateLocationVote(Long locationVoteId, UpdateLocationVoteRequest updateLocationVoteRequest);

    void deleteLocationVote(Long locationVoteId);
}
