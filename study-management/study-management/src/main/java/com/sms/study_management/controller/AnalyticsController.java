package com.sms.study_management.controller;

import com.sms.study_management.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/weekly")
    public Map<String, Object> getWeeklyStats(@AuthenticationPrincipal UserDetails user) {
        return analyticsService.getWeeklyStats(user.getUsername());
    }
}
