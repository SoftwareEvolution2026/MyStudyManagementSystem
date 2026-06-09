package com.sms.study_management.repository;

import com.sms.study_management.model.PomodoroLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface PomodoroRepository extends JpaRepository<PomodoroLog, Long> {
    List<PomodoroLog> findByUserId(Long userId);
    List<PomodoroLog> findByUserIdAndLoggedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);
}
