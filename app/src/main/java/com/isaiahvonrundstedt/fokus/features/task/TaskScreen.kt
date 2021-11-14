package com.isaiahvonrundstedt.fokus.features.task

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.components.EmptyState

@Composable
fun TaskScreenLayout(viewModel: TaskViewModel) {
    val tasks: List<TaskPackage> by viewModel.tasks.observeAsState(listOf())
    if (tasks.isNotEmpty()) {
        LazyColumn {
            items(items = tasks, itemContent = { item ->
                TaskRow(item.task.name ?: "")
            })
        }
    } else {
        EmptyState(
            icon = R.drawable.ic_hero_check_24,
            title = R.string.empty_view_no_tasks_title,
            subtitle = R.string.empty_view_no_tasks_summary
        )
    }
}

@Composable
fun TaskRow(name: String) {
    Row {
        Text(name)
    }
}