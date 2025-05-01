package com.example.risetogether.Patient

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.PatientLoginScreen
import com.example.risetogether.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProvideFeedbackRatingsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideFeedbackRatingsContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvideFeedbackRatingsContent() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val patientId = auth.currentUser?.uid ?: ""

    var selectedWeek by remember { mutableStateOf("") }
    var weekExpanded by remember { mutableStateOf(false) }
    var availableWeeks by remember { mutableStateOf(listOf<String>()) }
    var acceptedTasks by remember { mutableStateOf(listOf<Map<String, String>>()) }



    // Fetch available weeks
    LaunchedEffect(patientId) {
        db.collection("NotificationOfPatientRequest")
            .whereEqualTo("PatID", patientId)
            .whereEqualTo("RequestStatus", "Accepted") // use "RequestStatus" Firestore
            .get()
            .addOnSuccessListener { snapshot ->
                val weeks = snapshot.documents
                    .mapNotNull { it.getString("WeekCommenceDate") }
                    .distinct()

                availableWeeks = weeks.sorted()
            }
    }

    fun fetchAcceptedTasksForWeek(week: String) {
        db.collection("NotificationOfPatientRequest")
            .whereEqualTo("PatID", patientId)
            .whereEqualTo("WeekCommenceDate", week)
            .whereEqualTo("RequestStatus", "Accepted")
            .get()
            .addOnSuccessListener { documents ->
                val tasks = documents.map {
                    mapOf(
                        "name" to (it.getString("AssistTaskName") ?: "N/A"),
                        "day" to (it.getString("RequestedDay") ?: ""),      // <- CORRETO
                        "time" to (it.getString("RequestTime") ?: ""),      // <- CORRETO
                        "requestId" to it.id
                    )
                }
                acceptedTasks = tasks
            }
    }
    @Composable
    fun GradientButton(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = text, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5), Color(0xFFFFF8E1))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            GradientButton("Logout") {
                                FirebaseAuth.getInstance().signOut()
                                context.startActivity(
                                    Intent(context, PatientLoginScreen::class.java)
                                        .apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        }
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Provide Feedback and Ratings for Obtained Assistance",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A237E)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Week Commencing Date:", fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = weekExpanded,
                            onExpandedChange = { weekExpanded = !weekExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedWeek,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor(),
                                label = { Text("Week") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = weekExpanded)
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = weekExpanded,
                                onDismissRequest = { weekExpanded = false }
                            ) {

                                availableWeeks.forEach { week ->
                                    DropdownMenuItem(
                                        text = { Text(week) },
                                        onClick = {
                                            selectedWeek = week
                                            weekExpanded = false
                                            fetchAcceptedTasksForWeek(week)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Tasks performed for the selected week: ${acceptedTasks.size}", color = Color.Black)

                        // Tasks List
                        acceptedTasks.forEach { task ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F8E9), RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(" \uD83D\uDCCC Task Name: ${task["name"]}")
                                Text("Day of Week Requested: ${task["day"]}")
                                Text("Time Requested: ${task["time"]}")

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val intent = Intent(context, ProvideFeedbackRatingsDetailScreen::class.java)
                                        intent.putExtra("requestId", task["requestId"])
                                        context.startActivity(intent)
                                    },
                                    colors = buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                                ) {
                                    Text("Select")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                GradientButton("Select Another Week Commencing Date") {
                    selectedWeek = ""
                    acceptedTasks = emptyList()
                }

                Spacer(modifier = Modifier.height(12.dp))

                GradientButton("Back to Dashboard") {
                    context.startActivity(Intent(context, PatientDashboardScreen::class.java))
                }
            }
        }
    }
}
