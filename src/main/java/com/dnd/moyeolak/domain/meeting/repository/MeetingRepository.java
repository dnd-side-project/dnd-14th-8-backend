package com.dnd.moyeolak.domain.meeting.repository;

import com.dnd.moyeolak.domain.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, String> {

}
