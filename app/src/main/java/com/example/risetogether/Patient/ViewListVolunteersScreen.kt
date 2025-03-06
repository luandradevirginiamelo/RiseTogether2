package com.example.risetogether.Patient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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


class ViewListVolunteersScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewListVolunteersScreenContent()
        }
    }
}
@Composable
fun SmallGradientButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(76.dp)
            .width(105.dp)
            .padding(end = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5)) // 🔥 Azul Claro → Azul Médio
                    ),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewListVolunteersScreenContent() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var selectedCategory by remember { mutableStateOf("") }
    var taskOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedTask by remember { mutableStateOf("") }
    var selectedTaskID by remember { mutableStateOf("") }
    var volunteerOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedVolunteer by remember { mutableStateOf("") }
    var patientCancerType by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("") }

    val assistanceOptions = listOf("Household", "Garden", "Transportation")

    // Fetch patient info (Cancer Type + Gender)
    LaunchedEffect(auth.currentUser?.uid) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        db.collection("PatientinTreatment").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    patientCancerType = document.getString("CancerType") ?: ""
                    patientGender = document.getString("CancerPatientGender") ?: ""
                }
            }
    }

    // Fetch Tasks when Category is Selected
    LaunchedEffect(selectedCategory) {
        if (selectedCategory.isBlank()) return@LaunchedEffect

        val categoryIDMap = mapOf(
            "Household" to "abc123",
            "Garden" to "def456",
            "Transportation" to "ghi789"
        )

        val categoryID = categoryIDMap[selectedCategory] ?: return@LaunchedEffect

        db.collection("AssistanceTaskwithinCategory")
            .whereEqualTo("AssistTaskCat", categoryID)
            .get()
            .addOnSuccessListener { documents ->
                val tasks = documents.mapNotNull { it.getString("AssistanceTaskNam") }
                taskOptions = if (tasks.isNotEmpty()) tasks else listOf("No tasks available")
                selectedTask = taskOptions.firstOrNull() ?: ""
                selectedTaskID = documents.firstOrNull()?.getString("AssistTaskID") ?: ""
            }
    }

    // Fetch Volunteers when Task is Selected
    LaunchedEffect(selectedTaskID) {
        if (selectedTaskID.isBlank() || patientCancerType.isBlank() || patientGender.isBlank()) return@LaunchedEffect

        db.collection("VolunteeringSurvivorProfilewithAssistTask&Category")
            .whereEqualTo("AssistTaskID", selectedTaskID)
            .whereEqualTo("VolunteerGender", patientGender)
            .get()
            .addOnSuccessListener { documents ->
                val volunteerIDs = documents.mapNotNull { it.getString("VolunteerID") }
                if (volunteerIDs.isNotEmpty()) {
                    db.collection("VolunteeringCancerSurvivor")
                        .whereIn("VolunteerID", volunteerIDs)
                        .whereEqualTo("CancerType", patientCancerType)
                        .get()
                        .addOnSuccessListener { volunteerDocs ->
                            val volunteers = volunteerDocs.mapNotNull {
                                val name = it.getString("VolunteerName")
                                val surname = it.getString("VolunteerSurname")
                                if (name != null && surname != null) "$name $surname" else null
                            }
                            volunteerOptions = if (volunteers.isNotEmpty()) volunteers else listOf("No volunteers available")
                            selectedVolunteer = volunteerOptions.firstOrNull() ?: ""
                        }
                }
            }
    }

    // Save automatically when Volunteer is selected
    LaunchedEffect(selectedVolunteer) {
        if (selectedVolunteer.isNotBlank()) {
            val userId = auth.currentUser?.uid ?: return@LaunchedEffect
            db.collection("PatientinTreatment").document(userId)
                .update("AssignedVolunteer", selectedVolunteer)
                .addOnSuccessListener {
                    Toast.makeText(context, "Volunteer Assigned Successfully!", Toast.LENGTH_LONG).show()
                }
        }
    }



    // Layout and UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5), Color(0xFF9E9E9E))
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
                                    auth.signOut()
                                    context.startActivity(Intent(context, PatientLoginScreen::class.java))
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
                        Text("List of Volunteers for Required Assistance Task", fontSize = 18.sp, color = Color.Black)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Select Assistance Task Category:", fontSize = 16.sp, color = Color.Black)
                        DropdownComponent(selectedCategory, assistanceOptions) { selectedCategory = it }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Assistance Task within Selected Category:", fontSize = 16.sp, color = Color.Black)
                        DropdownComponent(selectedTask, taskOptions) { selectedTask = it }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Available Volunteers for Selected Task:", fontSize = 16.sp, color = Color.Black)
                        DropdownComponent(selectedVolunteer, volunteerOptions) { selectedVolunteer = it }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownComponent(selectedValue: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
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
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}