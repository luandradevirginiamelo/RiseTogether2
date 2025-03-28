package com.example.risetogether.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.Patient.Patient
import com.example.risetogether.Patient.TaskData

@Composable
fun SendRequestUI(
    patient: Patient,
    taskData: TaskData,
    onSendClick: () -> Unit
) {
    val BlueMedium = Color(0xFF007AFF)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // ✅ Scroll aqua
        ) {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFFB3E5FC), BlueMedium))
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Review & Confirm Request",
                    color = Color.White,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Fields
            ReadOnlyField("Patient Name", "${patient.name} ${patient.surname}")
            ReadOnlyField("Email", patient.email)
            ReadOnlyField("Mobile", patient.mobile)
            ReadOnlyField("Cancer Type", patient.cancertype)

            Spacer(modifier = Modifier.height(8.dp))

            ReadOnlyField("Volunteer", taskData.volunteerName)
            ReadOnlyField("Task", taskData.taskName)
            ReadOnlyField("Week Commencing", taskData.weekCommenceDate)
            ReadOnlyField("Preferred Day", taskData.preferredDay)
            ReadOnlyField("Preferred Time", taskData.preferredTime)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = """
                    Dear ${taskData.volunteerName} ${patient.surname},

                    I am sending you my request for assistance in the task "${taskData.taskName}"
                    for the week commencing on ${taskData.weekCommenceDate}.

                    My preferred day is ${taskData.preferredDay},
                    and my preferred time is ${taskData.preferredTime}.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp),
                softWrap = true // ✅ Garante quebra de linha
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSendClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BlueMedium)
            ) {
                Text("Send Request", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadOnlyField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        enabled = false,
        readOnly = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color.LightGray,
            disabledLabelColor = Color.Gray,
            disabledPlaceholderColor = Color.Gray
        )
    )
}
