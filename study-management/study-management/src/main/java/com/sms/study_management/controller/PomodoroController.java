package com.sms.study_management.controller;

import com.sms.study_management.dto.PomodoroCompleteRequest;
import com.sms.study_management.dto.PomodoroDto;
import com.sms.study_management.service.PomodoroService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pomodoro")
@RequiredArgsConstructor
public class PomodoroController {

    private final PomodoroService pomodoroService;

    @PostMapping("/complete")
    public PomodoroDto complete(@AuthenticationPrincipal UserDetails user,
                                @RequestBody PomodoroCompleteRequest request) {
        return pomodoroService.logSession(
                user.getUsername(),
                request.getWorkMinutes(),
                request.getBreakMinutes(),
                request.getTaskId());
    }

    @GetMapping
    public List<PomodoroDto> getLogs(@AuthenticationPrincipal UserDetails user) {
        return pomodoroService.getLogsForUser(user.getUsername());
    }
}
