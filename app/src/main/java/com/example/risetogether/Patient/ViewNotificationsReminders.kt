package com.example.risetogether.Patient

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

class ViewNotificationsRemindersScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewNotificationsRemindersContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNotificationsRemindersContent() {
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
    var patCancerType by remember { mutableStateOf("") }

    LaunchedEffect(volunteerId) {
        val patientId = auth.currentUser?.uid ?: ""
        db.collection("PatientinTreatment")
            .document(patientId)
            .get()
            .addOnSuccessListener { doc ->
                patCancerType = doc.getString("PatCancerType") ?: ""
            }
    }

    LaunchedEffect(volunteerId) {
        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db.collection("NotificationOfPatientRequest")
            .whereEqualTo("PatID", patientId)
            .get()
            .addOnSuccessListener { snapshot ->
                val weeks = snapshot.documents.mapNotNull { it.getString("WeekCommenceDate") }.distinct()
                availableWeeks = weeks.sorted()
            }
    }


    fun fetchNotificationsForWeek(week: String) {
        val patientId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db.collection("NotificationOfPatientRequest")
            .whereEqualTo("PatID", patientId)
            .whereEqualTo("WeekCommenceDate", week)
            .get()
            .addOnSuccessListener { documents ->
                val sorted = documents.sortedByDescending {
                    val date = it.getString("NotificDate") ?: ""
                    val time = it.getString("NotificTime") ?: ""
                    "$date $time"
                }

                latestRequest = sorted.firstOrNull()?.let {
                    mapOf(
                        "name" to (it.getString("AssistTaskName") ?: "N/A"),
                        "date" to (it.getString("NotificDate") ?: ""),
                        "time" to (it.getString("NotificTime") ?: ""),
                        "requestId" to it.id // Usar ID do documento da notificação!
                    )
                }

                earliestRequest = sorted.lastOrNull()?.let {
                    mapOf(
                        "name" to (it.getString("AssistTaskName") ?: "N/A"),
                        "date" to (it.getString("NotificDate") ?: ""),
                        "time" to (it.getString("NotificTime") ?: ""),
                        "requestId" to it.id
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
                        Color(0xFFB3E5FC),
                        Color(0xFF42A5F5),
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

                Text("List of Notifications for Requests Acceptance/Rejections", fontSize = 20.sp, color = Color.Black)

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
                                    fetchNotificationsForWeek(week)
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
                    context.startActivity(Intent(context, PatientDashboardScreen::class.java))
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


    // Load data
    LaunchedEffect(requestId) {
        db.collection("NotificationOfPatientRequest")
            .document(requestId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    val volunteerId = doc.getString("VolunteerID") ?: ""
                    val patientId = doc.getString("PatID") ?: ""

                    db.collection("VolunteeringCancerSurvivor")
                        .document(volunteerId)
                        .get()
                        .addOnSuccessListener { vol ->
                            data["VoluntNam+Surnam"] = "${vol.getString("VolunteerName")} ${vol.getString("VolunteerSurname")}"
                            data["VoluntEmail"] = vol.getString("VolunteerUsername") ?: ""
                            data["VoluntMobile"] = vol.getString("VolunMobile") ?: ""

                            db.collection("PatientinTreatment")
                                .document(patientId)
                                .get()
                                .addOnSuccessListener { pat ->
                                    data["PatNam+Surnam"] = "${pat.getString("CancerPatientName")} ${pat.getString("CancerPatientSurname")}"
                                    requestData = data // ✅ Agora sim o mapa está pronto
                                }
                        }
            }
    }

    }

    val status = requestData?.get("RequestStatus") ?: ""

    if (requestData != null) {
        if (status == "Accepted") {
            DisplayNotificationAcceptScreen(
                requestData = requestData!!,
                onClose = { onDismiss() }
            )
        } else if (status == "Rejected") {
            DisplayNotificationRejectScreen(
                requestData = requestData!!,
                onClose = { onDismiss() }
            )
        }

    }

}


