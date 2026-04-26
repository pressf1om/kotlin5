package com.example.todolist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolist.presentation.ui.screen.TaskEditScreen
import com.example.todolist.presentation.ui.screen.TodoListScreen
import com.example.todolist.presentation.viewmodel.TodoViewModel

object TodoDestinations {
    const val LIST = "list"
    const val EDIT = "edit"
    const val TASK_ID_ARG = "taskId"
}

@Composable
fun TodoNavGraph(
    viewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = TodoDestinations.LIST,
        modifier = modifier
    ) {
        composable(TodoDestinations.LIST) {
            TodoListScreen(
                uiState = state,
                onToggleDoneColor = viewModel::setDoneColorEnabled,
                onToggleTask = viewModel::toggleTaskDone,
                onDeleteTask = viewModel::deleteTask,
                onEditTask = { task ->
                    navController.navigate("${TodoDestinations.EDIT}/${task.id}")
                },
                onAddTask = {
                    navController.navigate("${TodoDestinations.EDIT}/-1")
                }
            )
        }

        composable(
            route = "${TodoDestinations.EDIT}/{${TodoDestinations.TASK_ID_ARG}}",
            arguments = listOf(
                navArgument(TodoDestinations.TASK_ID_ARG) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt(TodoDestinations.TASK_ID_ARG) ?: -1
            val task = state.tasks.firstOrNull { it.id == taskId }

            TaskEditScreen(
                task = task,
                onBack = { navController.popBackStack() },
                onSave = { title, description ->
                    if (task == null) {
                        viewModel.addTask(title, description)
                    } else {
                        viewModel.updateTask(task.copy(title = title, description = description))
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}
