package com.sms.study_management.service;


import com.sms.study_management.exception.ResourceNotFoundException;
import com.sms.study_management.model.PomodoroLog;
import com.sms.study_management.model.User;
import com.sms.study_management.repository.PomodoroRepository;
import com.sms.study_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroRepository pomodoroRepository;
    private final UserRepository userRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public PomodoroLog logSession(String username, Integer workMinutes, Integer breakMinutes) {
        PomodoroLog log = PomodoroLog.builder()
                .user(getUser(username))
                .workMinutes(workMinutes != null ? workMinutes : 25)
                .breakMinutes(breakMinutes != null ? breakMinutes : 5)
                .loggedAt(LocalDateTime.now())
                .build();
        return pomodoroRepository.save(log);
    }

    public List<PomodoroLog> getLogsForUser(String username) {
        return pomodoroRepository.findByUserId(getUser(username).getId());
    }
}
