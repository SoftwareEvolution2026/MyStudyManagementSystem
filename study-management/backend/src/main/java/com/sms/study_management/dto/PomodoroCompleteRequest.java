package com.sms.study_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PomodoroCompleteRequest {
    private Integer workMinutes;
    private Integer breakMinutes;
    private Long taskId;
}
