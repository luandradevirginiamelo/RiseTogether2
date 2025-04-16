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

    val timeRequested = requestData["Day 1 AM"] as? String?
        ?: requestData["Day 1 PM"] as? String?
        ?: requestData["Day 2 AM"] as? String?
        ?: requestData["Day 2 PM"] as? String?
        ?: "N/A"

    val status = "Accepted"

    val volunteerId = requestData["VolunteerID"] as? String
    val patientId = requestData["CancerPatientID"] as? String
    var patientFcmToken by remember { mutableStateOf<String?>(null) }

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

                    // 👇 Token of the patient
                    patientFcmToken = doc.getString("fcmToken")
                }
        }
    }


    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Send Notification of Acceptance of Assistance Request") },
        text = {
            if (patientInfo == null || volunteerInfo == null) {
                CircularProgressIndicator()
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Dear ${patientInfo?.get("name")},", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Your request for assistance with task \"$taskName\" on the requested day ($preferredDay), " +
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
                val reminderMessage = "📅 Just a reminder! You have a task tomorrow: \"$taskName\" on $preferredDay at $timeRequested."

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
                    "Your request for assistance with task \"$taskName\" on the requested day ($preferredDay), for the week commencing $weekStart has been $status."

                val notificDate = LocalDate.now().toString()
                val notificTime = LocalTime.now().toString()
                val reminderSendDate = LocalDate.now().toString()

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

                        // 🔔 Buscar token do paciente correto e enviar push
                        db.collection("PatientinTreatment")
                            .document(patientId)
                            .get()
                            .addOnSuccessListener { document ->
                                val token = document.getString("fcmToken")
                                if (!token.isNullOrEmpty()) {
                                    FcmV1Sender.sendNotification(
                                        context = context,
                                        fcmToken = token,
                                        title = "Task Accepted!",
                                        body = notificText
                                    )
                                }
                            }


                        // ✅ Enviar notificação push real via FCM V1
                        if (!patientFcmToken.isNullOrEmpty()) {
                            FcmV1Sender.sendNotification(
                                context = context,
                                fcmToken = patientFcmToken!!,
                                title = "Accepted!",
                                body = notificText
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "FCM Token not founded. Notifification not Send",
                                Toast.LENGTH_LONG
                            ).show()
                        }

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
                        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy")
                        val weekdays = mapOf(
                            "monday" to 0,
                            "tuesday" to 1,
                            "wednesday" to 2,
                            "thursday" to 3,
                            "friday" to 4
                        )

                        val weekStartDate = LocalDate.parse(weekStart.toString(), formatter)
                        val selectedDayIndex = weekdays[preferredDay.lowercase()]
                        val taskDate = selectedDayIndex?.let { weekStartDate.plusDays(it.toLong()) }
                        val tomorrow = LocalDate.now().plusDays(1)

                       // always reminder
                        val reminderData = hashMapOf(
                            "NotificationOfPatientRequestID" to notificationID,
                            "PatRequestFormID" to patRequestFormID,
                            "PatID" to patientId,
                            "AssistTaskCatID" to assistTaskCatID,
                            "AssistTaskName" to taskName,
                            "RequestedDay" to preferredDay,
                            "RequestedTime" to timeRequested,
                            "ReminderSendDate" to LocalDate.now().toString(),
                            "ReminderSendTime" to "10:00",
                            "isTestReminder" to true,
                            "CustomSendTime" to LocalTime.now().plusMinutes(1).toString()
                        )

                        db.collection("AcceptedTaskRequestReminder")
                            .add(reminderData)
                            .addOnSuccessListener {
                                // ✅ Salvar no painel do paciente
                                db.collection("PatientinTreatment")
                                    .document(patientId!!)
                                    .collection("Notifications")
                                    .add(
                                        mapOf(
                                            "text" to reminderMessage,
                                            "date" to LocalDate.now().toString(),
                                            "time" to LocalTime.now().toString()
                                        )
                                    )

                                // ✅ ENVIAR PUSH se a task for pra amanhã
                                if (taskDate == tomorrow && !patientFcmToken.isNullOrEmpty()) {
                                    android.os.Handler().postDelayed({
                                        FcmV1Sender.sendNotification(
                                            context = context,
                                            fcmToken = patientFcmToken!!,
                                            title = "Task Reminder",
                                            body = reminderMessage
                                        )
                                    }, 60000) // 60000ms = 1 minuto
                                }

                                Toast.makeText(
                                    context,
                                    "Notification for request acceptance and reminder created.",
                                    Toast.LENGTH_LONG
                                ).show()


                                // Notific
                                db.collection("PatientinTreatment")
                                    .document(patientId!!)
                                    .collection("Notifications")
                                    .add(
                                        mapOf(
                                            "text" to reminderMessage,
                                            "date" to LocalDate.now().toString(),
                                            "time" to LocalTime.now().toString()
                                        )
                                    )

                                // ✅ Feedback message
                                Toast.makeText(
                                    context,
                                    "Notification for request acceptance and reminder successfully created & sent.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // ✅ CLOSE POPS
                                onClose()
                                onNotificationSent()
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
