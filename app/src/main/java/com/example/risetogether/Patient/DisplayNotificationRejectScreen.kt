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
    val patientFullName = requestData["PatNam+Surnam"] ?: "Patient"
    val assistTaskName = requestData["AssistTaskName"] ?: "N/A"
    val weekCommenceDate = requestData["WeekCommenceDate"] ?: "N/A"
    val requestedDay = requestData["RequestedDay"] ?: "N/A"
    val status = requestData["RequestStatus"] ?: "Rejected"

    val volunteerFullName = requestData["VoluntNam+Surnam"] ?: "Volunteer"
    val volunteerEmail = requestData["VoluntEmail"] ?: "-"
    val volunteerMobile = requestData["VoluntMobile"] ?: "-"

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text("Notification of Rejection of Requested Task Assistance")
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Text(
                    text = "Dear $patientFullName,",
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your request for assistance with task \"$assistTaskName\" on the requested day ($requestedDay) for Week Commencing ($weekCommenceDate) has regrettably been $status for availability reasons on that week and day. Kindly, send request for another Week.",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Sincerely, $volunteerFullName", fontSize = 14.sp)
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