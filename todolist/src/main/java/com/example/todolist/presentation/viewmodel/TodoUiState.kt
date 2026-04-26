package com.example.todolist.presentation.viewmodel

import com.example.todolist.domain.model.Task

data class TodoUiState(
    val tasks: List<Task> = emptyList(),
    val doneColorEnabled: Boolean = true
)
