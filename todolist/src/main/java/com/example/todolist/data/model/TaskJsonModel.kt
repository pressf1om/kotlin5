package com.example.todolist.data.model

data class TaskJsonModel(
    val title: String,
    val description: String,
    val isDone: Boolean = false
)
