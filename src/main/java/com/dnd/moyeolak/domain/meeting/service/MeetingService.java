package com.dnd.moyeolak.domain.meeting.service;

import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;
import com.dnd.moyeolak.domain.meeting.dto.GetMeetingScheduleResponse;

public interface MeetingService {

    String createMeeting(CreateMeetingRequest request);

    GetMeetingScheduleResponse getMeetingSchedules(String meetingId);

    void deleteMeeting(String meetingId);
}
