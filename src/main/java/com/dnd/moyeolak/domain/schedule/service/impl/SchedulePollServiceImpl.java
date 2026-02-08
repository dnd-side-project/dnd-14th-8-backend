package com.dnd.moyeolak.domain.schedule.service.impl;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import com.dnd.moyeolak.domain.meeting.service.MeetingService;
import com.dnd.moyeolak.domain.schedule.dto.UpdateSchedulePollRequest;
import com.dnd.moyeolak.domain.schedule.entity.SchedulePoll;
import com.dnd.moyeolak.domain.schedule.service.SchedulePollService;
import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SchedulePollServiceImpl implements SchedulePollService {

    private final MeetingService meetingService;

    @Override
    @Transactional
    public void updateSchedulePoll(String meetingId, UpdateSchedulePollRequest request) {
        Meeting meeting = meetingService.get(meetingId);
        SchedulePoll schedulePoll = meeting.getSchedulePoll();

        if (schedulePoll == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_POLL_NOT_FOUND);
        }

        if (request.startTime() >= request.endTime()) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }

        schedulePoll.updateOptions(request.dateOptions(), request.startTime(), request.endTime());
    }
}
