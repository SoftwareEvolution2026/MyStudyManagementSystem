package com.sms.study_management.service;

import com.sms.study_management.model.Task;
import com.sms.study_management.model.User;
import com.sms.study_management.exception.ResourceNotFoundException;
import com.sms.study_management.repository.TaskRepository;
import com.sms.study_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public List<Task> getAllTasks(String username) {
        return taskRepository.findByUserId(getUser(username).getId());
    }

    public Task createTask(String username, Task task) {
        task.setUser(getUser(username));
        return taskRepository.save(task);
    }

    public Task updateTask(String username, Long taskId, Task updated) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getUser().getId().equals(getUser(username).getId()))
            throw new AccessDeniedException("Unauthorized");
        task.setTitle(updated.getTitle());
        task.setDescription(updated.getDescription());
        task.setStatus(updated.getStatus());
        task.setDueDate(updated.getDueDate());
        return taskRepository.save(task);
    }

    public void deleteTask(String username, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getUser().getId().equals(getUser(username).getId()))
            throw new AccessDeniedException("Unauthorized");
        taskRepository.delete(task);
    }
}
