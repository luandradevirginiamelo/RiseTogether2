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
import com.example.risetogether.FcmV1Sender
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun NotificationRejectScreen(
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

    val preferredDay = requestData["RequestedDay"] as? String?
        ?: requestData["SelectedDayRequested"] as? String?
        ?: requestData["Day1 Availability"] as? String?
        ?: requestData["Day2 Availability"] as? String?
        ?: "N/A"

    val timeRequested = requestData["Day 1 AM"] as? String?
        ?: requestData["Day 1 PM"] as? String?
        ?: requestData["Day 2 AM"] as? String?
        ?: requestData["Day 2 PM"] as? String?
        ?: "N/A"

    val status = "Rejected"
    val volunteerId = requestData["VolunteerID"] as? String
    val patientId = requestData["CancerPatientID"] as? String
    var patientFcmToken by remember { mutableStateOf<String?>(null) }

    // Load info
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
                    patientFcmToken = doc.getString("fcmToken")
                }
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Send Notification of Rejection of Request") },
        text = {
            if (patientInfo == null || volunteerInfo == null) {
                CircularProgressIndicator()
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(8.dp)) {
                    Text("Dear ${patientInfo?.get("name")},", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your request for assistance with task \"$taskName\" on the requested day: ($preferredDay) for Week Commencing $weekStart has been $status due to availability reasons on that week or day. You can send a request for another week.",
                        fontSize = 14.sp
                    )
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
                val notificText =
                    "Your request for assistance with task \"$taskName\" on the requested day: ($preferredDay) for Week Commencing $weekStart has been $status due to availability reasons on that week or day. You can send a request for another week."

                val notificDate = LocalDate.now().toString()
                val notificTime = LocalTime.now().toString()
                val reminderSendDate = LocalDate.now().minusDays(1).toString()

                val notificationData = hashMapOf(
                    "PatRequestFormID" to (requestData["PatRequestFormID"] ?: ""),
                    "PatID" to patientId,
                    "AssistTaskCatID" to (requestData["AssistTaskCatID"] ?: ""),
                    "AssistTaskID" to (requestData["AssistTaskID"] ?: ""),
                    "AssistTaskName" to taskName,
                    "VolunteerID" to volunteerId,
                    "VolunteerProfileID" to (requestData["VolunteerProfileID"] ?: ""),
                    "VolunteerCalendarProfileScheduleID" to (requestData["VolunteerCalendarProfileScheduleID"] ?: ""),
                    "WeekCommenceDate" to weekStart,
                    "RequestedDay" to preferredDay,
                    "RequestTime" to timeRequested,
                    "RequestAccept" to "No",
                    "RequestStatus" to status,
                    "NotificTxt" to notificText,
                    "NotificDate" to notificDate,
                    "NotificTime" to notificTime
                )

                db.collection("NotificationOfPatientRequest")
                    .add(notificationData)
                    .addOnSuccessListener { notifRef ->
                        val notificationID = notifRef.id

                        // Enviar push
                        if (!patientFcmToken.isNullOrEmpty()) {
                            FcmV1Sender.sendNotification(
                                context = context,
                                fcmToken = patientFcmToken!!,
                                title = "Task Rejected",
                                body = notificText
                            )
                        }

                        // Mensagem na subcoleção do paciente
                        db.collection("PatientinTreatment")
                            .document(patientId ?: "")
                            .collection("Notifications")
                            .add(
                                mapOf(
                                    "text" to notificText,
                                    "date" to notificDate,
                                    "time" to notificTime
                                )
                            )

                        // Criar lembrete (opcional — como feito no Accept)
                        val reminderData = hashMapOf(
                            "NotificationOfPatientRequestID" to notificationID,
                            "PatRequestFormID" to (requestData["PatRequestFormID"] ?: ""),
                            "PatID" to patientId,
                            "AssistTaskCatID" to (requestData["AssistTaskCatID"] ?: ""),
                            "AssistTaskName" to taskName,
                            "RequestedDay" to preferredDay,
                            "RequestedTime" to timeRequested,
                            "ReminderSendDate" to reminderSendDate
                        )

                        db.collection("AcceptedTaskRequestReminder")
                            .add(reminderData)

                        Toast.makeText(
                            context,
                            "Notification for request rejection created and sent. Select to view another request from the list, or press 'Back to Dashboard'.",
                            Toast.LENGTH_LONG
                        ).show()

                        onClose()
                        onNotificationSent()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Failed to create rejection notification.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }) {
                Text("Send")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Cancel")
            }
        }
    )
}
