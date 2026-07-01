package com.sms.app.userinterface.tasks

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sms.app.data.model.Task
import com.sms.app.userinterface.components.BottomNavBar
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Composable
fun TasksScreen(navController: NavController, vm: TaskViewModel = viewModel()) {
    val tasks by vm.tasks.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("MEDIUM") }
    var selectedDueDate by remember { mutableStateOf<LocalDate?>(null) }

    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    val orderedTasks by remember(tasks) {
        derivedStateOf { tasks.sortedWith(taskPriorityComparator()) }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Outlined.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Tasks", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            if (isLoading && tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No tasks yet. Tap + to add one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(orderedTasks, key = { it.id ?: 0L }) { task ->
                        TaskCard(
                            task = task,
                            onMarkDone = { vm.markDone(task) },
                            onDelete = { vm.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New task") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Due date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(onClick = {
                        val initial = selectedDueDate ?: LocalDate.now()
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                selectedDueDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            initial.year,
                            initial.monthValue - 1,
                            initial.dayOfMonth
                        ).show()
                    }) {
                        Text(selectedDueDate?.toString() ?: "Pick a date")
                    }
                    if (selectedDueDate != null) {
                        TextButton(onClick = { selectedDueDate = null }) {
                            Text("Clear due date")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Priority",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                            FilterChip(
                                selected = selectedPriority == p,
                                onClick = { selectedPriority = p },
                                label = { Text(p) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        vm.createTask(newTitle, newDesc, selectedPriority, selectedDueDate?.toString())
                        newTitle = ""
                        newDesc = ""
                        selectedPriority = "MEDIUM"
                        selectedDueDate = null
                        showDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskCard(task: Task, onMarkDone: () -> Unit, onDelete: () -> Unit) {
    val isDone = task.status == "DONE"
    val priority = task.priority ?: "MEDIUM"
    val dueDate = task.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val today = LocalDate.now()
    val dueLabel = when {
        isDone -> "Completed"
        dueDate == null -> null
        dueDate.isBefore(today) -> "Overdue"
        dueDate.isEqual(today) -> "Due today"
        dueDate.isBefore(today.plusDays(3)) -> "Due soon"
        else -> "Due $dueDate"
    }
    val dueColor = when {
        isDone -> MaterialTheme.colorScheme.onSurfaceVariant
        dueDate == null -> MaterialTheme.colorScheme.onSurfaceVariant
        dueDate.isBefore(today) -> MaterialTheme.colorScheme.error
        dueDate.isEqual(today) -> MaterialTheme.colorScheme.tertiary
        dueDate.isBefore(today.plusDays(3)) -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val priorityColor = when (priority) {
        "HIGH"   -> MaterialTheme.colorScheme.error
        "MEDIUM" -> MaterialTheme.colorScheme.primary
        else     -> MaterialTheme.colorScheme.outline
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { if (!isDone) onMarkDone() }
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        task.title ?: "Untitled",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (dueLabel != null) {
                        Surface(
                            color = dueColor.copy(alpha = 0.12f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                dueLabel,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = dueColor
                            )
                        }
                    }
                    Surface(
                        color = priorityColor.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            priority,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor
                        )
                    }
                }
                if (!task.description.isNullOrBlank()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                task.dueDate?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Due: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun taskPriorityComparator(): Comparator<Task> {
    return compareBy<Task> { taskSortBucket(it) }
        .thenBy { taskDueDate(it) ?: LocalDate.MAX }
        .thenByDescending { priorityScore(it.priority) }
        .thenBy { it.title?.lowercase() ?: "" }
}

private fun taskSortBucket(task: Task): Int {
    if (task.status == "DONE") return 4
    val dueDate = taskDueDate(task) ?: return 3
    val today = LocalDate.now()
    return when {
        dueDate.isBefore(today) -> 0
        dueDate.isEqual(today) -> 1
        dueDate.isBefore(today.plusDays(3)) -> 2
        else -> 3
    }
}

private fun taskDueDate(task: Task): LocalDate? {
    val value = task.dueDate ?: return null
    return try {
        LocalDate.parse(value)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun priorityScore(priority: String?): Int {
    return when (priority) {
        "HIGH" -> 3
        "MEDIUM" -> 2
        "LOW" -> 1
        else -> 0
    }
}