package com.sms.study_management.service;

import com.sms.study_management.exception.ResourceNotFoundException;
import com.sms.study_management.repository.PomodoroRepository;
import com.sms.study_management.repository.SessionRepository;
import com.sms.study_management.repository.TaskRepository;
import com.sms.study_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

	private final TaskRepository taskRepository;
	private final PomodoroRepository pomodoroRepository;
	private final SessionRepository sessionRepository;
	private final UserRepository userRepository;

	public Map<String, Object> getWeeklyStats(String username) {
		Long userId = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"))
				.getId();

		LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
		LocalDateTime now = LocalDateTime.now();

		Map<String, Object> stats = new LinkedHashMap<>();
		stats.put("totalTasks", taskRepository.findByUserId(userId).size());
		stats.put("completedTasks", taskRepository.findByUserIdAndStatus(userId, "DONE").size());
		stats.put("pomodoroSessionsThisWeek", pomodoroRepository.findByUserIdAndLoggedAtBetween(userId, weekStart, now).size());
		stats.put("studySessionsThisWeek", sessionRepository.findByUserIdAndStartTimeBetween(userId, weekStart, now).size());
		return stats;
	}
}
