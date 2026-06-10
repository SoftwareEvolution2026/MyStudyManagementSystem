package com.sms.study_management.controller;

import com.sms.study_management.repository.PomodoroRepository;
import com.sms.study_management.repository.SessionRepository;
import com.sms.study_management.repository.TaskRepository;
import com.sms.study_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final TaskRepository taskRepository;
    private final PomodoroRepository pomodoroRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @GetMapping("/weekly")
    public Map<String, Object> getWeeklyStats(@AuthenticationPrincipal UserDetails user) {
        Long userId = userRepository.findByUsername(user.getUsername())
                .orElseThrow().getId();
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();

        long totalTasks = taskRepository.findByUserId(userId).size();
        long doneTasks = taskRepository.findByUserIdAndStatus(userId, "DONE").size();
        long pomodoroCount = pomodoroRepository.findByUserIdAndLoggedAtBetween(userId, weekStart, now).size();
        long sessionsCount = sessionRepository.findByUserIdAndStartTimeBetween(userId, weekStart, now).size();

        return Map.of(
                "totalTasks", totalTasks,
                "completedTasks", doneTasks,
                "pomodoroSessionsThisWeek", pomodoroCount,
                "studySessionsThisWeek", sessionsCount
        );
    }
}
