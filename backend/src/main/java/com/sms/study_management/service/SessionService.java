package com.sms.study_management.service;

import com.sms.study_management.exception.ResourceNotFoundException;
import com.sms.study_management.model.StudySession;
import com.sms.study_management.model.User;
import com.sms.study_management.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<StudySession> getSessionsForUser(String username) {
        User user = findUserByUsername(username);
        return sessionRepository.findByUserId(user.getId());
    }

    @Transactional
    public StudySession createSession(String username, StudySession session) {
        User user = findUserByUsername(username);
        session.setUser(user);

        // Ensure startTime is set
        if (session.getStartTime() == null) {
            session.setStartTime(LocalDateTime.now());
        }

        // Auto-calculate duration if both times are present (useful for Pomodoro sync)
        calculateDuration(session);

        return sessionRepository.save(session);
    }

    @Transactional
    public StudySession finishSession(String username, Long sessionId, LocalDateTime endTime) {
        StudySession session = findSessionAndValidateOwnership(sessionId, username);

        session.setEndTime(endTime != null ? endTime : LocalDateTime.now());
        calculateDuration(session);

        return sessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(String username, Long sessionId) {
        StudySession session = findSessionAndValidateOwnership(sessionId, username);
        sessionRepository.delete(session);
    }

    // Helper: Centralized user lookup
    private User findUserByUsername(String username) {
        return currentUserService.requireUser(username);
    }

    // Helper: Validates session exists and belongs to the user
    private StudySession findSessionAndValidateOwnership(Long sessionId, String username) {
        StudySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found"));

        if (!session.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to modify this session");
        }
        return session;
    }

    // Helper: Logic to maintain data consistency
    private void calculateDuration(StudySession session) {
        if (session.getStartTime() != null && session.getEndTime() != null) {
            long minutes = Duration.between(session.getStartTime(), session.getEndTime()).toMinutes();
            session.setDurationMinutes((int) minutes);
        }
    }
}