package com.dnd.moyeolak.domain.schedule.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateScheduleVoteRequest(
    Long participantId,
    String participantName,
    List<LocalDateTime> votedDates
) {
}
