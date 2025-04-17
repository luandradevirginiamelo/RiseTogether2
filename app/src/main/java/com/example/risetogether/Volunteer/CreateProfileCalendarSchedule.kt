package com.example.risetogether.Volunteer

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight

class CreateProfileCalendarScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // ✅ FIXED THIS LINE!
        setContent {
            CreateProfileCalendarSchedule()
        }
    }
}

val Green = Color(0xFF81C784)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF81C784), Color(0xFFFFF8E1)) // Verde → Bege
                )
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabeçalho
            Text(
                "Schedule Assistance Task",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🧊 Container branco com sombra
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Assistance Task Name: $selectedTaskName", fontSize = 16.sp)
                    Text("Category: $selectedTaskCategory", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

        // 🔥 Generate fields dynamically based on selected days
        repeat(selectedDays) { dayIndex ->
            val dayNumber = dayIndex + 1
            var selectedDay by remember { mutableStateOf("") }
            var selectedAMTime by remember { mutableStateOf("") }
            var selectedPMTime by remember { mutableStateOf("") }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Day $dayNumber of the Week Availability:", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    DropdownField(
                        label = "Select Day",
                        options = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                        selectedOption = selectedDay
                    ) { selected ->
                        selectedDay = selected
                        schedule["Day$dayNumber"] = Triple(selected, schedule["Day$dayNumber"]?.second, schedule["Day$dayNumber"]?.third)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    DropdownField(
                        label = "AM Time",
                        options = listOf("08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00"),
                        selectedOption = selectedAMTime
                    ) { selected ->
                        selectedAMTime = selected
                        schedule["Day$dayNumber"] = Triple(schedule["Day$dayNumber"]?.first, selected, schedule["Day$dayNumber"]?.third)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    DropdownField(
                        label = "PM Time",
                        options = listOf("14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00", "17:00 - 18:00"),
                        selectedOption = selectedPMTime
                    ) { selected ->
                        selectedPMTime = selected
                        schedule["Day$dayNumber"] = Triple(schedule["Day$dayNumber"]?.first, schedule["Day$dayNumber"]?.second, selected)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

            errorMessage?.let {
                Text(it, color = Color.Red, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (schedule.isEmpty()) {
                        errorMessage = "Please select at least one time slot!"
                        return@Button
                    }

                    val hasAtLeastOneTimeSelected = schedule.values.any {
                        !it.second.isNullOrBlank() || !it.third.isNullOrBlank()
                    }
                    if (!hasAtLeastOneTimeSelected) {
                        errorMessage = "Please select at least one AM or PM time!"
                        return@Button
                    }

                    val scheduleData = hashMapOf<String, Any?>(
                        "VolunteerCalendarProfileScheduleID" to "",
                        "VolunteerProfileID" to volunteerProfileID,
                        "VolunteerID" to currentVolunteerID,
                        "AssistTaskCatID" to assistTaskCatID,
                        "AssistTaskID" to assistTaskID,
                        "DaysSelected" to selectedDays,
                    )

                    schedule.forEach { (dayKey, triple) ->
                        val dayNumber = dayKey.filter { it.isDigit() }
                        scheduleData["Day$dayNumber Availability"] = triple.first?.takeIf { it.isNotBlank() } ?: null
                        scheduleData["Day $dayNumber AM"] = triple.second?.takeIf { it.isNotBlank() } ?: null
                        scheduleData["Day $dayNumber PM"] = triple.third?.takeIf { it.isNotBlank() } ?: null
                    }
                    isSaving = true
                    db.collection("VolunteeringSurvivorAssistTaskCategoryPersonalProfileCalendarSchedule")
                        .add(scheduleData)
                        .addOnSuccessListener { documentReference ->
                            val generatedID = documentReference.id
                            db.collection("VolunteeringSurvivorAssistTaskCategoryPersonalProfileCalendarSchedule")
                                .document(generatedID)
                                .update("VolunteerCalendarProfileScheduleID", generatedID)
                                .addOnSuccessListener {
                                    isSaving = false
                                    isSaved = true
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
                modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFF8E1), Color(0xFF81C784))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSaving) "Saving..." else "Save Schedule",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isSaved) {
            AlertDialog(
                onDismissRequest = { isSaved = false },
                title = { Text("✅ Success") },
                text = { Text("Schedule successfully saved!") },
                confirmButton = {
                    Button(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Text("✖ Close")
                    }
                }
            )
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