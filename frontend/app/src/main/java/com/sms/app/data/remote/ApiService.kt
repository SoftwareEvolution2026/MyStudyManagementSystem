package com.sms.app.data.remote

import com.sms.app.data.model.*
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): MessageResponse

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    // Tasks
    @GET("api/tasks")
    suspend fun getTasks(@Header("Authorization") token: String): List<Task>

    @POST("api/tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Body task: Task
    ): Task

    @PUT("api/tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body task: Task
    ): Task

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    )

    // Sessions
    @GET("api/sessions")
    suspend fun getSessions(
        @Header("Authorization") token: String
    ): List<StudySession>

    @POST("api/sessions")
    suspend fun createSession(
        @Header("Authorization") token: String,
        @Body session: StudySession
    ): StudySession

    // Pomodoro
    @POST("api/pomodoro/complete")
    suspend fun logPomodoro(
        @Header("Authorization") token: String,
        @Body req: PomodoroCompleteRequest
    ): PomodoroLog

    @GET("api/pomodoro")
    suspend fun getPomodoroLogs(
        @Header("Authorization") token: String
    ): List<PomodoroLog>

    // Analytics
    @GET("api/analytics/weekly")
    suspend fun getWeeklyStats(
        @Header("Authorization") token: String
    ): WeeklyStats
}