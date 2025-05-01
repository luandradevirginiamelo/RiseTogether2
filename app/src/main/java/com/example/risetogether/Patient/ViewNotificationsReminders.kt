package com.example.risetogether.Patient

import android.content.Intent
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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

    @Composable
    fun Funbutton(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
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
                        "requestId" to it.id
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
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            SmallGradientButton(
                                text = "Logout",
                                onClick = {
                                    FirebaseAuth.getInstance().signOut()

                                    val intent = Intent(context, PatientLoginScreen::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }

                                    context.startActivity(intent)
                                }
                            )
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
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "List of Notifications for Requests Acceptance/Rejections",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Select Week Commence Date:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0D47A1)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

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

                            // LATEST
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F8E9), RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "📌 Notifications",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF33691E)
                                )
                                Text("Task Name: ${latestRequest?.get("name") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                                Text("Date: ${latestRequest?.get("date") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                                Text("Time: ${latestRequest?.get("time") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        selectedRequestId = latestRequest?.get("requestId")
                                        showPopup = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("View")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // EARLIEST
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "📌 Notifications",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D47A1)
                                )
                                Text("Task Name: ${earliestRequest?.get("name") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                                Text("Date: ${earliestRequest?.get("date") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                                Text("Time: ${earliestRequest?.get("time") ?: "N/A"}", fontSize = 14.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        selectedRequestId = earliestRequest?.get("requestId")
                                        showPopup = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("View")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Funbutton(text = "Select Another Week Commence Date") {
                        selectedWeek = ""
                        latestRequest = null
                        earliestRequest = null
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Funbutton(text = "Back to Dashboard") {
                        context.startActivity(Intent(context, PatientDashboardScreen::class.java))
                    }


                }

                if (showPopup && selectedRequestId != null) {
                    ViewPatientRequestPopup(
                        requestId = selectedRequestId!!,
                        onDismiss = {
                            showPopup = false
                            selectedRequestId = null
                        },
                        onAccept = {
                            showPopup = false
                        },
                        onReject = {
                            showPopup = false
                        }
                    )
                }
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
        val context = LocalContext.current

        var requestData by remember { mutableStateOf<Map<String, Any>?>(null) }

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
                                        requestData = data
                                    }
                            }
                    }
                }
        }

        val status = requestData?.get("RequestStatus") ?: ""

        if (requestData != null) {
            when (status) {
                "Accepted" -> DisplayNotificationAcceptScreen(
                    requestData = requestData!!,
                    onClose = onDismiss
                )
                "Rejected" -> DisplayNotificationRejectScreen(
                    requestData = requestData!!,
                    onClose = onDismiss
                )
            }

    }

        }



