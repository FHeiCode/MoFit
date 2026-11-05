package com.fheicode.mofit.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fheicode.mofit.data.MoFitRepository
import com.fheicode.mofit.ui.screens.ExercisesScreen
import com.fheicode.mofit.ui.screens.PlanScreen
import com.fheicode.mofit.ui.screens.ProgressScreen
import com.fheicode.mofit.ui.screens.TodayScreen
import com.fheicode.mofit.ui.theme.MoFitColors
import kotlinx.coroutines.launch

private enum class Tab(val label: String, val icon: ImageVector) {
    TODAY("Heute", Icons.Rounded.Bolt),
    PLAN("Plan", Icons.Rounded.CalendarMonth),
    PROGRESS("Fortschritt", Icons.AutoMirrored.Rounded.ShowChart),
    EXERCISES("Übungen", Icons.Rounded.FitnessCenter),
}

@Composable
fun MoFitRoot(repository: MoFitRepository) {
    val data by repository.data.collectAsState()
    var tab by rememberSaveable { mutableStateOf(Tab.TODAY) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snack ->
                Snackbar(
                    snack,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MoFitColors.Lime,
                    contentColor = Color.Black,
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MoFitColors.Surface) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Icon(t.icon, contentDescription = null) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = MoFitColors.Lime,
                            indicatorColor = MoFitColors.Lime,
                            unselectedIconColor = MoFitColors.TextSecondary,
                            unselectedTextColor = MoFitColors.TextSecondary,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                Tab.TODAY -> TodayScreen(data, repository, showMessage)
                Tab.PLAN -> PlanScreen(data, repository, showMessage)
                Tab.PROGRESS -> ProgressScreen(data, repository, showMessage)
                Tab.EXERCISES -> ExercisesScreen(data, repository, showMessage)
            }
        }
    }
}
