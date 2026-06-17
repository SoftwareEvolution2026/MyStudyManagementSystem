package com.sms.app.userinterface.calendar

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sms.app.data.model.StudySession
import com.sms.app.data.model.Task
import com.sms.app.data.remote.RetrofitInstance
import com.sms.app.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tasks: List<Task> = emptyList(),
    val sessions: List<StudySession> = emptyList(),
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now()
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitInstance.api

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState

    init {
        loadCalendar()
    }

    fun loadCalendar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = tokenManager.tokenFlow.first()
                if (token.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(error = "Not authenticated.")
                    return@launch
                }

                val authHeader = "Bearer $token"
                _uiState.value = _uiState.value.copy(
                    tasks = api.getTasks(authHeader),
                    sessions = api.getSessions(authHeader)
                )
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Error loading calendar", e)
                _uiState.value = _uiState.value.copy(error = "Failed to load calendar.")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun previousMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.minusMonths(1))
    }

    fun nextMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.plusMonths(1))
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}