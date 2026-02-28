package com.example.mypage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val routineData = mapOf(

        "Mon" to listOf(
            RoutineItem("OS", "08:00", "09:00", "C25-B-117"),
            RoutineItem("COA", "09:00", "10:00", "C25-B-117"),
            RoutineItem("OOPJ", "10:00", "11:00", "C25-B-117"),
            RoutineItem("DBMS", "11:00", "12:00", "C25-B-115"),
            RoutineItem("IEC", "12:00", "01:00", "C25-B-115"),
            RoutineItem("DM", "01:00", "02:00", "C25-B-115")
        ),

        "Tue" to listOf(
            RoutineItem("DM", "10:00", "11:00", "C25-B-213"),
            RoutineItem("OS", "11:00", "12:00", "C25-B-206"),
            RoutineItem("DBMS(L)", "12:00", "02:00", "C25-B-212(l)"),
            RoutineItem("DBMS", "04:00", "05:00", "C25-A-301"),
            RoutineItem("IEC", "05:00", "06:00", "C25-A-301")
        ),

        "Wed" to listOf(
            RoutineItem("OS", "10:00", "11:00", "C25-B-217"),
            RoutineItem("DM", "11:00", "12:00", "C25-B-217"),
            RoutineItem("OOPJ", "12:00", "01:00", "C25-B-217"),
            RoutineItem("VT", "03:00", "05:00", "-"),
        ),

        "Thu" to listOf(
            RoutineItem("OS(L)", "9:00", "11:00", "C25-B-018(L)"),
            RoutineItem("DM", "11:00", "12:00", "C25-B-120"),
            RoutineItem("IEC", "12:00", "01:00", "C25-B-120"),
            RoutineItem("COA", "01:00", "02:00", "C25-B-120")
        ),

        "Fri" to listOf(
            RoutineItem("DBMS", "08:00", "09:00", "C25-A-206"),
            RoutineItem("OOPJ(L)", "09:00", "11:00", "C25-B-102(L)"),
            RoutineItem("OOPJ", "11:00", "12:00", "C25-B-113"),
            RoutineItem("DM", "12:00", "01:00", "C25-B-113")
        )
    )

    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val today = LocalDate.now().dayOfWeek
    val initialIndex = when (today) {
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

    Scaffold{ padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)

        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "MyPage",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )

//                    IconButton(onClick = { }) {
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = "User"
//                        )
//                    }
                }
            }

            HorizontalDivider(
                thickness = 1.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEachIndexed { index, day ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        ) {
                            DayCircle(
                                text = day,
                                isSelected = selectedDay == index,
                                onClick = { selectedDay = index }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (todayClasses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Classes Today",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(todayClasses) { item ->
                        RoutineCard(item)
                    }
                }
            }

        }
    }
}
@Composable
fun RoutineCard(item: RoutineItem) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
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
                    text = item.startTime,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = item.endTime,
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
                text = text.take(1), // M T W T F
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}