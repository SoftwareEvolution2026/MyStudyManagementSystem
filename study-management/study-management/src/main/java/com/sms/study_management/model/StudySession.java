package com.sms.study_management.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_sessions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StudySession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String subject;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
}
