package com.isaiahvonrundstedt.fokus.features.shared.composables.overrides

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.core.compose.FokusTheme

@Composable
fun FokusTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    elevation: Dp = AppBarDefaults.TopAppBarElevation) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon ?: { Icon(painterResource(id = R.drawable.ic_hero_arrow_left_24), contentDescription = "back")},
        actions = actions,
        backgroundColor = FokusTheme.colors.surface,
        elevation = elevation,
        modifier = Modifier.padding(AppBarDefaults.ContentPadding)
    )
}

@Preview
@Composable
private fun FokusTopAppBarPreview() {
    FokusTopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        navigationIcon = {
            Icon(painterResource(id = R.drawable.ic_hero_arrow_left_24), 
                contentDescription = "Back") 
        },
        actions = {
            OutlinedButton(onClick = { /*TODO*/ }) {
                Text(stringResource(id = R.string.app_name))
            }
        }
    )
}