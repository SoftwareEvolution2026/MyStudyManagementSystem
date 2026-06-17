package com.sms.app.data.model

data class LoginRequest(val username: String, val password: String)

data class RegisterRequest(val username: String, val email: String, val password: String)

data class AuthResponse(val token: String)

data class MessageResponse(val message: String)

data class Task(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val status: String? = "PENDING",
    val priority: String? = "MEDIUM",
    val dueDate: String? = null
)

data class StudySession(
    val id: Long? = null,
    val subject: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val durationMinutes: Int? = 0
)

data class PomodoroLog(
    val id: Long? = null,
    val workMinutes: Int? = 25,
    val breakMinutes: Int? = 5,
    val loggedAt: String? = ""
)

data class PomodoroCompleteRequest(
    val workMinutes: Int,
    val breakMinutes: Int,
    val taskId: Long? = null // Added taskId to link session to a task
)

data class WeeklyStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pomodoroSessionsThisWeek: Int = 0,
    val studySessionsThisWeek: Int = 0
)