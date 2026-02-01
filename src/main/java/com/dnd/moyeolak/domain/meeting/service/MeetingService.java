package com.dnd.moyeolak.domain.meeting.service;

import com.dnd.moyeolak.domain.meeting.dto.CreateMeetingRequest;

public interface MeetingService {

    String createMeeting(CreateMeetingRequest request);
}
