package com.sms.study_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PomodoroDto {
    private Long id;
    private Integer workMinutes;
    private Integer breakMinutes;
    private LocalDateTime loggedAt;
}
