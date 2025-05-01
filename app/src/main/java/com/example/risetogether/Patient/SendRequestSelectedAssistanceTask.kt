package com.example.risetogether.Patient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date    // ✅ CERTO!
import java.util.Locale // ✅ CERTO
import java.text.SimpleDateFormat


// --- Data Classes ---
data class Patient(
    val name: String = "",
    val surname: String = "",
    val mobile: String = "",
    val email: String = "",
    val id: String = "",
    val cancertype: String = ""
)

data class TaskData(
    val volunteerName: String = "",
    val volunteerSurname: String = "",
    val taskName: String = "",
    val weekCommenceDate: String = "",
    val preferredDay1: String? = null,
    val preferredDay2: String? = null,
    val preferredDay1AM: Boolean = false,
    val preferredDay1PM: Boolean = false,
    val preferredDay2AM: Boolean = false,
    val preferredDay2PM: Boolean = false,
    val volunteerId: String = "",
    val taskId: String = "",
    val categoryId: String = "",
    val volunteerProfileScheduleId: String = "",
    val day1Time: String = "",
    val day2Time: String = "",
    val volunteerProfileId: String = ""

)

class SendRequestSelectedAssistanceTask : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Dados que vêm da tela anterior
        val taskId = intent.getStringExtra("taskId") ?: ""
        val volunteerId = intent.getStringExtra("volunteerId") ?: ""
        val weekCommenceDate = intent.getStringExtra("weekCommenceDate") ?: ""
        val preferredDay1 = intent.getStringExtra("day1")
        val preferredDay2 = intent.getStringExtra("day2")



        // ✅ Aqui está o que você pediu (ajustado)
        val day1Time = intent.getStringExtra("day1Time") ?: "NOT_RECEIVED"
        val day2Time = intent.getStringExtra("day2Time") ?: "NOT_RECEIVED"
        Log.d("RECEIVED_TIME", "Day1Time: $day1Time | Day2Time: $day2Time")

        Log.d("RECEIVED_TIME", "DAY1 = ${intent.getStringExtra("day1")} | TIME=$day1Time | AM=${intent.getBooleanExtra("day1AM", false)} | PM=${intent.getBooleanExtra("day1PM", false)}")
        Log.d("RECEIVED_TIME", "DAY2 = ${intent.getStringExtra("day2")} | TIME=$day2Time | AM=${intent.getBooleanExtra("day2AM", false)} | PM=${intent.getBooleanExtra("day2PM", false)}")


        val volunteerName = intent.getStringExtra("volunteerName") ?: ""
        val volunteerSurname = intent.getStringExtra("volunteerSurname") ?: ""
        val assistTaskName = intent.getStringExtra("assistTaskName") ?: ""
        val volunteerProfileId = intent.getStringExtra("volunteerProfileID") ?: ""
        val volunteerProfileScheduleId = intent.getStringExtra("volunteerProfileScheduleID") ?: ""
        val categoryId = intent.getStringExtra("selectedTaskCatID") ?: ""

        val preferredDay1AM = intent.getBooleanExtra("day1AM", false)
        val preferredDay1PM = intent.getBooleanExtra("day1PM", false)
        val preferredDay2AM = intent.getBooleanExtra("day2AM", false)
        val preferredDay2PM = intent.getBooleanExtra("day2PM", false)

        setContent {
            var patient by remember { mutableStateOf<Patient?>(null) }
            var taskData by remember { mutableStateOf<TaskData?>(null) }
            var showDialog by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val db = Firebase.firestore

                if (userId != null) {
                    db.collection("PatientinTreatment").document(userId).get()
                        .addOnSuccessListener { doc ->
                            val p = Patient(
                                name = doc.getString("CancerPatientName") ?: "",
                                surname = doc.getString("CancerPatientSurname") ?: "",
                                mobile = doc.getString("PatientMobileNo") ?: "",
                                email = doc.getString("PatientUsernameEmail") ?: "",
                                cancertype = doc.getString("PatCancerType") ?: "",
                                id = userId
                            )
                            patient = p

                            // Finding results from Volunteer selected
                            db.collection("VolunteeringSurvivorAssistTaskCategoryPersonalProfileCalendarSchedule")
                                .document(taskId).get()
                                .addOnSuccessListener { taskDoc ->

                                   db.collection("VolunteeringCancerSurvivor")
                                        .document(volunteerId).get()
                                        .addOnSuccessListener { volDoc ->

                                            val task = TaskData(
                                                volunteerName = volunteerName,
                                                volunteerSurname = volunteerSurname,
                                                taskName = assistTaskName,
                                                weekCommenceDate = weekCommenceDate,
                                                preferredDay1 = preferredDay1,
                                                preferredDay2 = preferredDay2,
                                                preferredDay1AM = preferredDay1AM,
                                                preferredDay1PM = preferredDay1PM,
                                                preferredDay2AM = preferredDay2AM,
                                                preferredDay2PM = preferredDay2PM,
                                                volunteerId = volunteerId,
                                                taskId = taskId,
                                                categoryId = categoryId,
                                                volunteerProfileScheduleId = volunteerProfileScheduleId,
                                                volunteerProfileId = volunteerProfileId,
                                                day1Time = day1Time,
                                                day2Time = day2Time
                                            )


                                            taskData = task
                                        }
                                }
                        }
                }
            }

            if (patient != null && taskData != null) {
                SendRequestUI(
                    patient = patient!!,
                    taskData = taskData!!,
                    onSendClick = {
                        sendRequestToFirestore(patient!!, taskData!!) {
                            showDialog = true
                        }
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Request Sent") },
                    text = { Text("\"Request For Required Assistance Successfully created and Sent.\n" +
                            "Select either button Back  to View List of Volunteers or button Back to Dashboard.\"") },
                    confirmButton = {
                        Button(onClick = {
                            showDialog = false
                            finish()
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }

    private fun sendRequestToFirestore(
        patient: Patient,
        taskData: TaskData,
        onSuccess: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val requestId = FirebaseFirestore.getInstance()
            .collection("CancerPatientRequestFormRequiredAssistTask")
            .document().id

        // Time and Date Right Place
        val currentDateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTimeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val patRequestText = """
            Dear, ${taskData.volunteerName} ${taskData.volunteerSurname}.
            
            I’m sending you here my request for assistance with the task "${taskData.taskName}" 
            for the week that commences on ${taskData.weekCommenceDate}.
            
            My preferred day from your profile schedule is ${taskData.preferredDay1 ?: taskData.preferredDay2 ?: "N/A"}.
            My preferred Time for that Day is ${
            if (taskData.preferredDay1 != null) {
                when {
                    taskData.preferredDay1AM -> "AM"
                    taskData.preferredDay1PM -> "PM"
                    else -> "N/A"
                }
            } else {
                when {
                    taskData.preferredDay2AM -> "AM"
                    taskData.preferredDay2PM -> "PM"
                    else -> "N/A"
                }
            }
        }.
            
            Sincerely, ${patient.name} ${patient.surname}
            Email: ${patient.email}
            Mobile Number: ${patient.mobile}
        """.trimIndent()


        val requestData = hashMapOf(
            "PatientRequestFormID" to requestId,
            "CancerPatientID" to patient.id,
            "CancerPatientName" to "${patient.name} ${patient.surname}",
            "AssistTaskCatID" to taskData.categoryId,
            "AssistTaskID" to taskData.taskId,
            "AssistTaskName" to taskData.taskName,
            "VolunteerID" to taskData.volunteerId,
            "VolunteerProfileID" to taskData.volunteerProfileId,
            "VolunteerCalendarProfileScheduleID" to taskData.volunteerProfileScheduleId, // 👈 este aqui!
            "WeekCommenceDate" to taskData.weekCommenceDate,
            "Day1 Availability" to taskData.preferredDay1
                ?.removePrefix("Day 1 of Week: ")
                ?.takeIf { it.isNotBlank() },
            "Day 1 AM" to if (taskData.preferredDay1AM) taskData.day1Time else null,
            "Day 1 PM" to if (taskData.preferredDay1PM) taskData.day1Time else null,
            "Day2 Availability" to taskData.preferredDay2
                ?.removePrefix("Day 2 of Week: ")
                ?.takeIf { it.isNotBlank() },
            "Day 2 AM" to if (taskData.preferredDay2AM) taskData.day2Time else null,
            "Day 2 PM" to if (taskData.preferredDay2PM) taskData.day2Time else null,
            "RequestDate" to currentDateFormatted,
            "RequestTime" to currentTimeFormatted,
            "RequestStatus" to "Created",
            "RequestText" to patRequestText
        )

        db.collection("CancerPatientRequestFormRequiredAssistTask")
            .document(requestId)
            .set(requestData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error creating request: ", e)
            }
    }
}

// --- UI Compose dentro do mesmo arquivo ---
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
                .verticalScroll(rememberScrollState())
        ) {
            // Top gradient title bar
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
                    text = "Send Request for Selected Assistance Task",
                    color = Color.White,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Patient Info
            PatientReadOnlyField("Name", patient.name)
            PatientReadOnlyField("Surname", patient.surname)
            PatientReadOnlyField("Mobile No", patient.mobile)
            PatientReadOnlyField("Email Address", patient.email)

            Spacer(modifier = Modifier.height(8.dp))

            val preferredDay1 = taskData.preferredDay1 ?: ""
            val preferredDay2 = taskData.preferredDay2 ?: ""

            val fullDay1 = if (preferredDay1.isNotBlank() && preferredDay1 != "Day 1 of Week: ") {
                "$preferredDay1 at ${taskData.day1Time}"
            } else ""

            val fullDay2 = if (preferredDay2.isNotBlank() && preferredDay2 != "Day 2 of Week: ") {
                "$preferredDay2 at ${taskData.day2Time}"
            } else ""


            val message = """
    Dear, ${taskData.volunteerName} ${taskData.volunteerSurname}.
    
    I am sending you here my request for Assistance in the Task "${taskData.taskName}" 
    for the week that commences on ${taskData.weekCommenceDate}.
    
    My preferred availability from your profile schedule:
    ${if (fullDay1.isNotEmpty()) "- $fullDay1" else ""}
    ${if (fullDay2.isNotEmpty()) "- $fullDay2" else ""}
    
    Sincerely, ${patient.name} ${patient.surname}
    Email: ${patient.email}
    Phone: ${patient.mobile}
""".trimIndent()

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
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
                Text("Send", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientReadOnlyField(label: String, value: String) {
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
