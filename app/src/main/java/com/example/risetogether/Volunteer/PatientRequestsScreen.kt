package com.example.risetogether.Volunteer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PatientRequestsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatientRequestsScreenContent()
        }
    }
}

@Composable
fun PatientRequestsScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 Title
        Text(text = "Patient Requests Screen", fontSize = 22.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 Placeholder for future content
        Text(text = "Here you will see patient requests for assistance.", fontSize = 16.sp)
    }
}
