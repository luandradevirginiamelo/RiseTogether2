package com.example.risetogether.Patient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProvideFeedbackRatingsDetailScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestId = intent.getStringExtra("requestId")
        setContent {
            if (requestId != null) {
                ProvideFeedbackRatingsDetailContent(requestId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvideFeedbackRatingsDetailContent(requestId: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val patientId = auth.currentUser?.uid ?: ""

    var taskData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var volunteerName by remember { mutableStateOf("") }
    var patientName by remember { mutableStateOf("") }

    var rating by remember { mutableStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    // Buscar os dados da notificação + voluntário + paciente
    LaunchedEffect(requestId) {
        db.collection("NotificationOfPatientRequest").document(requestId).get()
            .addOnSuccessListener { doc ->
                val task = mutableMapOf<String, String>()
                val week = doc.getString("WeekCommenceDate") ?: ""
                val taskName = doc.getString("AssistTaskName") ?: ""
                val day = doc.getString("RequestedDay") ?: ""
                val time = doc.getString("RequestTime") ?: ""
                val volunteerId = doc.getString("VolunteerID") ?: ""

                task["WeekCommenceDate"] = week
                task["AssistTaskName"] = taskName
                task["DayRequested"] = day
                task["TimeRequested"] = time
                task["VolunteerID"] = volunteerId
                task["PatientID"] = doc.getString("PatID") ?: ""

                taskData = task

                // Buscar nome do voluntário
                db.collection("VolunteeringCancerSurvivor").document(volunteerId).get()
                    .addOnSuccessListener {
                        volunteerName = "${it.getString("VolunteerName") ?: ""} ${it.getString("VolunteerSurname") ?: ""}"
                    }

                // Buscar nome do paciente
                db.collection("PatientinTreatment").document(patientId).get()
                    .addOnSuccessListener {
                        patientName = "${it.getString("CancerPatientName") ?: ""} ${it.getString("CancerPatientSurname") ?: ""}"
                    }
            }
    }

    fun submitFeedback() {
        if (rating in 1..5 && feedbackText.isNotBlank()) {
            val feedback = hashMapOf(
                "WeekCommenceDate" to taskData["WeekCommenceDate"],
                "PatientID" to taskData["PatientID"],
                "PatientName" to patientName,
                "VolunteerID" to taskData["VolunteerID"],
                "VolunteerName" to volunteerName,
                "AssistTaskName" to taskData["AssistTaskName"],
                "DayProvided" to taskData["DayRequested"],
                "TimeProvided" to taskData["TimeRequested"],
                "RatingStars" to rating,
                "FeedbackText" to feedbackText,
                "FeedbackDateTime" to Timestamp.now()
            )

            db.collection("FeedbackRatings").add(feedback)
                .addOnSuccessListener {
                    Toast.makeText(context, "Feedback sent successfully!", Toast.LENGTH_LONG).show()
                    context.startActivity(Intent(context, PatientDashboardScreen::class.java))
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error when send feedback.", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(context, "Provide Feedback and Ratings for Obteined Assistance with Selected Task !", Toast.LENGTH_LONG).show()
        }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Provide Feedback Text", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE3F2FD), Color.White)
                    )
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Week Commencing Date: ${taskData["WeekCommenceDate"] ?: ""}")
            Text("Assistance Task Name: ${taskData["AssistTaskName"] ?: ""}")
            Text("Day Provided: ${taskData["DayRequested"]}")
            Text("Time Provided: ${taskData["TimeRequested"]}")
            Text("Volunteer's Name: $volunteerName")
            Text("Patient's Name: $patientName")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Provide No. of Rate Stars (1-5):", fontWeight = FontWeight.SemiBold)
            Row {
                (1..5).forEach { star ->
                    IconToggleButton(
                        checked = star <= rating,
                        onCheckedChange = {
                            rating = star
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $star",
                            tint = if (star <= rating) Color.Yellow else Color.Gray
                        )
                    }
                }
            }

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Provide Feedback and Ratings for Obteined Assistance with Selected Task ") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { submitFeedback() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Submit", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}


