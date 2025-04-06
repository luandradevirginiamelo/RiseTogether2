package com.example.risetogether.Volunteer

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun NotificationAcceptScreen(
    requestData: Map<String, Any>,
    onClose: () -> Unit,
    onNotificationSent: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var volunteerInfo by remember { mutableStateOf<Map<String, String>?>(null) }
    var patientInfo by remember { mutableStateOf<Map<String, String>?>(null) }

    val taskName = requestData["AssistTaskName"] ?: "N/A"
    val weekStart = requestData["WeekCommenceDate"] ?: "N/A"

    val day1 = requestData["Day1 Availability"] as? String?
    val day2 = requestData["Day2 Availability"] as? String?
    val preferredDay = if (!day1.isNullOrEmpty()) day1 else day2 ?: "N/A"

    val timeRequested = requestData["Selected AM Day 1"]
        ?: requestData["Selected PM Day 1"]
        ?: requestData["Selected AM Day 2"]
        ?: requestData["Selected PM Day 2"]
        ?: "N/A"

    val status = "Accepted"

    val volunteerId = requestData["VolunteerID"] as? String
    val patientId = requestData["CancerPatientID"] as? String

    // Load volunteer and patient info
    LaunchedEffect(requestData) {
        if (volunteerId != null) {
            db.collection("VolunteeringCancerSurvivor").document(volunteerId)
                .get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("VolunteerName") ?: ""
                    val surname = doc.getString("VolunteerSurname") ?: ""
                    val email = doc.getString("VolunteerUsername") ?: "-"
                    val phone = doc.getString("VolunMobile") ?: "-"
                    volunteerInfo = mapOf(
                        "name" to "$name $surname",
                        "email" to email,
                        "mobile" to phone
                    )
                }
        }

        if (patientId != null) {
            db.collection("PatientinTreatment").document(patientId)
                .get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("CancerPatientName") ?: ""
                    val surname = doc.getString("CancerPatientSurname") ?: ""
                    patientInfo = mapOf("name" to "$name $surname")
                }
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Send Notification of Assistance Request Acceptance") },
        text = {
            if (patientInfo == null || volunteerInfo == null) {
                CircularProgressIndicator()
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Dear ${patientInfo?.get("name")},", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Your request for assistance with the task \"$taskName\" on the requested day ($preferredDay), " +
                                "for the week commencing $weekStart has been $status.",
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Will see you then!", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Sincerely,", fontSize = 14.sp)
                    Text(volunteerInfo?.get("name") ?: "", fontSize = 14.sp)
                    Text("Email: ${volunteerInfo?.get("email")}", fontSize = 14.sp)
                    Text("Mobile: ${volunteerInfo?.get("mobile")}", fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // DEBUG: Print all requestData to help identify missing fields
                println("DEBUG: requestData = $requestData")

                // Extract required fields and validate
                val patRequestFormID = requestData["PatRequestFormID"] as? String
                val assistTaskCatID = requestData["AssistTaskCatID"] as? String
                val assistTaskID = requestData["AssistTaskID"] as? String
                val volunteerProfileID = requestData["VolunteerProfileID"] as? String
                val volunteerCalendarProfileScheduleID = requestData["VolunteerCalendarProfileScheduleID"] as? String

                if (
                    patRequestFormID == null || assistTaskCatID == null || assistTaskID == null ||
                    volunteerId == null || volunteerProfileID == null || volunteerCalendarProfileScheduleID == null || patientId == null
                ) {
                    Toast.makeText(
                        context,
                        "Missing required data to send notification. Please check the request.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                // Build notification
                val notificText =
                    "Your request for assistance with the task \"$taskName\" on the requested day ($preferredDay), for the week commencing $weekStart has been $status."

                val notificDate = LocalDate.now().toString()
                val notificTime = LocalTime.now().toString()
                val reminderSendDate = LocalDate.now().minusDays(1).toString()

                val notificationData = hashMapOf(
                    "PatRequestFormID" to patRequestFormID,
                    "PatID" to patientId,
                    "AssistTaskCatID" to assistTaskCatID,
                    "AssistTaskID" to assistTaskID,
                    "AssistTaskName" to taskName,
                    "VolunteerID" to volunteerId,
                    "VolunteerProfileID" to volunteerProfileID,
                    "VolunteerCalendarProfileScheduleID" to volunteerCalendarProfileScheduleID,
                    "WeekCommenceDate" to weekStart,
                    "RequestedDay" to preferredDay,
                    "RequestTime" to timeRequested,
                    "RequestAccept" to "Yes",
                    "NotificDate" to notificDate,
                    "NotificTime" to notificTime,
                    "RequestStatus" to status,
                    "NotificTxt" to notificText
                )

                // Save notification
                db.collection("NotificationOfPatientRequest")
                    .add(notificationData)
                    .addOnSuccessListener { notifRef ->
                        val notificationID = notifRef.id

                        // Add message to patient UI
                        db.collection("PatientinTreatment")
                            .document(patientId)
                            .collection("Notifications")
                            .add(
                                mapOf(
                                    "text" to notificText,
                                    "date" to notificDate,
                                    "time" to notificTime
                                )
                            )

                        // Create reminder
                        val reminderData = hashMapOf(
                            "NotificationOfPatientRequestID" to notificationID,
                            "PatRequestFormID" to patRequestFormID,
                            "PatID" to patientId,
                            "AssistTaskCatID" to assistTaskCatID,
                            "AssistTaskName" to taskName,
                            "RequestedDay" to preferredDay,
                            "RequestedTime" to timeRequested,
                            "ReminderSendDate" to reminderSendDate
                        )

                        db.collection("AcceptedTaskRequestReminder")
                            .add(reminderData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Notification for request acceptance successfully created & sent.",
                                    Toast.LENGTH_LONG
                                ).show()

                                onClose()            // Close popup
                                onNotificationSent() // Go back to screen 2.2B
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to create reminder!", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to create notification!", Toast.LENGTH_LONG).show()
                    }
            }) {
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
