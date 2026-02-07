package com.dnd.moyeolak.domain.location.service.impl;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.dto.LocationVoteResponse;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.repository.LocationVoteRepository;
import com.dnd.moyeolak.domain.location.service.LocationService;
import com.dnd.moyeolak.domain.meeting.dto.UpdateLocationVoteRequest;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final ParticipantService participantService;
    private final LocationVoteRepository locationVoteRepository;

    @Override
    public List<LocationVoteResponse> listLocationVote(Long locationPollId) {
        return locationVoteRepository.findByLocationPoll_LocationPollId(locationPollId)
                .stream().map(LocationVoteResponse::from).toList();
    }

    @Override
    @Transactional
    public void updateLocationVote(Long locationVoteId, UpdateLocationVoteRequest request) {
        LocationVote locationVote = locationVoteRepository.findById(locationVoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_VOTE_NOT_FOUND));
        locationVote.update(request);
    }

    @Override
    @Transactional
    public void createLocationVote(CreateLocationVoteRequest request) {
        LocationVote locationVote = LocationVote.fromByCreateLocationVoteRequest(request);
        if (StringUtils.hasText(request.localStorageKey())) {
            Participant participant = Participant.of(
                    Meeting.ofId(request.meetingId()), request.localStorageKey(), request.participantName(), locationVote
            );
            participantService.save(participant);
        } else {
            locationVoteRepository.save(locationVote);
        }
    }

    @Override
    @Transactional
    public void deleteLocationVote(Long locationVoteId) {
        LocationVote locationVote = locationVoteRepository.findById(locationVoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_VOTE_NOT_FOUND));
        locationVoteRepository.delete(locationVote);
    }
}
