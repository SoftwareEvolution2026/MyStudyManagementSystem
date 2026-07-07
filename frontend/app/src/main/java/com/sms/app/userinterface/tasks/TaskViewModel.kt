package com.sms.app.userinterface.tasks

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sms.app.data.model.Task
import com.sms.app.data.repository.StudyRepository
import com.sms.app.util.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(application)

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchTasks()
    }

    fun fetchTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _tasks.value = repository.getTasks()
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
                val newTask = repository.createTask(
                    Task(title = title, description = description, priority = priority, dueDate = dueDate)
                )
                _tasks.value = _tasks.value + newTask
                ReminderScheduler.scheduleTaskReminder(getApplication(), newTask)
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
                val updated = repository.updateTask(taskId, task.copy(status = "DONE"))
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
                repository.deleteTask(taskId)
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