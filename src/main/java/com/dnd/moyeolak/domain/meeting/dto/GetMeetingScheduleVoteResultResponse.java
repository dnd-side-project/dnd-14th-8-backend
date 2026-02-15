package com.dnd.moyeolak.domain.meeting.dto;


import com.dnd.moyeolak.domain.schedule.entity.ScheduleVote;
import com.dnd.moyeolak.global.utils.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public record GetMeetingScheduleVoteResultResponse(
    int resultCount,
    int participantCount,
    List<ScheduleVoteResult> scheduleVoteResult
) {
    public record ScheduleVoteResult(
            LocalDate scheduleDate,
            String scheduleDayOfWeek,
            String startTime,
            String endTime,
            int voteCount,
            List<String> availableParticipantNames,
            List<String> unavailableParticipantNames
    ) {}

    @Getter
    private static class MergedRange {
        private final LocalDateTime start;
        @Setter
        private LocalDateTime end;
        private final Set<String> voters;

        public MergedRange(LocalDateTime start, LocalDateTime end, Set<String> voters) {
            this.start = start;
            this.end = end;
            this.voters = voters;
        }
    }

    private static List<ScheduleVoteResult> returnScheduleVoteResults(List<String> participantNames, List<ScheduleVote> scheduleVotes) {
        // Step 1 : 슬롯별 투표자 집계
        Map<LocalDateTime, Set<String>> slotVoters = new HashMap<>();
        for (ScheduleVote scheduleVote : scheduleVotes) {
            String name = scheduleVote.getParticipant().getName();
            for (LocalDateTime localDateTime : scheduleVote.getVotedDate()) {
                slotVoters.computeIfAbsent(localDateTime, k -> new HashSet<>()).add(name);
            }
        }

        // Step 2 : 2명 미만 제거
        slotVoters.entrySet().removeIf(e -> e.getValue().size() < 2);

        // Step 3 : 시간순 정렬 후 연속 슬롯 병합
        List<LocalDateTime> sortedSlots = new ArrayList<>(slotVoters.keySet());
        Collections.sort(sortedSlots);

        List<MergedRange> ranges = new ArrayList<>();
        for (LocalDateTime localDateTime : sortedSlots) {
            Set<String> voters = slotVoters.get(localDateTime);
            if(!ranges.isEmpty()){
                MergedRange last = ranges.getLast();
                boolean consecutive = last.end.equals(localDateTime);
                boolean sameVoters = last.voters.equals(voters);
                if(consecutive && sameVoters){
                    last.setEnd(localDateTime.plusMinutes(30)); // 범위 확장
                    continue;
                }
            }
            ranges.add(new MergedRange(localDateTime, localDateTime.plusMinutes(30), voters));
        }

        // Step 3.5 : 최소 1시간(슬롯 2칸) 미만 범위 제거
        ranges.removeIf(r -> Duration.between(r.start, r.end).toMinutes() < 60);

        // Step 4 : 정렬 (투표 수 DESC → 시작 시간 ASC → 기간 DESC)
        ranges.sort(Comparator
                .comparingInt((MergedRange r) -> r.voters.size()).reversed()
                .thenComparing(r -> r.start)
                .thenComparing(Comparator.comparing((MergedRange r) -> Duration.between(r.start, r.end)).reversed()));

        // Step 5 : DTO 변환
        return ranges.stream()
                .map(r -> new ScheduleVoteResult(
                        r.start.toLocalDate(),
                        DateUtil.toDayOfWeek(r.start),
                        DateUtil.formatTime(r.start),
                        DateUtil.formatTime(r.end),
                        r.voters.size(),
                        new ArrayList<>(r.voters),
                        participantNames.stream()
                                .filter(name -> !r.voters.contains(name))
                                .toList()
                )).toList();
    }

    public static GetMeetingScheduleVoteResultResponse of(
            int participantCount, List<String> participantNames, List<ScheduleVote> scheduleVoteResult
    ) {
        List<ScheduleVoteResult> scheduleVoteResults = returnScheduleVoteResults(participantNames, scheduleVoteResult);
        return new GetMeetingScheduleVoteResultResponse(scheduleVoteResults.size(), participantCount, scheduleVoteResults);
    }
}
