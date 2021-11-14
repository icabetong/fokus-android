package com.isaiahvonrundstedt.fokus.features.subject

import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable

@Composable
fun SubjectEditorRoute() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Sample") },
                actions = {
                    OutlinedButton(onClick = {}) {
                        Text("Save")
                    }
                }
            )
        }
    ) {

    }
}