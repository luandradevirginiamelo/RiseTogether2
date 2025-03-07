package com.example.risetogether.Patient

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ViewDetailsSelectedVolunteer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get volunteer name from previous screen
        val volunteerName = intent.getStringExtra("volunteerName") ?: ""

        setContent {
            ViewDetailsSelectedVolunteerScreen(volunteerName)
        }
    }
}

@Composable
fun ViewDetailsSelectedVolunteerScreen(volunteerName: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Volunteer Details State
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var cancerType by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf("") }

    // Availability State
    var day1AM by remember { mutableStateOf(false) }
    var day1PM by remember { mutableStateOf(false) }
    var day2AM by remember { mutableStateOf(false) }
    var day2PM by remember { mutableStateOf(false) }

    // Selected Date
    var selectedDate by remember { mutableStateOf("") }

    // Fetch Volunteer Data from Firestore
    LaunchedEffect(volunteerName) {
        if (volunteerName.isNotBlank()) {
            val nameParts = volunteerName.split(" ") // Split "Brian Obrien" into ["Brian", "Obrien"]
            val firstName = nameParts.getOrNull(0) ?: ""
            val lastName = nameParts.getOrNull(1) ?: ""

            db.collection("VolunteeringCancerSurvivor")
                .whereEqualTo("VolunteerName", firstName)
                .whereEqualTo("VolunteerSurname", lastName) // Ensure last name is also checked
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents.first()
                        email = document.getString("VolunteerUsername") ?: "Not Available"
                        mobile = document.getString("VolunteerMobileNO") ?: "Not Available"
                        gender = document.getString("VolunteerGender") ?: "Not Available"
                        cancerType = document.getString("CancerType") ?: "Not Available"
                        val volunteerID = document.getString("VolunteerID") ?: ""

                        if (volunteerID.isNotEmpty()) {
                            // Fetch Task Assistance based on VolunteerID
                            db.collection("VolunteeringSurvivorProfilewithAssistTask&Category")
                                .whereEqualTo("VolunteerID", volunteerID)
                                .get()
                                .addOnSuccessListener { taskDocs ->
                                    if (!taskDocs.isEmpty) {
                                        val taskDoc = taskDocs.documents.first()
                                        val assistTaskID = taskDoc.getString("AssistTaskID") ?: "Not Available"

                                        // Fetch the actual Task Name from AssistanceTaskwithinCategory
                                        db.collection("AssistanceTaskwithinCategory")
                                            .whereEqualTo("AssistTaskID", assistTaskID)
                                            .get()
                                            .addOnSuccessListener { taskNameDocs ->
                                                if (!taskNameDocs.isEmpty) {
                                                    selectedTask = taskNameDocs.documents.first().getString("AssistanceTaskNam") ?: "Not Available"
                                                } else {
                                                    selectedTask = "Task Not Found"
                                                }
                                            }
                                    } else {
                                        selectedTask = "No Assigned Task"
                                    }
                                }
                        }
                    } else {
                        Toast.makeText(context, "No volunteer found!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load volunteer details", Toast.LENGTH_SHORT).show()
                }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "View Details of Selected Volunteer", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Volunteer Details
            VolunteerInfoRow("Volunteer Name:", "$volunteerName")
            VolunteerInfoRow("Email Address:", email)
            VolunteerInfoRow("Mobile Number:", mobile)
            VolunteerInfoRow("Gender:", gender)
            VolunteerInfoRow("Cancer Type:", cancerType)
            VolunteerInfoRow("Selected Task for Assistance:", selectedTask)

            Spacer(modifier = Modifier.height(20.dp))

            // Volunteer Availability Section
            Text("Volunteer Availability:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // Day 1
            Text("Day 1 Availability")
            Row {
                Checkbox(checked = day1AM, onCheckedChange = { day1AM = it })
                Text("AM")
                Spacer(modifier = Modifier.width(16.dp))
                Checkbox(checked = day1PM, onCheckedChange = { day1PM = it })
                Text("PM")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day 2
            Text("Day 2 Availability")
            Row {
                Checkbox(checked = day2AM, onCheckedChange = { day2AM = it })
                Text("AM")
                Spacer(modifier = Modifier.width(16.dp))
                Checkbox(checked = day2PM, onCheckedChange = { day2PM = it })
                Text("PM")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Calendar Picker
            Text("Select Week Commencing Date:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                }, year, month, day).show()
            }) {
                Text(if (selectedDate.isEmpty()) "Select Date" else selectedDate)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Button(onClick = {
                if (selectedDate.isEmpty() || (!day1AM && !day1PM && !day2AM && !day2PM)) {
                    Toast.makeText(context, "Please select a date and availability", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Request Placed Successfully!", Toast.LENGTH_SHORT).show()
                    // Navigate to next screen if needed
                }
            }) {
                Text("Place Request for Assistance")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                // Navigate back to volunteer list
            }) {
                Text("Back to List of Volunteers")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                // Navigate back to Dashboard
            }) {
                Text("Back to Dashboard")
            }
        }
    }
}

// Helper Composable to display volunteer details
@Composable
fun VolunteerInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        SelectionContainer {
            Text(value, fontSize = 16.sp)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
