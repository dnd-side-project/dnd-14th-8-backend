package com.dnd.moyeolak.domain.meeting.service;

import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.dto.GetMeetingScheduleResponse;

import java.util.List;

public interface MeetingService {

    String createMeeting(CreateMeetingRequest request);

    GetMeetingScheduleResponse getMeetingSchedules(String meetingId);

    List<String> findAllMeetings();

    void deleteMeeting(String meetingId);
}
