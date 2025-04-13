package com.example.risetogether.Volunteer

import android.content.Intent
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.R
import com.example.risetogether.VolunteerLoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewPatientRequestsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewPatientRequestsContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewPatientRequestsContent() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    val volunteerId = auth.currentUser?.uid ?: ""

    var selectedWeek by remember { mutableStateOf("") }
    var weekExpanded by remember { mutableStateOf(false) }
    var availableWeeks by remember { mutableStateOf(listOf<String>()) }

    var latestRequest by remember { mutableStateOf<Map<String, String>?>(null) }
    var earliestRequest by remember { mutableStateOf<Map<String, String>?>(null) }

    var showPopup by remember { mutableStateOf(false) }
    var selectedRequestId by remember { mutableStateOf<String?>(null) }



    // Load weeks
    LaunchedEffect(volunteerId) {
        db.collection("CancerPatientRequestFormRequiredAssistTask")
            .whereEqualTo("VolunteerID", volunteerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val weeks = snapshot.documents.mapNotNull { it.getString("WeekCommenceDate") }.distinct()
                availableWeeks = weeks.sorted()
            }
    }

    fun fetchRequestsForWeek(week: String) {
        db.collection("CancerPatientRequestFormRequiredAssistTask")
            .whereEqualTo("VolunteerID", volunteerId)
            .whereEqualTo("WeekCommenceDate", week)
            .get()
            .addOnSuccessListener { documents ->
                val sorted = documents.sortedBy {
                    val date = it.getString("RequestDate") ?: ""
                    val time = it.getString("RequestTime") ?: ""
                    "$date $time"
                }

                earliestRequest = sorted.firstOrNull()?.let {
                    mapOf(
                        "name" to (it.getString("AssistTaskName") ?: "N/A"),
                        "date" to (it.getString("RequestDate") ?: ""),
                        "time" to (it.getString("RequestTime") ?: ""),
                        "requestId" to (it.getString("PatientRequestFormID") ?: "")
                    )
                }

                latestRequest = sorted.lastOrNull()?.let {
                    mapOf(
                        "name" to (it.getString("AssistTaskName") ?: "N/A"),
                        "date" to (it.getString("RequestDate") ?: ""),
                        "time" to (it.getString("RequestTime") ?: ""),
                        "requestId" to (it.getString("PatientRequestFormID") ?: "")
                    )
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF90CAF9),
                        Color(0xFF81C784),
                        Color(0xFFFFF8E1)
                    )
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
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(MaterialTheme.shapes.small)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                auth.signOut()
                                context.startActivity(Intent(context, VolunteerLoginScreen::class.java))
                            }) {
                                Text("Logout")
                            }
                        }
                    }
                )
                // ✅ INSIRA AQUI O POP-UP DENTRO DO BOX!
                if (showPopup && selectedRequestId != null) {
                    ViewPatientRequestPopup(
                        requestId = selectedRequestId!!,
                        onDismiss = {
                            showPopup = false
                            selectedRequestId = null
                        },
                        onAccept = {
                            // TODO: update Firestore status to "Accepted"
                            showPopup = false
                        },
                        onReject = {
                            // TODO: update Firestore status to "Rejected"
                            showPopup = false
                        }
                    )
                }

            }

        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text("View List of Patient’s Requests for Assistance", fontSize = 20.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Week Commence Date:", fontSize = 16.sp, color = Color.Black)

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
                                    fetchRequestsForWeek(week)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Text("(Latest Requested Assistance Task by: Requested Date and Time)", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("Task Name: ${latestRequest?.get("name") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                    Text("Notification Date: ${latestRequest?.get("date") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                    Text("Notification Time:  ${latestRequest?.get("time") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = {
                        selectedRequestId = latestRequest?.get("requestId")
                        showPopup = true
                    }) {
                        Text("View")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Text("(Earliest Requested Assistance Task by: Requested Date and Time)", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("Task Name: ${earliestRequest?.get("name") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                    Text("Notification Date: ${earliestRequest?.get("date") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                    Text("Notification Time: ${earliestRequest?.get("time") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = {
                        selectedRequestId = earliestRequest?.get("requestId")
                        showPopup = true
                    }) {
                        Text("View")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    selectedWeek = ""
                    latestRequest = null
                    earliestRequest = null
                }) {
                    Text("Select Another Week Commence Date")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(onClick = {
                    context.startActivity(Intent(context, VolunteerDashboardScreen::class.java))
                }) {
                    Text("Back to Dashboard")
                }
            }
        }

        // 🔥 Show popup
        if (showPopup && selectedRequestId != null) {
            ViewPatientRequestPopup(
                requestId = selectedRequestId!!,
                onDismiss = {
                    showPopup = false
                    selectedRequestId = null
                },
                onAccept = {
                    // TODO: update status in Firestore
                    showPopup = false
                },
                onReject = {
                    // TODO: update status in Firestore
                    showPopup = false
                }
            )

        }
    }
}

@Composable
fun ViewPatientRequestPopup(
    requestId: String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var requestData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var volunteerName by remember { mutableStateOf("") }
    var patientInfo by remember { mutableStateOf<Map<String, String>?>(null) }
    var showAcceptPopup by remember { mutableStateOf(false) }
    var showRejectPopup by remember { mutableStateOf(false) }

    // Load data
    LaunchedEffect(requestId) {
        db.collection("CancerPatientRequestFormRequiredAssistTask")
            .document(requestId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val fullData = doc.data?.toMutableMap() ?: mutableMapOf()
                    fullData["PatRequestFormID"] = doc.id  // 👈 Esse é o ID do documento no Firestore
                    requestData = fullData
                    val patientId = doc.getString("CancerPatientID") ?: ""
                    val volunteerId = doc.getString("VolunteerID") ?: ""

                    db.collection("VolunteeringCancerSurvivor")
                        .document(volunteerId)
                        .get()
                        .addOnSuccessListener {
                            val name = it.getString("VolunteerName") ?: ""
                            val surname = it.getString("VolunteerSurname") ?: ""
                            volunteerName = "$name $surname"
                        }

                    db.collection("PatientinTreatment")
                        .document(patientId)
                        .get()
                        .addOnSuccessListener { pat ->
                            patientInfo = mapOf(
                                "name" to "${pat.getString("CancerPatientName") ?: ""} ${pat.getString("CancerPatientSurname") ?: ""}",
                                "email" to (pat.getString("PatientUsernameEmail") ?: ""),
                                "mobile" to (pat.getString("PatientMobileNo") ?: "")
                            )
                        }
                }
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Patient Request Details") },
        text = {
            if (requestData == null || patientInfo == null) {
                CircularProgressIndicator()
            } else {
                val taskName = requestData?.get("AssistTaskName") ?: "N/A"
                val week = requestData?.get("WeekCommenceDate") ?: "N/A"
                val day1 = requestData?.get("Day1 Availability") ?: ""
                val day2 = requestData?.get("Day2 Availability") ?: ""

                val time1AM = requestData?.get("Day 1 AM") as? String?
                val time1PM = requestData?.get("Day 1 PM") as? String?
                val time2AM = requestData?.get("Day 2 AM") as? String?
                val time2PM = requestData?.get("Day 2 PM") as? String?

                val preferredDay = if (!day1.toString().isNullOrEmpty()) day1 else day2
                val preferredTime = when {
                    !time1AM.isNullOrEmpty() -> time1AM
                    !time1PM.isNullOrEmpty() -> time1PM
                    !time2AM.isNullOrEmpty() -> time2AM
                    !time2PM.isNullOrEmpty() -> time2PM
                    else -> "N/A"
                }

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Dear, $volunteerName.", fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("I am sending you here my request for assistance with the task \"$taskName\" for the week that commences on $week.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("My preferred Day from your Profile Schedule is: $preferredDay", fontSize = 14.sp)
                    Text("My preferred Time for that day is: $preferredTime", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sincerely, ${patientInfo?.get("name")}")
                    Text("Email: ${patientInfo?.get("email")}")
                    Text("Mobile: ${patientInfo?.get("mobile")}")
                }
            }
        },
        confirmButton = {
            Button(onClick = { showAcceptPopup = true }) {
                Text("Accept Request")
            }
        },
        dismissButton = {
            Button(
                onClick = { showRejectPopup = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Reject Request", color = Color.White)
            }
        }
    )

    if (showAcceptPopup && requestData != null) {
        NotificationAcceptScreen(
            requestData = requestData!!,
            onClose = {
                showAcceptPopup = false // Fecha apenas o pop-up de confirmação (2.2B2a)
                onDismiss()             // Fecha a tela anterior (2.2B1)
            },
            onNotificationSent = {
                showAcceptPopup = false // Fecha 2.2B2a
                onDismiss()             // Fecha 2.2B1 e volta para 2.2B
            }
        )
    }

    if (showRejectPopup && requestData != null) {
        NotificationRejectScreen(
            requestData = requestData!!,
            onClose = {
                showRejectPopup = false
                onDismiss()
            },
            onNotificationSent = {
                showRejectPopup = false
                onDismiss()
            }
        )
    }

}
