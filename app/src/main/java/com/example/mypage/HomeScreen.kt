package com.example.mypage

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val routineData = mapOf(

        "Mon" to listOf(
            RoutineItem("Operating Systems", "08:00", "09:00", "C25-B-117"),
            RoutineItem("Computer Organization (COA)", "09:00", "10:00", "C25-B-117"),
            RoutineItem("OOPJ", "10:00", "11:00", "—"),
            RoutineItem("DBMS", "11:00", "12:00", "C25-B-115")
        ),

        "Tue" to listOf(
            RoutineItem("Discrete Mathematics (DM)", "10:00", "11:00", "C25-B-213"),
            RoutineItem("Operating Systems", "11:00", "12:00", "C25-B-206")
        ),

        "Wed" to listOf(
            RoutineItem("Operating Systems", "10:00", "11:00", "C25-B-217"),
            RoutineItem("Discrete Mathematics (DM)", "11:00", "12:00", "—"),
            RoutineItem("OOPJ", "12:00", "01:00", "C25-B-217")
        ),

        "Thu" to listOf(
            RoutineItem("OS Lab", "10:00", "12:00", "C25-B-018(L)"),
            RoutineItem("Discrete Mathematics (DM)", "12:00", "01:00", "C25-B-120")
        ),

        "Fri" to listOf(
            RoutineItem("DBMS", "08:00", "09:00", "C25-A-206"),
            RoutineItem("OOPJ Lab", "09:00", "11:00", "C25-B-102(L)"),
            RoutineItem("OOPJ", "11:00", "12:00", "C25-B-113")
        )
    )

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    var selectedDay by remember { mutableStateOf(0) }

    val selectedDayName = days[selectedDay]
    val todayClasses = routineData[selectedDayName] ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyPage") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)

        ) {

            ScrollableTabRow(selectedTabIndex = selectedDay) {
                days.forEachIndexed { index, day ->
                    Tab(
                        selected = selectedDay == index,
                        onClick = { selectedDay = index },
                        text = { Text(day) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (todayClasses.isEmpty()) {
                Text(
                    text = "No Classes",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

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