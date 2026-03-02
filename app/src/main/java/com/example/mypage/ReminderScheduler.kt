package com.example.mypage

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import com.example.mypage.model.ClassItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ReminderScheduler {

    fun scheduleTestReminder(context: Context) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE)
                    as AlarmManager

        val intent =
            Intent(context, ClassReminderReceiver::class.java)

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val triggerTime =
            System.currentTimeMillis() + 30_000 // 30 seconds

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun scheduleTodayReminder(context: Context) {

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE)
                    as AlarmManager

        val intent =
            Intent(context, ClassReminderReceiver::class.java)

        val reminderList = getReminderMinutesList(context)

        // Cancel all possible previous alarms first
        val requestCodes = listOf(1001, 1002, 1003, 1004)
        for (code in requestCodes) {
            val pending =
                PendingIntent.getBroadcast(
                    context,
                    code,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )
            alarmManager.cancel(pending)
        }

        if (reminderList.isEmpty()) return

        val nextClassTime =
            getNextClassTime(context)
                ?: return

        for ((index, minutesBefore) in reminderList.withIndex()) {

            val triggerTime =
                nextClassTime.minusMinutes(minutesBefore.toLong())

            if (triggerTime.isAfter(LocalDateTime.now())) {

                val triggerMillis =
                    triggerTime.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

                val reminderIntent =
                    Intent(context, ClassReminderReceiver::class.java).apply {
                        putExtra("offset_minutes", minutesBefore)
                    }

                val pendingIntent =
                    PendingIntent.getBroadcast(
                        context,
                        1001 + index,
                        reminderIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or
                                PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun getNextClassTime(context: Context): LocalDateTime? {

        val prefs =
            context.getSharedPreferences("MyPagePrefs", Context.MODE_PRIVATE)

        val json = prefs.getString("timetable_json", null)
            ?: return null

        val selectedSection =
            prefs.getString("selected_section", null)
                ?: return null

        val type = object : TypeToken<List<ClassItem>>() {}.type
        val classList: List<ClassItem> =
            Gson().fromJson(json, type)

        val now = LocalDateTime.now()

        for (i in 0..6) {

            val date = now.plusDays(i.toLong())

            val day = when (date.dayOfWeek) {
                DayOfWeek.SUNDAY -> "SUN"
                DayOfWeek.MONDAY -> "MON"
                DayOfWeek.TUESDAY -> "TUE"
                DayOfWeek.WEDNESDAY -> "WED"
                DayOfWeek.THURSDAY -> "THU"
                DayOfWeek.FRIDAY -> "FRI"
                DayOfWeek.SATURDAY -> "SAT"
            }

            val classes =
                classList
                    .filter {
                        it.section == selectedSection &&
                                it.day == day
                    }
                    .sortedBy { java.time.LocalTime.parse(it.startTime) }

            for (classItem in classes) {

                val parts = classItem.startTime.split(":")
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()

                val classTime =
                    date.withHour(hour)
                        .withMinute(minute)
                        .withSecond(0)

                if (classTime.isAfter(now)) {
                    return classTime
                }
            }
        }

        return null
    }

    private fun parseToDateTime(time: String): LocalDateTime {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        return LocalDateTime.now()
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
    }

    private fun getReminderMinutesList(context: Context): List<Int> {

        val prefs =
            context.getSharedPreferences("MyPagePrefs", Context.MODE_PRIVATE)

        val set =
            prefs.getStringSet("notify_options", emptySet())
                ?: emptySet()

        if (set.contains("never")) return emptyList()

        val result = mutableListOf<Int>()

        if (set.contains("2h")) result.add(120)
        if (set.contains("1h")) result.add(60)
        if (set.contains("30m")) result.add(30)

        if (set.contains("custom")) {
            val minutes = prefs.getInt("custom_notify_minutes", 0)
            if (minutes > 0) result.add(minutes)
        }

        return result.sortedDescending()
    }
}