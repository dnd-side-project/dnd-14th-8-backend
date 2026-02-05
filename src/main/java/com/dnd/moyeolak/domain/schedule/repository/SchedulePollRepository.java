package com.dnd.moyeolak.domain.schedule.repository;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchedulePollRepository extends JpaRepository<SchedulePoll, Long> {

    Optional<SchedulePoll> findByMeeting(Meeting meeting);
}
