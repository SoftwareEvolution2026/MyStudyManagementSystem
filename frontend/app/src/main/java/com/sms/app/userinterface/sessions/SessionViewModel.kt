package com.sms.app.userinterface.sessions

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sms.app.data.model.StudySession
import com.sms.app.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SessionUiState(
    val sessions: List<StudySession> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(application)

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState

    init {
        fetchSessions()
    }

    fun fetchSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = repository.getSessions()
                _uiState.update { it.copy(sessions = response, errorMessage = null) }
            } catch (e: HttpException) {
                val msg = when(e.code()) {
                    404 -> "Sessions endpoint not found (404). Check backend controller mapping."
                    401 -> "Session expired. Please log in again."
                    else -> "Server error (${e.code()})."
                }
                _uiState.update { it.copy(errorMessage = msg) }
            } catch (e: Exception) {
                Log.e("SessionViewModel", "Error: ${e.message}", e)
                _uiState.update { it.copy(errorMessage = "Connection error: ${e.localizedMessage}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
