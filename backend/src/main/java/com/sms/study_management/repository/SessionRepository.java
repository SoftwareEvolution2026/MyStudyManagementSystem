package com.sms.study_management.repository;

import com.sms.study_management.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface SessionRepository extends JpaRepository<StudySession, Long> {
    List<StudySession> findByUserId(Long userId);
    List<StudySession> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime from, LocalDateTime to);
}
