package com.dnd.moyeolak.domain.meeting.dto;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record GetMeetingScheduleResponse(
        String meetingId,
        int participantCount,
        int votedParticipantCount,
        List<ParticipantResponse> participants,
        List<LocalDate> dateOptions,
        int startTime,
        int endTime
) {
    public record ParticipantResponse(
            String name,
            List<LocalDateTime> votedDates
    ) {}

    public static GetMeetingScheduleResponse from(Meeting meeting) {
        List<ParticipantResponse> participantResponses = meeting.getParticipants().stream()
                .map(participant -> {
                    List<LocalDateTime> votedDates = meeting.getSchedulePoll().getScheduleVotes().stream()
                            .filter(vote -> vote.getParticipant().equals(participant))
                            .map(ScheduleVote::getVotedDate)
                            .toList();
                    return new ParticipantResponse(participant.getName(), votedDates);
                })
                .toList();

        int votedParticipantCount = (int) meeting.getParticipants().stream()
                .filter(p -> !p.getLocationVotes().isEmpty())
                .count();

        List<LocalDate> dateOptions = meeting.getSchedulePoll().getDateOptions();

        return new GetMeetingScheduleResponse(
                meeting.getMeetingId(),
                meeting.getParticipantCount(),
                votedParticipantCount,
                participantResponses,
                dateOptions,
                meeting.getSchedulePoll().getStartTime(),
                meeting.getSchedulePoll().getEndTime()
        );
    }
}
