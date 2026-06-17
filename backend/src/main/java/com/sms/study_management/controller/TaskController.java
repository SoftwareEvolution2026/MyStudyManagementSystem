package com.sms.study_management.controller;

import com.sms.study_management.model.Task;
import com.sms.study_management.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<Task> getTasks(@AuthenticationPrincipal UserDetails user) {
        return taskService.getAllTasks(user.getUsername());
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@AuthenticationPrincipal UserDetails user,
                                           @RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(user.getUsername(), task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@AuthenticationPrincipal UserDetails user,
                                           @PathVariable Long id,
                                           @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(user.getUsername(), id, task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@AuthenticationPrincipal UserDetails user,
                                        @PathVariable Long id) {
        taskService.deleteTask(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
