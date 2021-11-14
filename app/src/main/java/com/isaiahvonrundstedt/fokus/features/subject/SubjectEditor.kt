package com.isaiahvonrundstedt.fokus.features.subject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.composables.overrides.FokusTopAppBar

@Preview(showBackground = true)
@Composable
fun SubjectEditorRoute() {
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            FokusTopAppBar(
                title = { Spacer(modifier = Modifier.width(16.dp)) },
                actions = {
                    Button(
                        onClick = {}, modifier = Modifier.clip(
                            RoundedCornerShape(
                                dimensionResource(id = R.dimen.shape_radius)
                            )
                        )
                    ) {
                        Text(stringResource(R.string.button_save))
                    }
                },
                elevation = 0.dp
            )
        },
        content = {
            Column(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.container_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.composable_spacing))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.composable_spacing_siblings))) {
                    Text(stringResource(id = R.string.field_subject_code))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        placeholder = {
                            Text("Intech 1100")
                        },
                        leadingIcon = {
                            Icon(painterResource(id = R.drawable.ic_hero_hashtag_24),
                                contentDescription = stringResource(id = R.string.field_subject_code))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.composable_spacing_siblings))) {
                    Text(stringResource(id = R.string.field_description))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = {
                            Text("Web Design")
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(id = R.drawable.ic_hero_pencil_24),
                                contentDescription = stringResource(R.string.field_description)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.composable_spacing_siblings))) {
                    Text(stringResource(R.string.field_schedule))
                    OutlinedButton(onClick = { /*TODO*/ }) {
                        Text(stringResource(R.string.button_add))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.composable_spacing_siblings))) {
                    Text(stringResource(R.string.field_color))
                }
            }
        }
    )
}