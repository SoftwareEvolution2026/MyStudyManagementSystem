package com.sms.study_management.service;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.sms.study_management.dto.PomodoroDto;
import com.sms.study_management.exception.ResourceNotFoundException;
import com.sms.study_management.model.PomodoroLog;
import com.sms.study_management.model.Task;
import com.sms.study_management.model.User;
import com.sms.study_management.repository.PomodoroRepository;
import com.sms.study_management.repository.TaskRepository;
import com.sms.study_management.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroRepository pomodoroRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public PomodoroDto logSession(String username, Integer workMinutes, Integer breakMinutes, Long taskId) {
        User user = getUser(username);
        Task task = null;
        if (taskId != null) {
            task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
            if (!task.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Unauthorized task access");
            }
            if (!"DONE".equals(task.getStatus())) {
                task.setStatus("IN_PROGRESS");
                taskRepository.save(task);
            }
        }

        PomodoroLog log = PomodoroLog.builder()
                .user(user)
                .task(task)
                .workMinutes(workMinutes != null ? workMinutes : 25)
                .breakMinutes(breakMinutes != null ? breakMinutes : 5)
                .loggedAt(LocalDateTime.now())
                .build();
        return toDto(pomodoroRepository.save(log));
    }

    public List<PomodoroDto> getLogsForUser(String username) {
        return pomodoroRepository.findByUserId(getUser(username).getId()).stream()
                .map(this::toDto)
                .toList();
    }

    private PomodoroDto toDto(PomodoroLog log) {
        return new PomodoroDto(
                log.getId(),
                log.getWorkMinutes(),
                log.getBreakMinutes(),
                log.getLoggedAt(),
                log.getTask() != null ? log.getTask().getId() : null,
                log.getTask() != null ? log.getTask().getTitle() : null
        );
    }
}
