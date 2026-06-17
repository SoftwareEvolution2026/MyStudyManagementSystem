package com.sms.app.userinterface.pomodoro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sms.app.userinterface.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(navController: NavController, vm: PomodoroViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.fetchTasks()
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    val bgColor by animateColorAsState(
        targetValue = if (state.phase == PomodoroPhase.WORK)
            MaterialTheme.colorScheme.errorContainer
        else
            MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(600),
        label = "phase color"
    )

    val timeLabel = "%02d:%02d".format(
        state.secondsRemaining / 60,
        state.secondsRemaining % 60
    )
    val phaseLabel = if (state.phase == PomodoroPhase.WORK) "Focus time" else "Break time"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Pomodoro Timer", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Sessions completed: ${state.sessionsCompleted}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Subject Input (Visible only during WORK phase setup)
            if (state.phase == PomodoroPhase.WORK && !state.isRunning) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.subject,
                        onValueChange = { vm.setSubject(it) },
                        label = { Text("What are you studying?") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(12.dp))
                    
                    var dropdownExpanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = state.selectedTaskName,
                            onValueChange = {},
                            label = { Text("Link to task") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            singleLine = true,
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            enabled = state.availableTasks.isNotEmpty() || state.isLoadingTasks
                        )
                        
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    vm.selectTask(null)
                                    dropdownExpanded = false
                                }
                            )
                            state.availableTasks.forEach { task ->
                                DropdownMenuItem(
                                    text = { Text(task.title ?: "Untitled task") },
                                    onClick = {
                                        vm.selectTask(task)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (state.isLoadingTasks) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
                    } else if (state.availableTasks.isEmpty()) {
                        Text(
                            "No tasks available to link. Create one in Tasks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else if (state.phase == PomodoroPhase.WORK) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Studying: ${state.subject}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (state.selectedTaskId != null) {
                        Text(
                            "Task: ${state.selectedTaskName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Timer circle
            Surface(
                color = bgColor,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.size(240.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(timeLabel, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                    Text(phaseLabel, style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { vm.reset() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Reset")
                        Spacer(Modifier.width(6.dp))
                        Text("Reset")
                    }
                    Button(
                        onClick = { vm.startPause() },
                        modifier = Modifier.width(130.dp)
                    ) {
                        Icon(
                            if (state.isRunning) Icons.Outlined.Pause
                            else Icons.Outlined.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (state.isRunning) "Pause" else "Start")
                    }
                }
                
                if (!state.isRunning) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Work: ${state.workMinutes} min",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = state.workMinutes.toFloat(),
                        onValueChange = { vm.setWorkMinutes(it.toInt()) },
                        valueRange = 15f..60f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Break: ${state.breakMinutes} min",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = state.breakMinutes.toFloat(),
                        onValueChange = { vm.setBreakMinutes(it.toInt()) },
                        valueRange = 5f..15f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}