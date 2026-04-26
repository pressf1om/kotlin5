package com.example.todolist

import android.app.Application
import com.example.todolist.data.local.TodoDatabase
import com.example.todolist.data.preferences.TodoPreferencesRepository
import com.example.todolist.data.repository.TodoRepositoryImpl
import com.example.todolist.domain.usecase.AddTaskUseCase
import com.example.todolist.domain.usecase.DeleteTaskUseCase
import com.example.todolist.domain.usecase.GetTasksUseCase
import com.example.todolist.domain.usecase.ImportTasksFromJsonUseCase
import com.example.todolist.domain.usecase.ToggleTaskDoneUseCase
import com.example.todolist.domain.usecase.UpdateTaskUseCase

class TodoApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val database = TodoDatabase.getDatabase(application)
    private val preferences = TodoPreferencesRepository(application)
    private val repository = TodoRepositoryImpl(database.todoDao(), application)

    val getTasksUseCase = GetTasksUseCase(repository)
    val addTaskUseCase = AddTaskUseCase(repository)
    val updateTaskUseCase = UpdateTaskUseCase(repository)
    val deleteTaskUseCase = DeleteTaskUseCase(repository)
    val toggleTaskDoneUseCase = ToggleTaskDoneUseCase(repository)
    val importTasksFromJsonUseCase = ImportTasksFromJsonUseCase(repository, preferences)
    val preferencesRepository = preferences
}
