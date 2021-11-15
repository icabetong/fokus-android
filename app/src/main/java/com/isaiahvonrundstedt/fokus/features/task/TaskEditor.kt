package com.isaiahvonrundstedt.fokus.features.task

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.Scaffold
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.composables.overrides.FokusTopAppBar

@Preview(showBackground = true)
@Composable
fun TaskEditorRoute() {

    Scaffold(
        topBar = {
            FokusTopAppBar(
                title = { Spacer(Modifier.width(16.dp)) },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(painterResource(id = R.drawable.ic_hero_arrow_left_24),
                            contentDescription = Icons.Default.ArrowBack.name)
                    }
                },
                actions = {
                    Button(onClick = {},
                        modifier = Modifier.padding(end = dimensionResource(id = R.dimen.composable_spacing))) {
                        Text(stringResource(id = R.string.button_save))
                    }
                },
                elevation = 0.dp
            )
        },
        content = {
            Column(
                Modifier.padding(it),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.composable_spacing))) {

            }
        }
    )
}