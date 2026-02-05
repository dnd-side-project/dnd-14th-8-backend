package com.dnd.moyeolak.domain.participant.repository;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    boolean existsByMeetingAndLocalStorageKey(Meeting meeting, String localStorageKey);
}
