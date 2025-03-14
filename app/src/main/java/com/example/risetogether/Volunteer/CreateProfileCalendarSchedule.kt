package com.example.risetogether.Volunteer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

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
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 🔥 Buscar o nome do voluntário logado
    LaunchedEffect(auth.currentUser?.uid) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        db.collection("VolunteeringCancerSurvivor").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    volunteerName = document.getString("VolunteerNam") ?: "Unknown"
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

        Text("Hello, $volunteerName", fontSize = 18.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(10.dp))

        // 🔥 Seletor de Data (Simulado)
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text("Select Date") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Button(onClick = { selectedDate = getCurrentDate() }) {
                    Text("Pick")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 Seletor de Horário (Simulado)
        OutlinedTextField(
            value = selectedTime,
            onValueChange = {},
            label = { Text("Select Time") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Button(onClick = { selectedTime = getCurrentTime() }) {
                    Text("Pick")
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 Mensagem de erro
        errorMessage?.let {
            Text(it, color = Color.Red, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 🔥 Botão de Salvar
        Button(
            onClick = {
                val userId = auth.currentUser?.uid ?: return@Button

                if (selectedDate.isBlank() || selectedTime.isBlank()) {
                    errorMessage = "Please select a date and time!"
                    return@Button
                }

                isSaving = true
                errorMessage = null

                val scheduleData = hashMapOf(
                    "VolunteerID" to userId,
                    "VolunteerName" to volunteerName,
                    "ScheduledDate" to selectedDate,
                    "ScheduledTime" to selectedTime,
                    "CreatedAt" to System.currentTimeMillis()
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
    }
}

// 🔥 Função para pegar a data atual (Simulando Date Picker)
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}

// 🔥 Função para pegar a hora atual (Simulando Time Picker)
fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}
