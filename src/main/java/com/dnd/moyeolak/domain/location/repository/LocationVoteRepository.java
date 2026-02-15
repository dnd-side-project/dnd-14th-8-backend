package com.dnd.moyeolak.domain.location.repository;

import com.dnd.moyeolak.domain.location.entity.LocationVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationVoteRepository extends JpaRepository<LocationVote, Long> {
    List<LocationVote> findByLocationPoll_Id(Long locationPollId);
}
