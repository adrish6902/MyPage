package com.example.mypage

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mypage.model.ClassItem
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.graphics.Color
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allClasses: List<ClassItem>,
    themeMode: String,
    onThemeChange: (String) -> Unit
) {

    val haptic = LocalHapticFeedback.current

    var showNotificationDialog by remember { mutableStateOf(false) }
    var showCustomInput by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("MyPagePrefs", Context.MODE_PRIVATE)

    var showProfileMenu by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingSection by remember { mutableStateOf<String?>(null) }

    val sections = remember(allClasses) {
        allClasses.map { it.section }.distinct()
    }

    var persistedSection by remember {
        mutableStateOf(
            prefs.getString("selected_section", sections.firstOrNull())
                ?: sections.firstOrNull().orEmpty()
        )
    }

    var selectedSection by remember { mutableStateOf(persistedSection) }

    LaunchedEffect(sections) {
        if (persistedSection !in sections && sections.isNotEmpty()) {
            selectedSection = sections.first()
        }
    }

    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000)
        }
    }

    val sectionClasses = remember(selectedSection) {
        allClasses.filter { it.section == selectedSection }
    }

    val routineData = remember(sectionClasses) {
        sectionClasses.groupBy { it.day }
    }

    val days = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    val today = LocalDate.now()

    val sunday = today.minusDays(
        today.dayOfWeek.value % 7L
    )

    val weekDates = (0..6).map { offset ->
        sunday.plusDays(offset.toLong())
    }

    val initialIndex = when (LocalDate.now().dayOfWeek) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }

    var selectedDay by remember { mutableStateOf(initialIndex) }

    val selectedDayName = days[selectedDay]
    val todayClasses = routineData[selectedDayName] ?: emptyList()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    SectionSelector(
                        sections = sections,
                        selectedSection = selectedSection,
                        onSectionSelected = { section ->
                            if (section != selectedSection) {
                                selectedSection = section
                                pendingSection = section
                                showSaveDialog = true
                            }
                        }
                    )
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = {
                                showProfileMenu = true
                                performStrongHaptic(context)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile"
                            )
                        }

                        DropdownMenu(
                            expanded = showProfileMenu,
                            onDismissRequest = { showProfileMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Theme") },
                                onClick = {
                                    showProfileMenu = false
                                    showThemeDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Notifications") },
                                onClick = {
                                    showProfileMenu = false
                                    showNotificationDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    var totalDrag = 0f

                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            totalDrag += dragAmount.x
                        },
                        onDragEnd = {

                            if (totalDrag > 150) {
                                // Swiped Right
                                if (selectedDay > 0) {
                                    selectedDay--
                                    performStrongHaptic(context)
                                }
                            } else if (totalDrag < -150) {
                                // Swiped Left
                                if (selectedDay < 6) {
                                    selectedDay++
                                    performStrongHaptic(context)
                                }
                            }

                            totalDrag = 0f
                        }
                    )
                }
        ) {

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 2.dp,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                ),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEachIndexed { index, day ->

                        val displayText = if (selectedDay == index) {
                            weekDates[index].dayOfMonth
                                .toString()
                                .padStart(2, '0')
                        } else {
                            day
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        ) {
                            DayCircle(
                                text = displayText,
                                isSelected = selectedDay == index,
                                onClick = {
                                    if (selectedDay != index) {
                                        selectedDay = index
                                        performStrongHaptic(context)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (todayClasses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Classes Today",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = todayClasses,
                        key = { it.subject + it.startTime }
                    ) { item ->

                        val isTodaySelected =
                            weekDates[selectedDay] == LocalDate.now()

                        val isRunning = if (isTodaySelected) {

                            val formatter = DateTimeFormatter.ofPattern("HH:mm")

                            var start = LocalTime.parse(item.startTime, formatter)
                            var end = LocalTime.parse(item.endTime, formatter)

                            // 🔥 Fix 12-hour data without AM/PM
                            if (end.isBefore(start)) {
                                end = end.plusHours(12)
                            }

                            !currentTime.isBefore(start) &&
                                    currentTime.isBefore(end)

                        } else {
                            false
                        }

                        val storageFormatter = DateTimeFormatter.ofPattern("HH:mm")
                        val displayFormatter = DateTimeFormatter.ofPattern("hh:mm a")

                        val startTimeFormatted =
                            LocalTime.parse(item.startTime, storageFormatter)
                                .format(displayFormatter)

                        val endTimeFormatted =
                            LocalTime.parse(item.endTime, storageFormatter)
                                .format(displayFormatter)

                        RoutineCard(
                            item = RoutineItem(
                                subject = item.subject,
                                startTime = startTimeFormatted,
                                endTime = endTimeFormatted,
                                room = item.room ?: "-"
                            ),
                            isRunning = isRunning,
                            onClick = {}
                        )
                    }
                }
            }
        }

        // THEME DIALOG
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Select Theme") },
                text = {
                    Column {

                        ThemeOption(
                            text = "System Default",
                            isSelected = themeMode == "system",
                            onClick = {
                                onThemeChange("system")
                                showThemeDialog = false
                            }
                        )

                        ThemeOption(
                            text = "Light Mode",
                            isSelected = themeMode == "light",
                            onClick = {
                                onThemeChange("light")
                                showThemeDialog = false
                            }
                        )

                        ThemeOption(
                            text = "Dark Mode",
                            isSelected = themeMode == "dark",
                            onClick = {
                                onThemeChange("dark")
                                showThemeDialog = false
                            }
                        )

                        ThemeOption(
                            text = "Absolute Dark",
                            isSelected = themeMode == "amoled",
                            onClick = {
                                onThemeChange("amoled")
                                showThemeDialog = false
                            }
                        )
                    }
                },
                confirmButton = {}
            )
        }

        if (showCustomInput) {

            var hours by remember { mutableStateOf("") }
            var minutes by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCustomInput = false },
                title = { Text("Custom Reminder Time") },
                text = {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        OutlinedTextField(
                            value = hours,
                            onValueChange = {
                                if (it.all { ch -> ch.isDigit() }) {
                                    hours = it
                                }
                            },
                            label = { Text("Hours") },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = minutes,
                            onValueChange = {
                                if (it.all { ch -> ch.isDigit() }) {
                                    minutes = it
                                }
                            },
                            label = { Text("Minutes") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {

                            val h = hours.toIntOrNull() ?: 0
                            val m = minutes.toIntOrNull() ?: 0

                            val totalMinutes = (h * 60) + m

                            prefs.edit()
                                .putInt("custom_notify_minutes", totalMinutes)
                                .apply()

                            showCustomInput = false
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
        }

        if (showNotificationDialog) {

            var selectedOptions by remember {
                mutableStateOf(
                    prefs.getStringSet("notify_options", emptySet())
                        ?: emptySet()
                )
            }

            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                title = { Text("Notify me before") },
                text = {

                    val savedCustomMinutes =
                        prefs.getInt("custom_notify_minutes", 0)

                    val customPreviewText =
                        if (savedCustomMinutes > 0) {
                            val h = savedCustomMinutes / 60
                            val m = savedCustomMinutes % 60

                            when {
                                h > 0 && m > 0 -> "${h}h ${m}m"
                                h > 0 -> "${h}h"
                                else -> "${m}m"
                            }
                        } else {
                            null
                        }

                    Column {

                        NotificationOption(
                            label = "2 hours",
                            isChecked = selectedOptions.contains("2h"),
                            onToggle = {
                                selectedOptions =
                                    toggleSelection(selectedOptions, "2h")
                            }
                        )

                        NotificationOption(
                            label = "1 hour",
                            isChecked = selectedOptions.contains("1h"),
                            onToggle = {
                                selectedOptions =
                                    toggleSelection(selectedOptions, "1h")
                            }
                        )

                        NotificationOption(
                            label = "30 mins",
                            isChecked = selectedOptions.contains("30m"),
                            onToggle = {
                                selectedOptions =
                                    toggleSelection(selectedOptions, "30m")
                            }
                        )

                        NotificationOption(
                            label = "Custom",
                            subText = customPreviewText,
                            isChecked = selectedOptions.contains("custom"),
                            onToggle = {
                                selectedOptions = setOf("custom")
                                showCustomInput = true
                            }
                        )

                        NotificationOption(
                            label = "Never",
                            isChecked = selectedOptions.contains("never"),
                            onToggle = {
                                selectedOptions = setOf("never")
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {

                            if (selectedOptions.contains("custom")) {

                                val customMinutes =
                                    prefs.getInt("custom_notify_minutes", 0)

                                if (customMinutes > 0) {
                                    prefs.edit()
                                        .putStringSet("notify_options", setOf("custom"))
                                        .apply()
                                }

                            } else {

                                prefs.edit()
                                    .putStringSet("notify_options", selectedOptions)
                                    .apply()
                            }

                            // 🔥 ALWAYS schedule after saving
                            ReminderScheduler.scheduleTodayReminder(context)

                            showNotificationDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
        }

        if (showSaveDialog && pendingSection != null) {
            AlertDialog(
                onDismissRequest = {
                    showSaveDialog = false
                    pendingSection = null
                },
                title = { Text("Save as default?") },
                text = {
                    Text("Do you want to set $pendingSection as your default section?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {

                            prefs.edit()
                                .putString("selected_section", pendingSection)
                                .remove("notify_options")
                                .remove("custom_notify_minutes")
                                .apply()

                            // Cancel and reschedule (will effectively cancel since options are cleared)
                            ReminderScheduler.scheduleTodayReminder(context)

                            persistedSection = pendingSection!!
                            showSaveDialog = false
                            pendingSection = null
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showSaveDialog = false
                            pendingSection = null
                        }
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
}

fun performStrongHaptic(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createOneShot(
            40, // duration in ms
            VibrationEffect.DEFAULT_AMPLITUDE
        )
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(40)
    }
}

@Composable
fun ThemeOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionSelector(
    sections: List<String>,
    selectedSection: String,
    onSectionSelected: (String) -> Unit
) {

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (!expanded) {
                performStrongHaptic(context)
            }
            expanded = !expanded
        }
    ) {
        Surface(
            modifier = Modifier
                .menuAnchor()
                .width(110.dp),
            shape = RoundedCornerShape(30.dp),
            tonalElevation = 1.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedSection,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp),
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            sections.forEach { section ->
                DropdownMenuItem(
                    text = { Text(section) },
                    onClick = {
                        expanded = false
                        performStrongHaptic(context)
                        onSectionSelected(section)
                    }
                )
            }
        }
    }
}
@Composable
fun RoutineCard(
    item: RoutineItem,
    isRunning: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    val inputFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    val formattedStart = try {
        LocalTime.parse(item.startTime, inputFormatter)
            .format(outputFormatter)
    } catch (e: Exception) {
        item.startTime
    }

    val formattedEnd = try {
        LocalTime.parse(item.endTime, inputFormatter)
            .format(outputFormatter)
    } catch (e: Exception) {
        item.endTime
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                MaterialTheme.colorScheme.background == Color.Black ->
                    MaterialTheme.colorScheme.surface

                MaterialTheme.colorScheme.background.luminance() > 0.5f ->
                    MaterialTheme.colorScheme.surface

                else ->
                    MaterialTheme.colorScheme.surfaceContainerHigh
            }
        ),
        border = if (isRunning)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRunning) 14.dp else 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.room,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedStart,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = formattedEnd,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Composable
fun DayCircle(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        label = "scaleAnim"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 4.dp else 1.dp,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = if (isSelected)
                    MaterialTheme.typography.titleMedium
                else
                    MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected)
                    FontWeight.Bold
                else
                    FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun toggleSelection(
    current: Set<String>,
    value: String
): Set<String> {

    val newSet = current.toMutableSet()

    newSet.remove("custom")
    newSet.remove("never")

    if (newSet.contains(value)) {
        newSet.remove(value)
    } else {
        newSet.add(value)
    }

    return newSet
}

@Composable
fun NotificationOption(
    label: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    subText: String? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (subText != null) {
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() }
            )
        }
    }
}