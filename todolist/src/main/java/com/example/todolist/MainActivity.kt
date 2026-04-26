package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.todolist.navigation.TodoNavGraph
import com.example.todolist.presentation.viewmodel.TodoViewModel
import com.example.todolist.presentation.viewmodel.TodoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as TodoApplication
        val viewModel = ViewModelProvider(
            this,
            TodoViewModelFactory(app.container)
        )[TodoViewModel::class.java]

        setContent {
            TodoNavGraph(viewModel = viewModel)
        }
    }
}
