package com.dnd.moyeolak.domain.location.service;

import com.dnd.moyeolak.domain.location.entity.NearbyPlaceHours;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BusinessHoursCalculator {

    private final Clock clock;

    public record BusinessStatus(Boolean isOpen, String message) {}

    public BusinessStatus calculateBusinessStatus(List<NearbyPlaceHours> hours) {
        if (hours == null || hours.isEmpty()) {
            return new BusinessStatus(null, null);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        int currentDay = convertDayOfWeek(now.getDayOfWeek());
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        for (NearbyPlaceHours period : hours) {
            if (isWithinPeriod(currentDay, currentHour, currentMinute, period)) {
                String closeTime = formatTime(period.getCloseHour(), period.getCloseMinute());
                String message = closeTime + "에 영업 종료";
                return new BusinessStatus(true, message);
            }
        }

        String nextOpenMessage = findNextOpenMessage(currentDay, currentHour, currentMinute, hours);
        return new BusinessStatus(false, nextOpenMessage);
    }

    private boolean isWithinPeriod(int day, int hour, int minute, NearbyPlaceHours period) {
        int currentTotal = day * 24 * 60 + hour * 60 + minute;
        int openTotal = period.getOpenDay() * 24 * 60 + period.getOpenHour() * 60 + period.getOpenMinute();
        int closeTotal = period.getCloseDay() * 24 * 60 + period.getCloseHour() * 60 + period.getCloseMinute();

        if (closeTotal > openTotal) {
            return currentTotal >= openTotal && currentTotal < closeTotal;
        } else {
            return currentTotal >= openTotal || currentTotal < closeTotal;
        }
    }

    private String findNextOpenMessage(int currentDay, int currentHour, int currentMinute, List<NearbyPlaceHours> hours) {
        int currentTotal = currentDay * 24 * 60 + currentHour * 60 + currentMinute;

        NearbyPlaceHours nearest = null;
        int minDiff = Integer.MAX_VALUE;

        for (NearbyPlaceHours period : hours) {
            int openTotal = period.getOpenDay() * 24 * 60 + period.getOpenHour() * 60 + period.getOpenMinute();
            int diff = openTotal - currentTotal;
            if (diff < 0) {
                diff += 7 * 24 * 60;
            }
            if (diff < minDiff) {
                minDiff = diff;
                nearest = period;
            }
        }

        if (nearest == null) {
            return null;
        }

        String openTime = formatTime(nearest.getOpenHour(), nearest.getOpenMinute());

        if (nearest.getOpenDay() == currentDay && nearest.getOpenHour() * 60 + nearest.getOpenMinute() > currentHour * 60 + currentMinute) {
            return openTime + "에 영업 시작";
        }

        int nextDay = (currentDay + 1) % 7;
        if (nearest.getOpenDay() == nextDay) {
            return "내일 " + openTime + "에 영업 시작";
        }

        String dayName = getDayName(nearest.getOpenDay());
        return dayName + " " + openTime + "에 영업 시작";
    }

    /**
     * Java DayOfWeek (1=월~7=일) → Google 방식 (0=일, 1=월~6=토)
     */
    private int convertDayOfWeek(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SUNDAY ? 0 : dayOfWeek.getValue();
    }

    private String formatTime(int hour, int minute) {
        return String.format("%02d:%02d", hour, minute);
    }

    private String getDayName(int day) {
        return switch (day) {
            case 0 -> "일요일";
            case 1 -> "월요일";
            case 2 -> "화요일";
            case 3 -> "수요일";
            case 4 -> "목요일";
            case 5 -> "금요일";
            case 6 -> "토요일";
            default -> "";
        };
    }
}
