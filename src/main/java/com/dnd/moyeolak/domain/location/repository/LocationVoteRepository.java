package com.dnd.moyeolak.domain.location.repository;

import com.dnd.moyeolak.domain.location.entity.LocationVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationVoteRepository extends JpaRepository<LocationVote, Long> {
}
