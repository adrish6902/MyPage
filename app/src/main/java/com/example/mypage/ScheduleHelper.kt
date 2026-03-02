package com.example.mypage

import android.content.Context
import com.example.mypage.model.ClassItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
import java.time.LocalDate

object ScheduleHelper {

    fun getTodayFirstPeriodTime(context: Context): String? {

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

        val today = when (LocalDate.now().dayOfWeek) {
            DayOfWeek.SUNDAY -> "SUN"
            DayOfWeek.MONDAY -> "MON"
            DayOfWeek.TUESDAY -> "TUE"
            DayOfWeek.WEDNESDAY -> "WED"
            DayOfWeek.THURSDAY -> "THU"
            DayOfWeek.FRIDAY -> "FRI"
            DayOfWeek.SATURDAY -> "SAT"
        }

        val todayClasses =
            classList
                .filter {
                    it.section == selectedSection &&
                            it.day == today
                }
                .sortedBy { java.time.LocalTime.parse(it.startTime) }

        return todayClasses.firstOrNull()?.startTime
    }

    fun getTodaySubjects(context: Context): List<String> {

        val prefs =
            context.getSharedPreferences("MyPagePrefs", Context.MODE_PRIVATE)

        val json = prefs.getString("timetable_json", null)
            ?: return emptyList()

        val selectedSection =
            prefs.getString("selected_section", null)
                ?: return emptyList()

        val type = object : TypeToken<List<ClassItem>>() {}.type
        val classList: List<ClassItem> =
            Gson().fromJson(json, type)

        val today = when (LocalDate.now().dayOfWeek) {
            DayOfWeek.SUNDAY -> "SUN"
            DayOfWeek.MONDAY -> "MON"
            DayOfWeek.TUESDAY -> "TUE"
            DayOfWeek.WEDNESDAY -> "WED"
            DayOfWeek.THURSDAY -> "THU"
            DayOfWeek.FRIDAY -> "FRI"
            DayOfWeek.SATURDAY -> "SAT"
        }

        return classList
            .filter {
                it.section == selectedSection &&
                        it.day == today
            }
            .sortedBy { java.time.LocalTime.parse(it.startTime) }
            .map { it.subject }
    }
}

