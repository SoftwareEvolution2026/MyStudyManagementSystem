package com.sms.app.userinterface.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sms.app.data.model.StudySession
import com.sms.app.data.model.WeeklyStats
import com.sms.app.data.remote.RetrofitInstance
import com.sms.app.util.GoalsManager
import com.sms.app.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: WeeklyStats = WeeklyStats(),
    val todayStudyMinutes: Int = 0,
    val currentStreakDays: Int = 0,
    val dailyStudyGoalMinutes: Int = 120,
    val weeklyTaskGoal: Int = 5
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val goalsManager = GoalsManager(application)
    private val api = RetrofitInstance.api

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.tokenFlow.first()
                if (token != null) {
                    val authHeader = "Bearer $token"
                    val stats = api.getWeeklyStats(authHeader)
                    val sessions = api.getSessions(authHeader)
                    val goals = goalsManager.goalsFlow.first()

                    _uiState.value = _uiState.value.copy(
                        stats = stats,
                        todayStudyMinutes = calculateTodayStudyMinutes(sessions),
                        currentStreakDays = calculateCurrentStreak(sessions),
                        dailyStudyGoalMinutes = goals.dailyStudyGoalMinutes,
                        weeklyTaskGoal = goals.weeklyTaskGoal
                    )
                } else {
                    _uiState.value = _uiState.value.copy(error = "User not authenticated.")
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error loading dashboard stats", e)
                _uiState.value = _uiState.value.copy(error = "Failed to load dashboard data.")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateGoals(dailyStudyGoalMinutes: Int, weeklyTaskGoal: Int) {
        viewModelScope.launch {
            goalsManager.saveGoals(dailyStudyGoalMinutes, weeklyTaskGoal)
            loadDashboard()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun calculateTodayStudyMinutes(sessions: List<StudySession>): Int {
        val today = LocalDate.now()
        return sessions.sumOf { session ->
            val sessionDate = parseDate(session.startTime)?.toLocalDate()
            if (sessionDate == today) session.durationMinutes ?: durationFromSession(session) else 0
        }
    }

    private fun calculateCurrentStreak(sessions: List<StudySession>): Int {
        val activeDays = sessions.mapNotNull { parseDate(it.startTime)?.toLocalDate() }.toSet()
        var streak = 0
        var cursor = LocalDate.now()

        while (activeDays.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }

        return streak
    }

    private fun durationFromSession(session: StudySession): Int {
        val start = parseDate(session.startTime)
        val end = parseDate(session.endTime)
        return if (start != null && end != null) {
            Duration.between(start, end).toMinutes().toInt().coerceAtLeast(0)
        } else {
            0
        }
    }

    private fun parseDate(value: String?): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDateTime.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}