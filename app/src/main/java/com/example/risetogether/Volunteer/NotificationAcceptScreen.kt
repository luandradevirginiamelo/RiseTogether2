package com.example.risetogether.Volunteer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationAcceptScreen(
    requestData: Map<String, Any>,
    onClose: () -> Unit
) {
    val patientFullName = requestData["CancerPatientFullName"] ?: "Patient"
    val taskName = requestData["AssistTaskName"] ?: "N/A"
    val weekStart = requestData["WeekCommenceDate"] ?: "N/A"

    val day1 = requestData["Day1 Availability"] as? String?
    val day2 = requestData["Day2 Availability"] as? String?
    val preferredDay = if (!day1.isNullOrEmpty()) day1 else day2 ?: "N/A"

    val status = "Accepted"

    val volunteerName = requestData["VolunteerFullName"] ?: "Volunteer"
    val volunteerEmail = requestData["VolunteerEmail"] ?: "-"
    val volunteerMobile = requestData["VolunteerMobile"] ?: "-"

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Send Notification of Acceptance of Request for Assistance") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Dear $patientFullName,", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Your request for assistance with the task \"$taskName\" on the requested day " +
                            "($preferredDay) (whichever is not null) \n" +
                            "For the week commencing $weekStart has been $status.",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Will see you then!", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Sincerely,", fontSize = 14.sp)
                Text(volunteerName.toString(), fontSize = 14.sp)
                Text("Email: $volunteerEmail", fontSize = 14.sp)
                Text("Mobile No: $volunteerMobile", fontSize = 14.sp)
            }
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text("Send")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Close")
            }
        }
    )
}


