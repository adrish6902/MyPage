package com.example.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StartupScreen(
    onKiitSelected: () -> Unit,
    onNonKiitSelected: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Are you a student of KIIT?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onKiitSelected) {
                Text("Yes")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onNonKiitSelected) {
                Text("No")
            }
        }
    }
}
