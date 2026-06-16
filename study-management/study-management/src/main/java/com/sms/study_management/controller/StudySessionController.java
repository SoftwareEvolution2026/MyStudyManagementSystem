package com.sms.study_management.controller;


import com.sms.study_management.model.StudySession;
import com.sms.study_management.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final SessionService sessionService;

    // GET /api/sessions - Returns all sessions for the logged-in user
    @GetMapping
    public ResponseEntity<List<StudySession>> getSessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(sessionService.getSessionsForUser(userDetails.getUsername()));
    }

    // POST /api/sessions - Creates a new session (called by Pomodoro completion)
    @PostMapping
    public ResponseEntity<StudySession> createSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody StudySession session) {
        return ResponseEntity.ok(sessionService.createSession(userDetails.getUsername(), session));
    }

    // DELETE /api/sessions/{id} - Deletes a specific session
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        sessionService.deleteSession(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}

