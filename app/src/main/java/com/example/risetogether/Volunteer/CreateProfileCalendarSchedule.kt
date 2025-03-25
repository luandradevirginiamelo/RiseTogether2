package com.example.risetogether.Volunteer

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign // ✅ IMPORTED TO FIX ERROR
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

class CreateProfileCalendarScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // ✅ FIXED THIS LINE!
        setContent {
            CreateProfileCalendarSchedule()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileCalendarSchedule() {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentVolunteerID = auth.currentUser?.uid ?: ""

    var selectedTaskName by remember { mutableStateOf("") }
    var selectedTaskCategory by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(1) }
    var assistTaskCatID by remember { mutableStateOf("") }
    var assistTaskID by remember { mutableStateOf("") }

    val volunteerProfileID = (context as? ComponentActivity)?.intent?.getStringExtra("VolunteerProfileID") ?: ""

    var schedule by remember { mutableStateOf(mutableMapOf<String, Triple<String?, String?, String?>>()) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(volunteerProfileID) {
        if (volunteerProfileID.isNotEmpty()) {
            db.collection("VolunteeringSurvivorProfilewithAssistTaskCategory")
                .document(volunteerProfileID)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        selectedTaskName = doc.getString("AssistTaskNam") ?: "No Task"
                        selectedTaskCategory = doc.getString("AssistTaskCatID") ?: "No Category"
                        assistTaskCatID = doc.getString("AssistTaskCatID") ?: ""
                        assistTaskID = doc.getString("AssistTaskID") ?: ""
                        selectedDays = when (val value = doc.get("NoOfDaysPerWeek")) {
                            is Long -> value.toInt()
                            is String -> value.toIntOrNull() ?: 1
                            else -> 1
                        }
                        Log.d("Firestore", "✅ Dados carregados com sucesso para VolunteerProfileID: $volunteerProfileID")
                    } else {
                        errorMessage = "No task found for this VolunteerProfileID."
                        Log.e("Firestore", "⚠️ Documento não encontrado para o ID: $volunteerProfileID")
                    }
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Failed to fetch task details: ${exception.localizedMessage}"
                    Log.e("Firestore", "❌ Erro ao buscar dados: ${exception.localizedMessage}")
                }
        } else {
            errorMessage = "VolunteerProfileID is missing!"
            Log.e("Firestore", "❌ VolunteerProfileID veio vazio.")
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

        Text("Assistance Task Name: $selectedTaskName", fontSize = 18.sp, color = Color.Black)
        Text("Assistance Task Category: $selectedTaskCategory", fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 Generate fields dynamically based on selected days
        repeat(selectedDays) { dayIndex ->
            val dayNumber = dayIndex + 1
            var selectedDay by remember { mutableStateOf("") }
            var selectedAMTime by remember { mutableStateOf("") }
            var selectedPMTime by remember { mutableStateOf("") }

            Text("Day $dayNumber of the Week Availability:", fontSize = 16.sp, color = Color.Black)

            DropdownField(
                label = "Select Day",
                options = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                selectedOption = selectedDay
            ) { selected ->
                selectedDay = selected
                schedule["Day$dayNumber"] = Triple(selected, schedule["Day$dayNumber"]?.second ?: "", schedule["Day$dayNumber"]?.third ?: "")
            }

            DropdownField(
                label = "AM Time",
                options = listOf("08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00"),
                selectedOption = selectedAMTime
            ) { selected ->
                selectedAMTime = selected
                schedule["Day$dayNumber"] = Triple(schedule["Day$dayNumber"]?.first ?: "", selected, schedule["Day$dayNumber"]?.third ?: "")
            }

            DropdownField(
                label = "PM Time",
                options = listOf("14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00", "17:00 - 18:00"),
                selectedOption = selectedPMTime
            ) { selected ->
                selectedPMTime = selected
                schedule["Day$dayNumber"] = Triple(schedule["Day$dayNumber"]?.first ?: "", schedule["Day$dayNumber"]?.second ?: "", selected)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        errorMessage?.let {
            Text(it, color = Color.Red, fontSize = 16.sp)
        }

        // 🔥 Save button
        Button(
            onClick = {
                if (schedule.isEmpty()) {
                    errorMessage = "Please select at least one time slot!"
                    return@Button
                }

                // ✅ Check if at least one of the time is selected!
                val hasAtLeastOneTimeSelected = schedule.values.any {
                    !it.second.isNullOrBlank() || !it.third.isNullOrBlank()
                }
                if (!hasAtLeastOneTimeSelected) {
                    errorMessage = "Please select at least one AM or PM time for the week!"
                    return@Button
                }

                val scheduleData = hashMapOf(
                    "VolunteerCalendarProfileScheduleID" to "", // 🔥 This will be updated after Firestore generates the ID
                    "VolunteerProfileID" to volunteerProfileID,
                    "VolunteerID" to currentVolunteerID, // ✅🔥 AQUI ESTÁ A CHAVE!
                    "AssistTaskCatID" to assistTaskCatID, // ✅ Matching TaskCatID
                    "AssistTaskID" to assistTaskID, // ✅ Matching TaskID
                    "DaysSelected" to selectedDays,
                    "Schedule" to schedule.mapValues {
                        hashMapOf(
                            "Day" to it.value.first,
                            "AMTime" to (it.value.second ?: "null"),
                            "PMTime" to (it.value.third ?: "null")
                        )
                    }
                )

                // 🔥 Add to Firestore and Capture Auto-Generated ID
                db.collection("VolunteeringSurvivorAssistTaskCategoryPersonalProfileCalendarSchedule")
                    .add(scheduleData)
                    .addOnSuccessListener { documentReference ->
                        val generatedID = documentReference.id

                        // 🔥 Update the document with its own Firestore-generated ID
                        db.collection("VolunteeringSurvivorAssistTaskCategoryPersonalProfileCalendarSchedule")
                            .document(generatedID)
                            .update("VolunteerCalendarProfileScheduleID", generatedID)
                            .addOnSuccessListener {
                                isSaving = false
                                isSaved = true // ✅ Now success message & buttons appear!
                            }
                            .addOnFailureListener {
                                isSaving = false
                                errorMessage = "Error updating Schedule ID."
                            }
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
    }

    if (isSaved) { // ✅ Show success message & buttons after saving
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) // ✅ FIXED ERROR (background)
                .padding(16.dp), // ✅ Inner padding
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "🎉 Record of Assistance Task & Category personal Calendar schedule successfully created! 🎉\n\n" +
                            "You may press 'Another Assistance Task' button or 'Back to Dashboard'.",
                    fontSize = 16.sp,
                    color = Color(0xFF2E7D32), // ✅ Dark Green Text
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center // ✅ FIXED ERROR (TextAlign)
                )

                Button(
                    onClick = { (context as? ComponentActivity)?.finish() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { context.startActivity(Intent(context, VolunteerProfileScreen::class.java)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Another Assistance Task")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { context.startActivity(Intent(context, VolunteerDashboardScreen::class.java)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Dashboard")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, options: List<String>, selectedOption: String, onSelection: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelection(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
