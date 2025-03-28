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

    var selectedWeek by remember { mutableStateOf("") }
    var weekExpanded by remember { mutableStateOf(false) }
    val weekOptions = listOf("Monday1", "Monday2", "Monday3")

    var latestRequest by remember { mutableStateOf("Loading...") }
    var earliestRequest by remember { mutableStateOf("Loading...") }

    val volunteerId = auth.currentUser?.uid ?: ""

    fun fetchRequests() {
        if (selectedWeek.isEmpty()) return

        db.collection("CancerPatientRequestFormForRequiredAssistTask")
            .whereEqualTo("VolunteerID", volunteerId)
            .whereEqualTo("WeekCommenceDate", selectedWeek)
            .get()
            .addOnSuccessListener { documents ->
                val sortedDocs = documents.sortedBy { it.getString("RequestDate") + it.getString("RequestTime") }

                earliestRequest = sortedDocs.firstOrNull()?.getString("AssistanceTaskName") ?: "None"
                latestRequest = sortedDocs.lastOrNull()?.getString("AssistanceTaskName") ?: "None"
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
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = weekExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = weekExpanded,
                        onDismissRequest = { weekExpanded = false }
                    ) {
                        weekOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedWeek = option
                                    weekExpanded = false
                                    fetchRequests()
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
                    Text("Latest Requested Assistance Task:", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(latestRequest, fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { /* Abre Pop-up */ }) {
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
                    Text("Earliest Requested Assistance Task:", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text(earliestRequest, fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { /* Abre Pop-up */ }) {
                        Text("View")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    selectedWeek = ""
                    latestRequest = ""
                    earliestRequest = ""
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
    }
}
