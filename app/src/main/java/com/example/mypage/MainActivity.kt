package com.example.mypage

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
import com.example.mypage.model.ClassItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("MyPagePrefs", MODE_PRIVATE)

        val inputStream = assets.open("timetable.json")
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<ClassItem>>() {}.type
        val classList: List<ClassItem> = Gson().fromJson(reader, type)

        if (!prefs.contains("selected_section")) {
            classList.firstOrNull()?.section?.let {
                prefs.edit().putString("selected_section", it).apply()
            }
        }

        val selectedSection =
            prefs.getString("selected_section", classList.firstOrNull()?.section)
                ?: ""

        NotificationHelper.createChannel(this)
        ReminderScheduler.scheduleTodayReminder(this)

        setContent {

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

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }

            RoutineTheme(themeMode = themeMode) {

                HomeScreen(
                    allClasses = classList,
                    themeMode = themeMode,
                    onThemeChange = { newMode ->
                        themeMode = newMode
                        prefs.edit().putString("theme_mode", newMode).apply()
                    }
                )
            }
        }
    }
}
