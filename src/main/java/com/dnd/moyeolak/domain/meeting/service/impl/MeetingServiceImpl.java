package com.dnd.moyeolak.domain.meeting.service.impl;

import com.dnd.moyeolak.domain.location.entity.LocationPoll;
import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.dto.GetMeetingScheduleResponse;
import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.repository.MeetingRepository;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.participant.entity.Participant;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;

    @Override
    @Transactional
    public String createMeeting(CreateMeetingRequest request) {
        Meeting meeting = Meeting.of(request.participantCount());

        meeting.addParticipant(Participant.of(meeting, request.localStorageKey(), request.participantName()));

        meeting.addPolls(SchedulePoll.defaultOf(meeting), LocationPoll.defaultOf(meeting));

        Meeting saveMeeting = meetingRepository.save(meeting);
        return saveMeeting.getMeetingId();
    }

    @Override
    public GetMeetingScheduleResponse getMeetingSchedules(String meetingId) {
        meetingRepository.findByIdWithParticipants(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        Meeting meeting = meetingRepository.findByIdWithScheduleVotes(meetingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));

        return GetMeetingScheduleResponse.from(meeting);
    }
}
