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



data class VolunteerDayAvailability(
    val dayLabel: String,
    val amTime: String,
    val pmTime: String
)

class ViewDetailsSelectedVolunteer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val volunteerID = intent.getStringExtra("volunteerID") ?: ""
        val volunteerProfileID = intent.getStringExtra("volunteerProfileID") ?: ""
        val selectedTaskID = intent.getStringExtra("selectedTaskID") ?: ""
        val selectedTaskCatID = intent.getStringExtra("selectedTaskCatID") ?: ""

        setContent {
            ViewDetailsSelectedVolunteerScreen(
                volunteerID = volunteerID,
                volunteerProfileID = volunteerProfileID,
                selectedTaskID = selectedTaskID,
                selectedTaskCatID = selectedTaskCatID
            )
        }
    }
}

@Composable
fun ViewDetailsSelectedVolunteerScreen(
    volunteerID: String,
    volunteerProfileID: String,
    selectedTaskID: String,
    selectedTaskCatID: String
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var voluncancertype by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf("") }
    var volunteerNameDisplayed by remember { mutableStateOf("") }
    val availabilityList = remember { mutableStateListOf<VolunteerDayAvailability>() }
    var selectedDate by remember { mutableStateOf("") }



    // Fetch Volunteer Data from Firestore
    LaunchedEffect(volunteerID) {
        if (volunteerID.isNotBlank()) {
            db.collection("VolunteeringCancerSurvivor")
                .whereEqualTo("VolunteerID", volunteerID)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.first()
                        volunteerNameDisplayed = "${document.getString("VolunteerName") ?: ""} ${document.getString("VolunteerSurname") ?: ""}"
                        email = document.getString("VolunteerUsername") ?: "Not Available"
                        mobile = document.getString("VolunMobile") ?: "Not Available"
                        gender = document.getString("VolunGender") ?: "Not Available"
                        voluncancertype = document.getString("VolunCancerType") ?: "Not Available"

                        // Get Task Name
                        db.collection("AssistanceTaskwithinCategory")
                            .whereEqualTo("AssistTaskID", selectedTaskID)
                            .get()
                            .addOnSuccessListener { taskDocs ->
                                selectedTask = taskDocs.firstOrNull()?.getString("AssistanceTaskNam") ?: "Task Not Found"
                            }

                        // Get Availability
                        db.collection("VolunteeringSurvivorAssistTaskCategoryPersonalProfileCalendarSchedule")
                            .whereEqualTo("VolunteerProfileID", volunteerProfileID)
                            .whereEqualTo("AssistTaskID", selectedTaskID)
                            .whereEqualTo("AssistTaskCatID", selectedTaskCatID)
                            .get()
                            .addOnSuccessListener { scheduleDocs ->
                                if (!scheduleDocs.isEmpty) {
                                    val scheduleMap = scheduleDocs.first().get("Schedule") as? Map<String, Map<String, Any>>
                                    availabilityList.clear()
                                    scheduleMap?.forEach { (_, dayData) ->
                                        val dayLabel = dayData["Day"] as? String ?: ""
                                        val am = dayData["AMTime"] as? String ?: ""
                                        val pm = dayData["PMTime"] as? String ?: ""
                                        if (dayLabel.isNotEmpty()) {
                                            availabilityList.add(VolunteerDayAvailability(dayLabel, am, pm))
                                        }
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
            Text("View Details of Selected Volunteer", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            VolunteerInfoRow("Volunteer Name:", volunteerNameDisplayed)
            VolunteerInfoRow("Email Address:", email)
            VolunteerInfoRow("Mobile Number:", mobile)
            VolunteerInfoRow("Gender:", gender)
            VolunteerInfoRow("Cancer Type:", voluncancertype)
            VolunteerInfoRow("Selected Task for Assistance:", selectedTask)

            Spacer(modifier = Modifier.height(20.dp))
            // Volunteer Availability Section
            Text("Volunteer Availability:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            availabilityList.forEachIndexed { index, item ->
                Column {
                    Text("Day ${index + 1}: ${item.dayLabel}", fontWeight = FontWeight.SemiBold)
                    if (item.amTime.isNotBlank()) {
                        Text("AM Time: ${item.amTime}")
                    }
                    if (item.pmTime.isNotBlank()) {
                        Text("PM Time: ${item.pmTime}")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

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

            Button(onClick = {
                if (selectedDate.isEmpty() || availabilityList.isEmpty()) {
                    Toast.makeText(context, "Please select a date and check availability", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Request Placed Successfully!", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Place Request for Assistance")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                // TODO: Back to list of volunteers
            }) {
                Text("Back to List of Volunteers")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                // TODO: Back to Dashboard
            }) {
                Text("Back to Dashboard")
            }
        }
    }
}

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
