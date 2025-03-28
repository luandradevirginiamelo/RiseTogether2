package com.example.risetogether.Patient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.risetogether.ui.theme.SendRequestUI


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
    val taskName: String = "",
    val weekCommenceDate: String = "",
    val preferredDay: String = "",
    val preferredTime: String = "",
    val volunteerId: String = "",
    val taskId: String = "",
    val categoryId: String = "",
    val volunteerProfileScheduleId: String = ""
)

class ReviewConfirmRequestScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve intent extras
        val taskId = intent.getStringExtra("taskId") ?: ""
        val volunteerId = intent.getStringExtra("volunteerId") ?: ""
        val weekCommenceDate = intent.getStringExtra("weekCommenceDate") ?: ""
        val preferredDay = intent.getStringExtra("preferredDay") ?: ""
        val preferredTime = intent.getStringExtra("preferredTime") ?: ""

        setContent {
            var patient by remember { mutableStateOf<Patient?>(null) }
            var taskData by remember { mutableStateOf<TaskData?>(null) }
            var showDialog by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val db = Firebase.firestore

                if (userId != null) {
                    // Fetch patient data
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

                            // Fetch task data
                            db.collection("VolunteeringSurvivorAssistTaskCategoryPersonalProfileCalendarSchedule")
                                .document(taskId).get()
                                .addOnSuccessListener { taskDoc ->
                                    val task = TaskData(
                                        volunteerName = "", // to be filled next
                                        taskName = taskDoc.getString("taskName") ?: "Unknown Task",
                                        weekCommenceDate = weekCommenceDate,
                                        preferredDay = preferredDay,
                                        preferredTime = preferredTime,
                                        volunteerId = volunteerId,
                                        taskId = taskId,
                                        categoryId = taskDoc.getString("assistTaskCategoryId") ?: "",
                                        volunteerProfileScheduleId = taskDoc.getString("volunteerProfileScheduleId") ?: ""
                                    )

                                    // Fetch volunteer name
                                    db.collection("VolunteeringCancerSurvivor").document(volunteerId).get()
                                        .addOnSuccessListener { volDoc ->
                                            val volunteerName = volDoc.getString("volunteerName") ?: "Volunteer"
                                            taskData = task.copy(volunteerName = volunteerName)
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
                    text = { Text("Request for Required Assistance successfully created & sent.") },
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
        val requestId =
            db.collection("CancerPatientRequestFormRequiredAssistTask").document().id

        val requestData = hashMapOf(
            "requestId" to requestId,
            "patientId" to patient.id,
            "patientName" to "${patient.name} ${patient.surname}",
            "assistTaskCategoryId" to taskData.categoryId,
            "assistTaskId" to taskData.taskId,
            "assistTaskName" to taskData.taskName,
            "volunteerId" to taskData.volunteerId,
            "volunteerName" to taskData.volunteerName,
            "volunteerProfileScheduleId" to taskData.volunteerProfileScheduleId,
            "weekCommenceDate" to taskData.weekCommenceDate,
            "preferredDay" to taskData.preferredDay,
            "preferredTime" to taskData.preferredTime,
            "requestStatus" to "Created",
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("CancerPatientRequestFormRequiredAssistTask")
            .document(requestId)
            .set(requestData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error creating request: ", e)
            }
    }
}
