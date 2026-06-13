package com.sms.study_management.service;

import com.sms.study_management.exception.ResourceNotFoundException;
import com.sms.study_management.model.StudySession;
import com.sms.study_management.model.User;
import com.sms.study_management.repository.SessionRepository;
import com.sms.study_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

	private final SessionRepository sessionRepository;
	private final UserRepository userRepository;

	private User getUser(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	public List<StudySession> getSessionsForUser(String username) {
		return sessionRepository.findByUserId(getUser(username).getId());
	}

	public StudySession createSession(String username, StudySession session) {
		session.setUser(getUser(username));
		if (session.getStartTime() == null) {
			session.setStartTime(LocalDateTime.now());
		}
		return sessionRepository.save(session);
	}

	public StudySession finishSession(String username, Long sessionId, LocalDateTime endTime) {
		StudySession session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new ResourceNotFoundException("Study session not found"));
		if (!session.getUser().getId().equals(getUser(username).getId())) {
			throw new AccessDeniedException("Unauthorized");
		}
		session.setEndTime(endTime != null ? endTime : LocalDateTime.now());
		if (session.getStartTime() != null && session.getEndTime() != null) {
			session.setDurationMinutes((int) Duration.between(session.getStartTime(), session.getEndTime()).toMinutes());
		}
		return sessionRepository.save(session);
	}

	public void deleteSession(String username, Long sessionId) {
		StudySession session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new ResourceNotFoundException("Study session not found"));
		if (!session.getUser().getId().equals(getUser(username).getId())) {
			throw new AccessDeniedException("Unauthorized");
		}
		sessionRepository.delete(session);
	}
}
