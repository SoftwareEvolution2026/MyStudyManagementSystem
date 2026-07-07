package com.sms.app.userinterface.pomodoro

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sms.app.data.model.PomodoroCompleteRequest
import com.sms.app.data.model.StudySession
import com.sms.app.data.model.Task
import com.sms.app.data.repository.StudyRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class PomodoroPhase { WORK, BREAK }

data class PomodoroState(
    val phase: PomodoroPhase = PomodoroPhase.WORK,
    val secondsRemaining: Int = 25 * 60,
    val isRunning: Boolean = false,
    val sessionsCompleted: Int = 0,
    val workMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val subject: String = "General Study",
    val selectedTaskId: Long? = null,
    val selectedTaskName: String = "None",
    val availableTasks: List<Task> = emptyList(),
    val isLoadingTasks: Boolean = false,
    val error: String? = null
)

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(application)

    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state

    private var timerJob: Job? = null

    init {
        fetchTasks()
    }

    fun startPause() {
        if (_state.value.isRunning) {
            timerJob?.cancel()
            _state.update { it.copy(isRunning = false) }
        } else {
            _state.update { it.copy(isRunning = true) }
            timerJob = viewModelScope.launch {
                while (_state.value.secondsRemaining > 0) {
                    delay(1000L)
                    _state.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
                }
                onPhaseComplete()
            }
        }
    }

    fun reset() {
        timerJob?.cancel()
        _state.update {
            it.copy(
                isRunning = false,
                secondsRemaining = it.workMinutes * 60,
                phase = PomodoroPhase.WORK,
                error = null
            )
        }
    }

    private fun onPhaseComplete() {
        val current = _state.value
        if (current.phase == PomodoroPhase.WORK) {
            viewModelScope.launch {
                try {
                    // 1. Log Pomodoro with the linked task ID
                    repository.logPomodoro(
                        PomodoroCompleteRequest(
                            current.workMinutes,
                            current.breakMinutes,
                            current.selectedTaskId
                        )
                    )

                    // 2. Automatically create a linked Study Session
                    val now = LocalDateTime.now()
                    val startTime = now.minusMinutes(current.workMinutes.toLong())
                    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

                    repository.createSession(
                        StudySession(
                            subject = current.subject,
                            startTime = startTime.format(formatter),
                            endTime = now.format(formatter),
                            durationMinutes = current.workMinutes
                        )
                    )
                } catch (e: Exception) {
                    Log.e("PomodoroViewModel", "Error logging session", e)
                    _state.update { it.copy(error = "Failed to log session to server") }
                }
            }
            _state.update {
                it.copy(
                    phase = PomodoroPhase.BREAK,
                    secondsRemaining = it.breakMinutes * 60,
                    sessionsCompleted = it.sessionsCompleted + 1,
                    isRunning = false
                )
            }
        } else {
            _state.update {
                it.copy(
                    phase = PomodoroPhase.WORK,
                    secondsRemaining = it.workMinutes * 60,
                    isRunning = false
                )
            }
        }
    }

    fun fetchTasks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTasks = true) }
            try {
                val tasks = repository.getTasks()
                // Filter to only show pending/in-progress tasks
                val filteredTasks = tasks.filter { it.status != "COMPLETED" }
                _state.update { it.copy(availableTasks = filteredTasks, isLoadingTasks = false) }
            } catch (e: Exception) {
                Log.e("PomodoroViewModel", "Error fetching tasks", e)
                _state.update { it.copy(isLoadingTasks = false) }
            }
        }
    }

    fun selectTask(task: Task?) {
        _state.update {
            it.copy(
                selectedTaskId = task?.id,
                selectedTaskName = task?.title ?: "None",
                // Automatically update subject to task title if one is selected
                subject = task?.title ?: it.subject
            )
        }
    }

    fun setWorkMinutes(minutes: Int) {
        timerJob?.cancel()
        _state.update {
            it.copy(
                workMinutes = minutes,
                secondsRemaining = minutes * 60,
                isRunning = false,
                phase = PomodoroPhase.WORK
            )
        }
    }

    fun setBreakMinutes(minutes: Int) {
        _state.update { it.copy(breakMinutes = minutes) }
    }
    
    fun setSubject(subject: String) {
        _state.update { it.copy(subject = subject) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
