package com.sms.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sms.app.data.model.Task
import com.sms.app.notifications.ReminderReceiver
import java.time.LocalDate
import java.time.ZoneId

object ReminderScheduler {

    fun scheduleTaskReminder(context: Context, task: Task) {
        val dueDate = task.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return
        val taskId = task.id ?: return
        val triggerAtMillis = dueDate.atTime(9, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerAtMillis <= System.currentTimeMillis()) return

        ReminderReceiver.ensureChannel(context)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TITLE, task.title ?: "Task reminder")
            putExtra(ReminderReceiver.EXTRA_MESSAGE, task.description ?: "You have an upcoming task due today.")
            putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, taskId.toInt())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
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

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}