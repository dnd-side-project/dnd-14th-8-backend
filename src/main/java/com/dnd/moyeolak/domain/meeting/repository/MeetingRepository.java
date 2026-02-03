package com.dnd.moyeolak.domain.meeting.repository;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, String> {

    @Query("""
        SELECT m FROM Meeting m
        LEFT JOIN FETCH m.participants
        WHERE m.meetingId = :meetingId
    """)
    Optional<Meeting> findByIdWithParticipants(@Param("meetingId") String meetingId);

    @Query("""
        SELECT m FROM Meeting m
        LEFT JOIN FETCH m.schedulePoll sp
        LEFT JOIN FETCH sp.scheduleVotes
        WHERE m.meetingId = :meetingId
    """)
    Optional<Meeting> findByIdWithScheduleVotes(@Param("meetingId") String meetingId);

    @Query("""
        SELECT m FROM Meeting m
        LEFT JOIN FETCH m.schedulePoll
        LEFT JOIN FETCH m.locationPoll
        LEFT JOIN FETCH m.participants
        WHERE m.meetingId = :meetingId
    """)
    Optional<Meeting> findByIdWithAllAssociations(@Param("meetingId") String meetingId);
}
