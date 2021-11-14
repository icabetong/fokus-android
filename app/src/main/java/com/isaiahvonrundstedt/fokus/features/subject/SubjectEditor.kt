package com.isaiahvonrundstedt.fokus.features.subject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.shared.composables.overrides.FokusTopAppBar

@Preview(showBackground = true)
@Composable
fun SubjectEditorRoute() {
    Scaffold(
        topBar = {
            FokusTopAppBar(
                title = { Spacer(modifier = Modifier.width(16.dp)) },
                actions = {
                    Button(onClick = {}, modifier = Modifier.clip(CircleShape)) {
                        Text(stringResource(R.string.button_save))
                    }
                },
                elevation = 0.dp
            )
        },
        content = {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = "",
                    leadingIcon = {
                        Icon(painterResource(id = R.drawable.ic_hero_hashtag_24),
                            contentDescription = stringResource(R.string.field_subject_code))
                    },
                    label = {
                        Text(stringResource(id = R.string.field_subject_code))
                    },
                    onValueChange = {},
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.field_description))
                    },
                    leadingIcon = {
                        Icon(painterResource(id = R.drawable.ic_hero_pencil_24),
                        contentDescription = stringResource(R.string.field_description))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Column {
                    Text(stringResource(R.string.field_schedule))
                    OutlinedButton(onClick = { /*TODO*/ }) {
                        Text(stringResource(R.string.button_add))
                    }
                }
            }
        }
    )
}