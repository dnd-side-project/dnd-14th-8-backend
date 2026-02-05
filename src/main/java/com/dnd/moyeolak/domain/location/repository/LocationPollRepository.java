package com.dnd.moyeolak.domain.location.repository;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationPollRepository extends JpaRepository<LocationPoll, Long> {

    Optional<LocationPoll> findByMeeting(Meeting meeting);
}
