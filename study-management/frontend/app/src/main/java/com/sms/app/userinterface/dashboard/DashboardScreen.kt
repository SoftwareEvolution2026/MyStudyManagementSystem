package com.sms.app.userinterface.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sms.app.navigation.Screen
import com.sms.app.userinterface.components.BottomNavBar
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(navController: NavController, vm: DashboardViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    var showGoalsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.loadDashboard()
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Good day!", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Your week at a glance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { navController.navigate(Screen.Calendar.route) }) {
                    Text("Open calendar")
                }
                OutlinedButton(onClick = { showGoalsDialog = true }) {
                    Text("Edit goals")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.isLoading && state.stats.totalTasks == 0 && state.stats.completedTasks == 0) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { StatCard("Total tasks",    "${state.stats.totalTasks}") }
                    item { StatCard("Completed",      "${state.stats.completedTasks}") }
                    item { StatCard("Pomodoros",      "${state.stats.pomodoroSessionsThisWeek}") }
                    item { StatCard("Study sessions", "${state.stats.studySessionsThisWeek}") }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Current streak", "${state.currentStreakDays}d", modifier = Modifier.weight(1f))
                    StatCard("Today", "${state.todayStudyMinutes}m", modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                GoalProgressCard(
                    title = "Daily study goal",
                    progressLabel = "${state.todayStudyMinutes} / ${state.dailyStudyGoalMinutes} min",
                    progress = progressFraction(state.todayStudyMinutes, state.dailyStudyGoalMinutes)
                )

                Spacer(Modifier.height(12.dp))

                GoalProgressCard(
                    title = "Weekly task goal",
                    progressLabel = "${state.stats.completedTasks} / ${state.weeklyTaskGoal} tasks",
                    progress = progressFraction(state.stats.completedTasks, state.weeklyTaskGoal)
                )
            }
        }
    }

    if (showGoalsDialog) {
        var dailyGoal by remember { mutableStateOf(state.dailyStudyGoalMinutes.toFloat()) }
        var weeklyGoal by remember { mutableStateOf(state.weeklyTaskGoal.toFloat()) }

        AlertDialog(
            onDismissRequest = { showGoalsDialog = false },
            title = { Text("Edit goals") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Daily study goal: ${dailyGoal.roundToInt()} minutes")
                    Slider(
                        value = dailyGoal,
                        onValueChange = { dailyGoal = it },
                        valueRange = 30f..360f,
                        steps = 21
                    )
                    Text("Weekly task goal: ${weeklyGoal.roundToInt()} tasks")
                    Slider(
                        value = weeklyGoal,
                        onValueChange = { weeklyGoal = it },
                        valueRange = 1f..20f,
                        steps = 18
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateGoals(dailyGoal.roundToInt(), weeklyGoal.roundToInt())
                    showGoalsDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showGoalsDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GoalProgressCard(title: String, progressLabel: String, progress: Float) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                progressLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun progressFraction(current: Int, target: Int): Float {
    if (target <= 0) return 0f
    return (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
}
