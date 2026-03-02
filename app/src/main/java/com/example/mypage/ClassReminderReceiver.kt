package com.example.mypage

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ClassReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val offsetMinutes =
            intent.getIntExtra("offset_minutes", -1)

        val subjects =
            ScheduleHelper.getTodaySubjects(context)

        if (subjects.isEmpty()) return

        // Convert offset into readable time text
        val timeText = when {
            offsetMinutes >= 60 && offsetMinutes % 60 == 0 -> {
                val hours = offsetMinutes / 60
                if (hours == 1) "1 hour"
                else "$hours hours"
            }
            offsetMinutes > 0 -> "$offsetMinutes mins"
            else -> "soon"
        }

        val titleText = "Classes start in $timeText"

        val bodyText =
            "Today's subjects are: " +
                    subjects.joinToString(", ")

        val notification =
            NotificationCompat.Builder(
                context,
                NotificationHelper.CHANNEL_ID
            )
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titleText)
                .setContentText(bodyText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(bodyText)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        manager.notify(1001 + offsetMinutes, notification)

        ReminderScheduler.scheduleTodayReminder(context)
    }
}