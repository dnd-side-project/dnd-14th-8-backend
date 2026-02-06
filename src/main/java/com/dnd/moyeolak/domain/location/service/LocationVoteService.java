package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.entity.LocationVote;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;

import java.math.BigDecimal;

public interface LocationVoteService {

    LocationVote createVote(Meeting meeting, Participant participant, String address, BigDecimal latitude, BigDecimal longitude);
}