package com.sms.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sms.app.data.model.Task
import com.sms.app.notifications.ReminderReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {

    private const val NUDGE_HOUR = 18
    private const val DUE_REMINDER_HOUR = 9
    private const val REMINDER_REQUEST_MULTIPLIER = 10
    private const val NUDGE_OFFSET = 1
    private const val DUE_OFFSET = 2
    private const val OVERDUE_OFFSET = 3

    fun scheduleTaskReminder(context: Context, task: Task) {
        val dueDate = task.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return
        val taskId = task.id ?: return
        ReminderReceiver.ensureChannel(context)

        if (dueDate.isBefore(LocalDate.now())) {
            scheduleReminder(
                context = context,
                requestCode = requestCode(taskId, OVERDUE_OFFSET),
                notificationId = requestCode(taskId, OVERDUE_OFFSET),
                triggerAtMillis = System.currentTimeMillis() + 60_000L,
                title = task.title ?: "Task overdue",
                message = overdueMessage(task)
            )
            return
        }

        val now = LocalDateTime.now()
        val nudgeAt = dueDate.minusDays(1).atTime(NUDGE_HOUR, 0)
        if (nudgeAt.isAfter(now)) {
            scheduleReminder(
                context = context,
                requestCode = requestCode(taskId, NUDGE_OFFSET),
                notificationId = requestCode(taskId, NUDGE_OFFSET),
                triggerAtMillis = nudgeAt.toMillis(),
                title = task.title ?: "Get started early",
                message = nudgeMessage(task)
            )
        }

        val dueReminder = dueDate.atTime(DUE_REMINDER_HOUR, 0)
        if (dueReminder.isAfter(now)) {
            scheduleReminder(
                context = context,
                requestCode = requestCode(taskId, DUE_OFFSET),
                notificationId = requestCode(taskId, DUE_OFFSET),
                triggerAtMillis = dueReminder.toMillis(),
                title = task.title ?: "Task due today",
                message = dueMessage(task)
            )
        } else if (dueDate.isEqual(LocalDate.now())) {
            scheduleReminder(
                context = context,
                requestCode = requestCode(taskId, OVERDUE_OFFSET),
                notificationId = requestCode(taskId, OVERDUE_OFFSET),
                triggerAtMillis = System.currentTimeMillis() + 60_000L,
                title = task.title ?: "Task due today",
                message = overdueMessage(task)
            )
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(NUDGE_OFFSET, DUE_OFFSET, OVERDUE_OFFSET).forEach { offset ->
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode(taskId, offset),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun scheduleReminder(
        context: Context,
        requestCode: Int,
        notificationId: Int,
        triggerAtMillis: Long,
        title: String,
        message: String
    ) {
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_MESSAGE, message)
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun requestCode(taskId: Long, offset: Int): Int {
        return ((taskId % Int.MAX_VALUE) * REMINDER_REQUEST_MULTIPLIER + offset).toInt()
    }

    private fun LocalDateTime.toMillis(): Long {
        return atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun nudgeMessage(task: Task): String {
        val title = task.title?.takeIf { it.isNotBlank() } ?: "this task"
        return "Start $title early so it does not pile up later today."
    }

    private fun dueMessage(task: Task): String {
        val title = task.title?.takeIf { it.isNotBlank() } ?: "this task"
        return "$title is due today. Finish it before it becomes overdue."
    }

    private fun overdueMessage(task: Task): String {
        val title = task.title?.takeIf { it.isNotBlank() } ?: "this task"
        return "$title is overdue. Open it now and clear it before moving on."
    }
}