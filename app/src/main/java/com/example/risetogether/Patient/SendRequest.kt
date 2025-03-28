package com.example.risetogether.Patient

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendRequest(
    patient: Patient,
    taskData: TaskData,
    onSendClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Window 15B: Send Request for Selected Assistance Task") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // Patient Info Fields
            OutlinedTextField(value = patient.name, onValueChange = {}, label = { Text("Name") }, readOnly = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = patient.surname, onValueChange = {}, label = { Text("Surname") }, readOnly = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = patient.mobile, onValueChange = {}, label = { Text("Mobile No") }, readOnly = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = patient.email, onValueChange = {}, label = { Text("Email Address") }, readOnly = true, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))

            val message = """
                Dear ${taskData.volunteerName} ${patient.surname},
                
                I am sending you here my request for assistance in the task "${taskData.taskName}"
                for the week that commences on ${taskData.weekCommenceDate}.
                
                My preferred day from your profile schedule is ${taskData.preferredDay}.
                My preferred time for that day is ${taskData.preferredTime}.
            """.trimIndent()

            Text(text = message, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onSendClick, modifier = Modifier.fillMaxWidth()) {
                Text("Send")
            }
        }
    }
}
