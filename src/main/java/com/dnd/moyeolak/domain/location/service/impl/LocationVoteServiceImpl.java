package com.dnd.moyeolak.domain.location.service.impl;

import com.dnd.moyeolak.domain.location.dto.CreateLocationVoteRequest;
import com.dnd.moyeolak.domain.location.dto.LocationVoteResponse;
import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.repository.LocationVoteRepository;
import com.dnd.moyeolak.domain.location.service.LocationVoteService;
import com.dnd.moyeolak.domain.meeting.dto.UpdateLocationVoteRequest;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.participant.service.ParticipantService;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationVoteServiceImpl implements LocationVoteService {

    private final MeetingRepository meetingRepository;
    private final ParticipantService participantService;
    private final LocationVoteRepository locationVoteRepository;

    @Override
    public List<LocationVoteResponse> listLocationVote(Long locationPollId) {
        return locationVoteRepository.findByLocationPoll_Id(locationPollId)
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
    public Long createLocationVote(CreateLocationVoteRequest request) {
        Meeting meeting = meetingRepository.findByIdWithAllAssociations(request.meetingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        LocationPoll locationPoll = meeting.getLocationPoll();
        LocationVote locationVote = LocationVote.fromByCreateLocationVoteRequest(locationPoll, request);

        if (!StringUtils.hasText(request.localStorageKey())) {
            locationVoteRepository.save(locationVote);
            return locationVote.getId();
        }

        Optional<Participant> existingParticipant = meeting.getParticipants().stream()
                .filter(p -> request.localStorageKey().equals(p.getLocalStorageKey()))
                .findFirst();

        if (existingParticipant.isPresent()) {
            Participant host = existingParticipant.get();
            if (!host.isHost() || !host.getLocationVotes().isEmpty()) {
                throw new BusinessException(ErrorCode.DUPLICATE_LOCAL_STORAGE_KEY);
            }
            host.updateName(request.participantName());
            host.addLocationVote(locationVote);
            locationVoteRepository.save(locationVote);
            return locationVote.getId();
        }

        Participant participant = Participant.of(
                Meeting.ofId(request.meetingId()), request.localStorageKey(), request.participantName(), locationVote
        );
        participantService.save(participant);

        return locationVote.getId();
    }

    @Override
    @Transactional
    public void deleteLocationVote(Long locationVoteId) {
        LocationVote locationVote = locationVoteRepository.findById(locationVoteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_VOTE_NOT_FOUND));
        locationVoteRepository.delete(locationVote);
    }
}
