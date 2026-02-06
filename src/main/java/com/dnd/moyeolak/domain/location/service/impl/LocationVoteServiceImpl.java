package com.dnd.moyeolak.domain.location.service.impl;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.location.repository.LocationPollRepository;
import com.dnd.moyeolak.domain.location.service.LocationVoteService;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationVoteServiceImpl implements LocationVoteService {

    private final LocationPollRepository locationPollRepository;

    @Override
    @Transactional
    public LocationVote createVote(Meeting meeting, Participant participant,
                                   String address, BigDecimal latitude, BigDecimal longitude) {
        LocationPoll locationPoll = locationPollRepository.findByMeeting(meeting)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCATION_POLL_NOT_FOUND));

        return LocationVote.of(locationPoll, participant, address, latitude, longitude);
    }
}