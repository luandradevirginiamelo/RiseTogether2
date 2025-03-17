package com.example.risetogether.Volunteer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateProfileCalendarScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreateProfileCalendarSchedule()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileCalendarSchedule() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var volunteerName by remember { mutableStateOf("") }
    var selectedTaskName by remember { mutableStateOf("") }
    var selectedTaskCategory by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(1) } // Default is 1

    var schedule by remember { mutableStateOf(mutableMapOf<String, Triple<String, String, String>>()) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val userId = auth.currentUser?.uid ?: ""

    // 🔥 Fetch data from Firestore
    LaunchedEffect(userId) {
        db.collection("VolunteeringSurvivorProfilewithAssistTask&Category")
            .whereEqualTo("VolunteerID", userId)
            .get()
            .addOnSuccessListener { documents ->
                documents.firstOrNull()?.let { doc ->
                    selectedTaskName = doc.getString("AssistTaskNam") ?: ""
                    selectedTaskCategory = doc.getString("AssistTaskCategoryID") ?: ""
                    selectedDays = doc.getString("NoOfDaysPerWeek")?.toIntOrNull() ?: 1
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Schedule Assistance Task", fontSize = 22.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Assistance Task: $selectedTaskName", fontSize = 18.sp, color = Color.Black)
        Text("Category: $selectedTaskCategory", fontSize = 16.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 Generate fields based on selectedDays
        for (day in 1..selectedDays) {
            var selectedDay by remember { mutableStateOf("") }
            var selectedAMTime by remember { mutableStateOf("") }
            var selectedPMTime by remember { mutableStateOf("") }

            Text("Day $day Availability", fontSize = 16.sp, color = Color.Black)

            DropdownField("Select Day", listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")) {
                selectedDay = it
                schedule["Day$day"] = Triple(it, schedule["Day$day"]?.second ?: "", schedule["Day$day"]?.third ?: "")
            }

            DropdownField("AM Time", listOf("08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00")) {
                selectedAMTime = it
                schedule["Day$day"] = Triple(schedule["Day$day"]?.first ?: "", it, schedule["Day$day"]?.third ?: "")
            }

            DropdownField("PM Time", listOf("14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00", "17:00 - 18:00")) {
                selectedPMTime = it
                schedule["Day$day"] = Triple(schedule["Day$day"]?.first ?: "", schedule["Day$day"]?.second ?: "", it)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 Show error message if any
        errorMessage?.let {
            Text(it, color = Color.Red, fontSize = 16.sp)
        }

        // 🔥 Save Schedule Button
        Button(
            onClick = {
                if (schedule.isEmpty()) {
                    errorMessage = "Please select at least one time slot!"
                    return@Button
                }

                isSaving = true
                errorMessage = null

                val scheduleData = hashMapOf(
                    "VolunteerID" to userId,
                    "TaskName" to selectedTaskName,
                    "TaskCategory" to selectedTaskCategory,
                    "DaysSelected" to selectedDays,
                    "Schedule" to schedule
                )

                db.collection("VolunteerSchedule")
                    .add(scheduleData)
                    .addOnSuccessListener {
                        isSaving = false
                        Toast.makeText(context, "Schedule created successfully!", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        isSaving = false
                        errorMessage = "Error saving schedule."
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text(if (isSaving) "Saving..." else "Save Schedule")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 🔥 Close Button
        Button(
            onClick = {
                (context as? ComponentActivity)?.finish()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Close")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 🔥 Another Assistance Task Button
        Button(
            onClick = {
                context.startActivity(Intent(context, VolunteerProfileScreen::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Another Assistance Task")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 🔥 Back to Dashboard Button
        Button(
            onClick = {
                context.startActivity(Intent(context, VolunteerDashboardScreen::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Dashboard")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, options: List<String>, onSelection: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    selectedOption = it
                    onSelection(it)
                    expanded = false
                })
            }
        }
    }
}

