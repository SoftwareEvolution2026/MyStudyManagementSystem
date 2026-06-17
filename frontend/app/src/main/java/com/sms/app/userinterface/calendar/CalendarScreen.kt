package com.sms.app.userinterface.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sms.app.data.model.StudySession
import com.sms.app.data.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(navController: NavController, vm: CalendarViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Calendar", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Tasks and study sessions by date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { vm.previousMonth() }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Previous month")
                }
                Text(
                    state.currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { vm.nextMonth() }) {
                    Icon(Icons.Outlined.ArrowForward, contentDescription = "Next month")
                }
            }

            Spacer(Modifier.height(12.dp))

            CalendarGrid(
                month = state.currentMonth,
                selectedDate = state.selectedDate,
                tasks = state.tasks,
                sessions = state.sessions,
                onDateSelected = vm::selectDate
            )

            Spacer(Modifier.height(16.dp))

            Text(
                state.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            val selectedTasks = state.tasks.filter { it.dueDate == state.selectedDate.toString() }
            val selectedSessions = state.sessions.filter { parseDate(it.startTime)?.toLocalDate() == state.selectedDate }

            if (selectedTasks.isEmpty() && selectedSessions.isEmpty()) {
                Text(
                    "No tasks or sessions on this date.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                selectedTasks.forEach { task ->
                    AgendaTaskCard(task)
                    Spacer(Modifier.height(8.dp))
                }
                selectedSessions.forEach { session ->
                    AgendaSessionCard(session)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Task reminders are scheduled automatically when a due date is set.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    tasks: List<Task>,
    sessions: List<StudySession>,
    onDateSelected: (LocalDate) -> Unit
) {
    val days = buildMonthCells(month)
    val taskDates = tasks.mapNotNull { task ->
        task.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }.groupingBy { it }.eachCount()
    val sessionDates = sessions.mapNotNull { parseDate(it.startTime)?.toLocalDate() }.groupingBy { it }.eachCount()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(min = 280.dp)
        ) {
            items(days) { day ->
                if (day == null) {
                    Spacer(modifier = Modifier.size(44.dp))
                } else {
                    val isSelected = day == selectedDate
                    val hasEvent = (taskDates[day] ?: 0) > 0 || (sessionDates[day] ?: 0) > 0
                    val backgroundColor = when {
                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                        hasEvent -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(backgroundColor)
                            .clickable { onDateSelected(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaTaskCard(task: Task) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.title ?: "Untitled task", style = MaterialTheme.typography.titleMedium)
            task.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Due: ${task.dueDate}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgendaSessionCard(session: StudySession) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(session.subject ?: "Study session", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Duration: ${session.durationMinutes ?: 0} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun buildMonthCells(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val leadingBlank = ((firstDay.dayOfWeek.value + 6) % 7)
    val days = mutableListOf<LocalDate?>()
    repeat(leadingBlank) { days.add(null) }
    for (day in 1..month.lengthOfMonth()) {
        days.add(month.atDay(day))
    }
    while (days.size % 7 != 0) {
        days.add(null)
    }
    return days
}

private fun parseDate(value: String?): LocalDateTime? {
    if (value.isNullOrBlank()) return null
    return runCatching { LocalDateTime.parse(value) }.getOrNull()
}