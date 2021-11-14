package com.isaiahvonrundstedt.fokus.features.core.compose

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.isaiahvonrundstedt.fokus.R
import com.isaiahvonrundstedt.fokus.features.event.EventScreenLayout
import com.isaiahvonrundstedt.fokus.features.shared.composables.custom.BottomBar
import com.isaiahvonrundstedt.fokus.features.shared.composables.custom.HomeSections
import com.isaiahvonrundstedt.fokus.features.subject.SubjectEditorRoute
import com.isaiahvonrundstedt.fokus.features.subject.SubjectScreenLayout
import com.isaiahvonrundstedt.fokus.features.task.TaskScreenLayout
import com.isaiahvonrundstedt.fokus.features.task.TaskViewModel

@Composable
fun FokusApp() {
    val navController = rememberNavController()
    ProvideWindowInsets {
        FokusTheme {
            ConstructRoutes(navController = navController) { route ->
                when (route) {
                    HomeSections.Tasks.route ->
                        navController.navigate(OuterRoutes.TaskEditorScreen.route)
                    HomeSections.Events.route ->
                        navController.navigate(OuterRoutes.EventEditorScreen.route)
                    HomeSections.Subjects.route ->
                        navController.navigate(OuterRoutes.SubjectEditorScreen.route)
                }
            }
        }
    }
}

@Composable
fun MainScreen(onNavigate: (String) -> Unit) {
    val innerNavController = rememberNavController()

    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route ?: "home/tasks"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.app_name),
                        textAlign = TextAlign.Center
                    )
                },
                backgroundColor = Color.White,
            )
        },
        bottomBar = {
            BottomBar(
                tabs = HomeSections.values(),
                currentRoute = route,
                navigateToRoute = {
                    innerNavController.navigate(it)
                })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon = {
                    Icon(
                        painterResource(
                            id = R.drawable.ic_hero_plus_24
                        ),
                        contentDescription = stringResource(id = R.string.button_add)
                    )
                },
                text = { Text(stringResource(id = R.string.button_add)) },
                onClick = {
                    onNavigate(route)
                }
            )
        }
    ) {
        NavHost(innerNavController, startDestination = HomeSections.Tasks.route) {
            composable(HomeSections.Tasks.route) {
                val viewModel = hiltViewModel<TaskViewModel>()
                TaskScreenLayout(viewModel = viewModel)
            }
            composable(HomeSections.Events.route) { EventScreenLayout() }
            composable(HomeSections.Subjects.route) { SubjectScreenLayout() }
        }
    }
}

@Composable
fun ConstructRoutes(navController: NavHostController, onNavigate: (String) -> Unit) {
    NavHost(navController, startDestination = OuterRoutes.MainScreen.route) {
        composable(OuterRoutes.MainScreen.route) { MainScreen(onNavigate = onNavigate) }
        composable(OuterRoutes.TaskEditorScreen.route) { }
        composable(OuterRoutes.EventEditorScreen.route) { }
        composable(OuterRoutes.SubjectEditorScreen.route) { SubjectEditorRoute() }
    }
}

enum class OuterRoutes(val route: String) {
    MainScreen("outer/main"),
    TaskEditorScreen("editor/task"),
    EventEditorScreen("editor/event"),
    SubjectEditorScreen("editor/subject")
}