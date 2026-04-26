package com.example.todolist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.AppContainer
import com.example.todolist.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TodoViewModel(
    private val container: AppContainer
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        observeTasks()
        observeDoneColor()
        importInitialData()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            container.getTasksUseCase().collectLatest { tasks ->
                _uiState.value = _uiState.value.copy(tasks = tasks)
            }
        }
    }

    private fun observeDoneColor() {
        viewModelScope.launch {
            container.preferencesRepository.doneColorEnabled.collectLatest { enabled ->
                _uiState.value = _uiState.value.copy(doneColorEnabled = enabled)
            }
        }
    }

    private fun importInitialData() {
        viewModelScope.launch {
            container.importTasksFromJsonUseCase()
        }
    }

    fun addTask(title: String, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            container.addTaskUseCase(
                Task(
                    title = title.trim(),
                    description = description.trim(),
                    isDone = false
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { container.updateTaskUseCase(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { container.deleteTaskUseCase(task) }
    }

    fun toggleTaskDone(task: Task) {
        viewModelScope.launch { container.toggleTaskDoneUseCase(task) }
    }

    fun setDoneColorEnabled(enabled: Boolean) {
        viewModelScope.launch { container.preferencesRepository.setDoneColorEnabled(enabled) }
    }
}

class TodoViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            return TodoViewModel(container) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
