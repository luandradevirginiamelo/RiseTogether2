package com.example.risetogether.Patient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DisplayNotificationRejectScreen(
    requestData: Map<String, Any>,
    onClose: () -> Unit
) {
    val patientName = requestData["CancerPatientFullName"] ?: "Patient"
    val taskName = requestData["AssistTaskName"] ?: "N/A"
    val weekStart = requestData["WeekCommenceDate"] ?: "N/A"
    val dayRequested = requestData["SelectedDayRequested"] ?: "Requested Day"
    val status = requestData["Status"] ?: "Rejected"

    val volunteerName = requestData["VolunteerFullName"] ?: "Volunteer"
    val volunteerEmail = requestData["VolunteerEmail"] ?: "-"
    val volunteerMobile = requestData["VolunteerMobile"] ?: "-"

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Notification of Task Rejection") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Dear $patientName,", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your request for assistance with task \"$taskName\" on the requested day ($dayRequested) " +
                            "for Week Commencing $weekStart has regretfully been $status " +
                            "for availability reasons on that week and day. Kindly, send request for another week.",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Sincerely,", fontSize = 14.sp)
                Text(volunteerName.toString(), fontSize = 14.sp)
                Text("Email: $volunteerEmail", fontSize = 14.sp)
                Text("Mobile No: $volunteerMobile", fontSize = 14.sp)
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Close")
            }
        }
    )
}
