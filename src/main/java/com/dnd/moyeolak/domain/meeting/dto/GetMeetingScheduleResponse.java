package com.dnd.moyeolak.domain.meeting.dto;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record GetMeetingScheduleResponse(
        String meetingId,
        int participantCount,
        int votedParticipantCount,
        List<ParticipantResponse> participants,
        List<LocalDate> dateOptions,
        String startTime,
        String endTime
) {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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

        int votedParticipantCount = (int) meeting.getSchedulePoll().getScheduleVotes().stream()
                .map(ScheduleVote::getParticipant)
                .distinct()
                .count();

        List<LocalDate> dateOptions = meeting.getSchedulePoll().getDateOptions();

        return new GetMeetingScheduleResponse(
                meeting.getMeetingId(),
                meeting.getParticipantCount(),
                votedParticipantCount,
                participantResponses,
                dateOptions,
                formatMinutes(meeting.getSchedulePoll().getStartTime()),
                formatMinutes(meeting.getSchedulePoll().getEndTime())
        );
    }

    private static String formatMinutes(int minutes) {
        if (minutes == 24 * 60) {
            return "24:00";
        }
        LocalTime time = LocalTime.of(minutes / 60, minutes % 60);
        return time.format(TIME_FORMATTER);
    }
}
