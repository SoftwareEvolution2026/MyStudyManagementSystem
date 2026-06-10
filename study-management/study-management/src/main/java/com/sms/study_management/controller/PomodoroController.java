package com.sms.study_management.controller;

import com.sms.study_management.model.PomodoroLog;
import com.sms.study_management.service.PomodoroService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pomodoro")
@RequiredArgsConstructor
public class PomodoroController {

    private final PomodoroService pomodoroService;

    @PostMapping("/complete")
    public PomodoroLog complete(@AuthenticationPrincipal UserDetails user,
                                @RequestBody Map<String, Integer> body) {
        return pomodoroService.logSession(
                user.getUsername(),
                body.get("workMinutes"),
                body.get("breakMinutes"));
    }

    @GetMapping
    public List<PomodoroLog> getLogs(@AuthenticationPrincipal UserDetails user) {
        return pomodoroService.getLogsForUser(user.getUsername());
    }
}
