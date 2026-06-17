package com.sms.app.userinterface.tasks

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sms.app.data.model.Task
import com.sms.app.data.remote.RetrofitInstance
import com.sms.app.util.ReminderScheduler
import com.sms.app.util.TokenManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = RetrofitInstance.api

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchTasks()
    }

    private suspend fun bearerToken(): String {
        val token = tokenManager.tokenFlow.first()
        return if (token != null) "Bearer $token" else ""
    }

    fun fetchTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val token = bearerToken()
                if (token.isNotEmpty()) {
                    _tasks.value = api.getTasks(token)
                } else {
                    _error.value = "No authentication token found."
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error fetching tasks", e)
                _error.value = "Failed to fetch tasks: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createTask(title: String, description: String, priority: String = "MEDIUM", dueDate: String? = null) {
        viewModelScope.launch {
            _error.value = null
            try {
                val token = bearerToken()
                if (token.isNotEmpty()) {
                    val newTask = api.createTask(
                        token,
                        Task(title = title, description = description, priority = priority, dueDate = dueDate)
                    )
                    _tasks.value = _tasks.value + newTask
                    ReminderScheduler.scheduleTaskReminder(getApplication(), newTask)
                } else {
                    _error.value = "Not authenticated."
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error creating task", e)
                _error.value = "Failed to create task. Check if you have permission."
            }
        }
    }

    fun markDone(task: Task) {
        viewModelScope.launch {
            _error.value = null
            try {
                val taskId = task.id ?: return@launch
                val token = bearerToken()
                val updated = api.updateTask(
                    token,
                    taskId,
                    task.copy(status = "DONE")
                )
                _tasks.value = _tasks.value.map {
                    if (it.id == updated.id) updated else it
                }
                ReminderScheduler.cancelTaskReminder(getApplication(), taskId)
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error updating task", e)
                _error.value = "Failed to update task."
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _error.value = null
            try {
                val taskId = task.id ?: return@launch
                api.deleteTask(bearerToken(), taskId)
                _tasks.value = _tasks.value.filter { it.id != task.id }
                ReminderScheduler.cancelTaskReminder(getApplication(), taskId)
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error deleting task", e)
                _error.value = "Failed to delete task."
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}