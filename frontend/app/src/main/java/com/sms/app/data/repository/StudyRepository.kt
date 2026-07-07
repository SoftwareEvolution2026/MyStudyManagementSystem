package com.sms.app.data.repository

import android.content.Context
import com.sms.app.data.model.PomodoroCompleteRequest
import com.sms.app.data.model.PomodoroLog
import com.sms.app.data.model.StudySession
import com.sms.app.data.model.Task
import com.sms.app.data.model.WeeklyStats
import com.sms.app.data.remote.RetrofitInstance
import com.sms.app.util.TokenManager
import kotlinx.coroutines.flow.first

class StudyRepository(context: Context) {

    private val tokenManager = TokenManager(context)
    private val api = RetrofitInstance.api

    suspend fun authHeader(): String? {
        val token = tokenManager.tokenFlow.first()
        return token?.takeIf { it.isNotBlank() }?.let { "Bearer $it" }
    }

    suspend fun getTasks(): List<Task> {
        return authHeader()?.let { api.getTasks(it) }
            ?: throw IllegalStateException("Not authenticated")
    }

    suspend fun createTask(task: Task): Task {
        return authHeader()?.let { api.createTask(it, task) }
            ?: throw IllegalStateException("Not authenticated")
    }

    suspend fun updateTask(taskId: Long, task: Task): Task {
        return authHeader()?.let { api.updateTask(it, taskId, task) }
            ?: throw IllegalStateException("Not authenticated")
    }

    suspend fun deleteTask(taskId: Long) {
        authHeader()?.let { api.deleteTask(it, taskId) }
            ?: throw IllegalStateException("Not authenticated")
    }

    suspend fun getSessions(): List<StudySession> {
        return authHeader()?.let { api.getSessions(it) }
            ?: throw IllegalStateException("Not authenticated")
    }

    suspend fun createSession(session: StudySession): StudySession {
        return authHeader()?.let { api.createSession(it, session) }
            ?: throw IllegalStateException("Not authenticated")
    }

    suspend fun logPomodoro(request: PomodoroCompleteRequest): PomodoroLog {
        return authHeader()?.let { api.logPomodoro(it, request) }
            ?: throw IllegalStateException("Not authenticated")
    }

    suspend fun getWeeklyStats(): WeeklyStats {
        return authHeader()?.let { api.getWeeklyStats(it) }
            ?: throw IllegalStateException("Not authenticated")
    }
}
