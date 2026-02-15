package com.dnd.moyeolak.domain.schedule.repository;

import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleVoteRepository extends JpaRepository<ScheduleVote, Long> {
   List<ScheduleVote> findAllBySchedulePollId(Long schedulePollId);
}
