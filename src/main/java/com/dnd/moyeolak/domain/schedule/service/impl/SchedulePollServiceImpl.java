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

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SchedulePollServiceImpl implements SchedulePollService {

    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final int MINUTE_STEP = 30;
    private static final String MIDNIGHT_STRING = "24:00";

    private final MeetingService meetingService;

    @Override
    @Transactional
    public void updateSchedulePoll(String meetingId, UpdateSchedulePollRequest request) {
        Meeting meeting = meetingService.get(meetingId);
        SchedulePoll schedulePoll = meeting.getSchedulePoll();

        if (schedulePoll == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_POLL_NOT_FOUND);
        }

        int startMinute = parseToMinuteOfDay(request.startTime(), false);
        int endMinute = parseToMinuteOfDay(request.endTime(), true);

        if (startMinute >= endMinute) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }

        schedulePoll.updateOptions(request.dateOptions(), startMinute, endMinute);
    }

    private int parseToMinuteOfDay(String value, boolean allowMidnight) {
        if (MIDNIGHT_STRING.equals(value)) {
            if (allowMidnight) {
                return MINUTES_PER_DAY;
            }
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }

        try {
            LocalTime time = LocalTime.parse(value);
            int minutes = time.getHour() * 60 + time.getMinute();
            if (minutes % MINUTE_STEP != 0) {
                throw new BusinessException(ErrorCode.INVALID_FORMAT);
            }
            return minutes;
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_FORMAT);
        }
    }
}
