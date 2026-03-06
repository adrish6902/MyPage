package com.example.mypage

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.mypage.ui.theme.RoutineTheme
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.LaunchedEffect
import android.provider.Settings
import androidx.compose.runtime.DisposableEffect
import com.example.mypage.model.ClassItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class MainActivity : BaseActivity() {
    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("MyPagePrefs", MODE_PRIVATE)

        val classList: List<ClassItem>

        if (!prefs.contains("timetable_json")) {

            val inputStream = assets.open("timetable.json")
            val reader = InputStreamReader(inputStream)

            val type = object : TypeToken<List<ClassItem>>() {}.type
            val parsed: List<ClassItem> = Gson().fromJson(reader, type)

            val jsonString = Gson().toJson(parsed)

            prefs.edit()
                .putString("timetable_json", jsonString)
                .apply()

            classList = parsed

        } else {

            val savedJson = prefs.getString("timetable_json", "[]")

            val type = object : TypeToken<List<ClassItem>>() {}.type
            classList = Gson().fromJson(savedJson, type)
        }

        val isKiitStudent = prefs.getBoolean("is_kiit_student", true)

        if (isKiitStudent && !prefs.contains("selected_section")) {
            classList.firstOrNull()?.section?.let {
                prefs.edit().putString("selected_section", it).apply()
            }
        }

        NotificationHelper.createChannel(this)

        setContent {

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

            val notificationPermissionLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { }

            val context = LocalContext.current
            val prefs = context.getSharedPreferences("MyPagePrefs", MODE_PRIVATE)

            var themeMode by remember {
                mutableStateOf(
                    prefs.getString("theme_mode", "system") ?: "system"
                )
            }

            var startupCompleted by remember {
                mutableStateOf(
                    prefs.getBoolean("startup_completed", false)
                )
            }

            var isKiitStudent by remember {
                mutableStateOf(
                    prefs.getBoolean("is_kiit_student", true)
                )
            }


            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager =
                        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    if (!alarmManager.canScheduleExactAlarms()) {
                        val intent = Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        )
                        context.startActivity(intent)
                    } else {
                        ReminderScheduler.scheduleTodayReminder(context)
                    }
                } else {
                    ReminderScheduler.scheduleTodayReminder(context)
                }
            }

            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val alarmManager =
                                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                            if (alarmManager.canScheduleExactAlarms()) {
                                ReminderScheduler.scheduleTodayReminder(context)
                            }
                        }
                    }
                }

                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            RoutineTheme(themeMode = themeMode) {
                if (!startupCompleted) {

                    StartupScreen(

                        onKiitSelected = {

                            prefs.edit()
                                .putBoolean("startup_completed", true)
                                .putBoolean("is_kiit_student", true)
                                .apply()

                            startupCompleted = true
                            isKiitStudent = true
                        },

                        onNonKiitSelected = {

                            prefs.edit()
                                .putBoolean("startup_completed", true)
                                .putBoolean("is_kiit_student", false)
                                .apply()

                            startupCompleted = true
                            isKiitStudent = false
                        }
                    )

                } else {

                    val classesToUse =
                        if (isKiitStudent) classList else emptyList()

                    HomeScreen(
                        allClasses = classesToUse,
                        themeMode = themeMode,
                        onThemeChange = { newMode ->
                            themeMode = newMode
                            prefs.edit().putString("theme_mode", newMode).apply()
                        },
                        isKiitStudent = isKiitStudent
                    )
                }
            }

        }
    }
}
