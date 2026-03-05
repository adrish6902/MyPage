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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allClasses: List<ClassItem>,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    isKiitStudent: Boolean
) {

    val haptic = LocalHapticFeedback.current

    var showNotificationDialog by remember { mutableStateOf(false) }

    var editMode by remember { mutableStateOf(false) }

    var showCustomInput by remember { mutableStateOf(false) }

    var editingClass by remember { mutableStateOf<CustomClass?>(null) }

    var editBackup by remember { mutableStateOf<List<CustomClass>>(emptyList()) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("MyPagePrefs", Context.MODE_PRIVATE)

    var customClasses by remember {

        val gson = Gson()

        val savedJson = prefs.getString("custom_classes", null)

        val savedList: List<CustomClass> =
            if (savedJson != null) {
                val type = object : TypeToken<List<CustomClass>>() {}.type
                gson.fromJson(savedJson, type)
            } else {
                emptyList()
            }

        mutableStateOf(savedList)
    }

    var showAddDialog by remember { mutableStateOf(false) }

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


    val todayClasses = if (isKiitStudent) {
        routineData[selectedDayName] ?: emptyList()
    } else {
        emptyList()
    }

    val customForDay = customClasses.filter { it.day == selectedDayName }

    val customConverted = customForDay.mapIndexed { index, it ->
        ClassItem(
            subject = it.subject,
            startTime = it.startTime,
            endTime = it.endTime,
            room = it.room,
            section = "CUSTOM",
            day = it.day
        ) to index
    }

    val untimedCustom =
        customConverted.filter { it.first.startTime.isBlank() }

    val timedCustom =
        customConverted.filter { it.first.startTime.isNotBlank() }

    val sortedTimed =
        timedCustom.sortedBy { it.first.startTime }

    val sortedUntimed =
        untimedCustom.sortedBy { it.second }

    val combinedClasses =
        todayClasses.map { it to false } +
                sortedUntimed.map { it.first to true } +
                sortedTimed.map { it.first to true }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(

                navigationIcon = {

                    if (!isKiitStudent) {

                        if (!editMode) {

                            Surface(
                                shape = CircleShape,
                                tonalElevation = 2.dp,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                onClick = {
                                    performStrongHaptic(context)

                                    editBackup = customClasses.toList()

                                    editMode = true
                                }
                            ) {
                                Box(
                                    modifier = Modifier.padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Routine"
                                    )
                                }
                            }

                        } else {

                            Surface(
                                shape = RoundedCornerShape(50),
                                tonalElevation = 2.dp,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                onClick = {
                                    performStrongHaptic(context)

                                    customClasses = editBackup
                                    editMode = false
                                }
                            ) {
                                Text(
                                    text = "Cancel",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                        }
                    }

                },
                title = {

                    if (isKiitStudent) {

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

                    } else {

                        Text(
                            text = if (editMode) "Edit Mode" else "MyPage",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                    }
                },
                actions = {
                    Box {
                        Surface(
                            shape = CircleShape,
                            tonalElevation = 2.dp,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = {
                                showProfileMenu = true
                                performStrongHaptic(context)
                            }
                        ) {
                            Box(
                                modifier = Modifier.padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile"
                                )
                            }
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

            if (combinedClasses.isEmpty() && !editMode) {
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
                        items = combinedClasses,
                        key = { it.first.subject + it.first.startTime }
                    ) { pair ->

                        val item = pair.first
                        val isCustom = pair.second

                        val isTodaySelected =
                            weekDates[selectedDay] == LocalDate.now()

                        val isRunning = if (
                            isTodaySelected &&
                            item.startTime.isNotBlank() &&
                            item.endTime.isNotBlank()
                        ) {

                            val formatter = DateTimeFormatter.ofPattern("HH:mm")

                            var start = LocalTime.parse(item.startTime, formatter)
                            var end = LocalTime.parse(item.endTime, formatter)

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
                            if (item.startTime.isBlank()) "-"
                            else LocalTime.parse(item.startTime, storageFormatter)
                                .format(displayFormatter)

                        val endTimeFormatted =
                            if (item.endTime.isBlank()) "-"
                            else LocalTime.parse(item.endTime, storageFormatter)
                                .format(displayFormatter)

                        var visible by remember { mutableStateOf(true) }

                        AnimatedVisibility(
                            visible = visible,
                            exit = slideOutHorizontally { it } + fadeOut()
                        ) {

                            RoutineCard(
                                item = RoutineItem(
                                    subject = item.subject,
                                    startTime = startTimeFormatted,
                                    endTime = endTimeFormatted,
                                    room = item.room ?: "-"
                                ),
                                isRunning = isRunning,
                                editMode = editMode,
                                allowMenu = isCustom,
                                onDelete = {

                                    visible = false

                                    kotlinx.coroutines.GlobalScope.launch {
                                        delay(220)

                                        customClasses =
                                            customClasses.filterNot {
                                                it.subject == item.subject &&
                                                        it.startTime == item.startTime &&
                                                        it.day == item.day
                                            }
                                    }
                                },
                                onEdit = {

                                    editingClass =
                                        customClasses.find {
                                            it.subject == item.subject &&
                                                    it.startTime == item.startTime &&
                                                    it.day == item.day
                                        }

                                    showAddDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }

        if (editMode) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {

                // Add Button
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart),
                    shape = RoundedCornerShape(50),
                    tonalElevation = 4.dp,
                    onClick = {
                        showAddDialog = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Class"
                        )
                    }
                }

                // Save Button
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd),
                    shape = RoundedCornerShape(50),
                    tonalElevation = 4.dp,
                    onClick = {

                        val gson = Gson()

                        val json = gson.toJson(customClasses)

                        prefs.edit()
                            .putString("custom_classes", json)
                            .apply()

                        editMode = false
                    }
                ) {
                    Text(
                        text = "Save",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
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

        if (showAddDialog) {

            var subject by remember { mutableStateOf(editingClass?.subject ?: "") }
            var room by remember { mutableStateOf(editingClass?.room ?: "") }

            var startHour by remember { mutableStateOf("") }
            var startMinute by remember { mutableStateOf("") }
            var startAmPm by remember { mutableStateOf("") }

            var endHour by remember { mutableStateOf("") }
            var endMinute by remember { mutableStateOf("") }
            var endAmPm by remember { mutableStateOf("") }

            LaunchedEffect(editingClass) {

                editingClass?.let {

                    if (it.startTime.isNotBlank()) {

                        val time = LocalTime.parse(it.startTime)
                        val hour = time.hour
                        val minute = time.minute

                        val ampm = if (hour >= 12) "PM" else "AM"
                        val displayHour =
                            when {
                                hour == 0 -> 12
                                hour > 12 -> hour - 12
                                else -> hour
                            }

                        startHour = displayHour.toString().padStart(2,'0')
                        startMinute = minute.toString().padStart(2,'0')
                        startAmPm = ampm
                    }

                    if (it.endTime.isNotBlank()) {

                        val time = LocalTime.parse(it.endTime)
                        val hour = time.hour
                        val minute = time.minute

                        val ampm = if (hour >= 12) "PM" else "AM"
                        val displayHour =
                            when {
                                hour == 0 -> 12
                                hour > 12 -> hour - 12
                                else -> hour
                            }

                        endHour = displayHour.toString().padStart(2,'0')
                        endMinute = minute.toString().padStart(2,'0')
                        endAmPm = ampm
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },

                title = {
                    Text("Add Class")
                },

                text = {

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject Name") }
                        )

                        OutlinedTextField(
                            value = room,
                            onValueChange = { room = it },
                            label = { Text("Classroom (Optional)") }
                        )

                        Text(
                            text = "Start Time",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            TimeDropdown(
                                label = "HH",
                                options = (1..12).map { it.toString().padStart(2,'0') },
                                selected = startHour,
                                onSelect = {
                                    startHour = it

                                    if (startMinute.isBlank()) startMinute = "00"
                                    if (startAmPm.isBlank()) startAmPm = "AM"
                                }
                            )

                            TimeDropdown(
                                label = "MM",
                                options = (0..59).map { it.toString().padStart(2,'0') },
                                selected = startMinute,
                                onSelect = { startMinute = it }
                            )

                            TimeDropdown(
                                label = "AM/PM",
                                options = listOf("AM","PM"),
                                selected = startAmPm,
                                onSelect = { startAmPm = it }
                            )
                        }

                        Text(
                            text = "End Time",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            TimeDropdown(
                                label = "HH",
                                options = (1..12).map { it.toString().padStart(2,'0') },
                                selected = endHour,
                                onSelect = {
                                    endHour = it

                                    if (endMinute.isBlank()) endMinute = "00"
                                    if (endAmPm.isBlank()) endAmPm = "AM"
                                }
                            )

                            TimeDropdown(
                                label = "MM",
                                options = (0..59).map { it.toString().padStart(2,'0') },
                                selected = endMinute,
                                onSelect = { endMinute = it }
                            )

                            TimeDropdown(
                                label = "AM/PM",
                                options = listOf("AM","PM"),
                                selected = endAmPm,
                                onSelect = { endAmPm = it }
                            )
                        }
                    }
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            if (subject.isNotBlank()) {

                                val start24 =
                                    if (startHour.isNotBlank() && startMinute.isNotBlank() && startAmPm.isNotBlank())
                                        convertTo24Hour(startHour, startMinute, startAmPm)
                                    else ""

                                val end24 =
                                    if (endHour.isNotBlank() && endMinute.isNotBlank() && endAmPm.isNotBlank())
                                        convertTo24Hour(endHour, endMinute, endAmPm)
                                    else ""

                                val newClass = CustomClass(
                                    subject = subject,
                                    room = room.ifBlank { null },
                                    startTime = start24,
                                    endTime = end24,
                                    day = days[selectedDay]
                                )

                                customClasses =
                                    if (editingClass != null) {

                                        customClasses.map {
                                            if (it == editingClass) newClass else it
                                        }

                                    } else {

                                        customClasses + newClass
                                    }

                                editingClass = null

                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = { showAddDialog = false }
                    ) {
                        Text("Cancel")
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
    editMode: Boolean = false,
    allowMenu: Boolean = false,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
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

    val rotation by rememberInfiniteTransition(label = "shake").animateFloat(
        initialValue = -0.8f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(90),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeRotation"
    )

    val shakeRotation = if (editMode) rotation else 0f

    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationZ = shakeRotation
            },
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                verticalArrangement = if (item.room == "-")
                    Arrangement.Center
                else
                    Arrangement.Top
            ) {

                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                )

                if (item.room != "-") {

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.room,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (formattedStart != "-" || formattedEnd != "-") {

                    Column(horizontalAlignment = Alignment.End) {

                        if (formattedStart != "-") {
                            Text(
                                text = formattedStart,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (formattedEnd != "-") {
                            Text(
                                text = formattedEnd,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (editMode && allowMenu) {

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .heightIn(min = 36.dp)
                            .wrapContentWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { showMenu = true }
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {

                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
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

fun convertTo24Hour(hour: String, minute: String, ampm: String): String {

    var h = hour.toInt()

    if (ampm == "PM" && h != 12) h += 12
    if (ampm == "AM" && h == 12) h = 0

    return "${h.toString().padStart(2,'0')}:$minute"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Surface(
                modifier = Modifier
                    .menuAnchor()
                    .width(72.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                onClick = { expanded = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = selected,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            options.forEach { option ->

                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}