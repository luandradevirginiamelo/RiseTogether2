package com.example.risetogether.Patient

import android.content.Intent
import android.os.Bundle
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
import android.os.Handler
import android.os.Looper
import android.widget.Toast


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

    var volunteerNameToProfileID by remember { mutableStateOf(mapOf<String, String>()) }

    var selectedCategory by remember { mutableStateOf("") }
    var taskOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedTask by remember { mutableStateOf("") }
    var selectedTaskID by remember { mutableStateOf("") }
    var volunteerOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedVolunteer by remember { mutableStateOf("") }
    var patientCancerType by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("") }
    var taskIDs by remember { mutableStateOf(listOf<String>()) } // 🔥 Adicionando lista para TaskIDs

    val assistanceOptions = listOf("Household", "Garden", "Transportation")

    fun updateSelectedTask(task: String, taskID: String) {
        selectedTask = task
        selectedTaskID = taskID
    }

    // Fetch patient info (Cancer Type + Gender)
    LaunchedEffect(auth.currentUser?.uid) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect

        db.collection("PatientinTreatment").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    patientCancerType = document.getString("PatCancerType") ?: ""
                    patientGender = document.getString("CancerPatientGender") ?: ""

                    println("Paciente encontrado: $patientCancerType, $patientGender")
                } else {
                    println("❌ Paciente não encontrado!")
                }
            }
            .addOnFailureListener { exception ->
                println("❌ Erro ao buscar paciente: ${exception.message}")
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
            .whereEqualTo("AssistTaskCatID", categoryID)
            .get()
            .addOnSuccessListener { documents ->
                val tasks = documents.mapNotNull { it.getString("AssistanceTaskNam") }
                val ids = documents.mapNotNull { it.getString("AssistTaskID") }

                taskOptions = tasks
                taskIDs = ids


                selectedTask = ""
                selectedTaskID = ""

                println("Firestore Tasks: $tasks com IDs: $ids")
            }
            .addOnFailureListener { exception ->
                println("❌ Error: ${exception.message}")
            }
    }

    LaunchedEffect(selectedTaskID) {
        if (selectedTaskID.isBlank() || selectedCategory.isBlank() || patientCancerType.isBlank() || patientGender.isBlank()) return@LaunchedEffect

        println("Searching for volunteers for:")
        println("TaskID: $selectedTaskID")
        println("Category: $selectedCategory")
        println("Cancer Type: $patientCancerType")
        println("Gender: $patientGender")

        db.collection("VolunteeringSurvivorProfilewithAssistTaskCategory")
            .whereEqualTo("AssistTaskID", selectedTaskID)
            .whereEqualTo("AssistTaskCatID", selectedCategory)
            .get()
            .addOnSuccessListener { documents ->
                val volunteerIDs = mutableListOf<String>()
                val profileMap = mutableMapOf<String, String>()

                documents.forEach { doc ->
                    val volunteerID = doc.getString("VolunteerID")
                    val profileID = doc.getString("VolunteerProfileID")
                    if (volunteerID != null && profileID != null) {
                        volunteerIDs.add(volunteerID)
                        profileMap[volunteerID] = profileID
                    }
                }

                if (volunteerIDs.isEmpty()) {
                    println("No Volunteer Appear for this Task")
                    volunteerOptions = listOf("No volunteers available")
                    return@addOnSuccessListener
                }

                db.collection("VolunteeringCancerSurvivor")
                    .whereIn("VolunteerID", volunteerIDs)
                    .whereEqualTo("VolunCancerType", patientCancerType.trim())
                    .whereEqualTo("VolunGender", patientGender.trim())
                    .get()
                    .addOnSuccessListener { volunteerDocs ->
                        val nameToProfile = mutableMapOf<String, String>()
                        val volunteerList = mutableListOf<String>()

                        volunteerDocs.forEach { doc ->
                            val name = doc.getString("VolunteerName") ?: return@forEach
                            val surname = doc.getString("VolunteerSurname") ?: return@forEach
                            val volunteerID = doc.getString("VolunteerID") ?: return@forEach
                            val fullName = "$name $surname"

                            val profileID = profileMap[volunteerID] ?: return@forEach
                            volunteerList.add(fullName)
                            nameToProfile[fullName] = profileID
                        }

                        volunteerOptions = volunteerList
                        volunteerNameToProfileID = nameToProfile
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
                        Text("List of Volunteers for Required Assistance Task", fontSize = 18.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(10.dp))

                        // 🔥 Seleção da Categoria
                        Text("Select Assistance Task Category:", fontSize = 16.sp, color = Color.Black)
                        DropdownComponent(
                            selectedValue = selectedCategory,
                            options = assistanceOptions,
                            onValueChange = { newCategory, _ ->
                                selectedCategory = newCategory
                                selectedTask = ""
                                selectedTaskID = ""
                                selectedVolunteer = ""
                                volunteerOptions = listOf()

                                println("📂 Selected category: $selectedCategory")
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 🔥 Seleção da Task
                        Text("Assistance Task within Selected Category:", fontSize = 16.sp, color = Color.Black)
                        DropdownComponent(
                            selectedValue = selectedTask,
                            options = taskOptions,
                            taskIDs = taskIDs,
                            onValueChange = { newTask, taskID ->
                                selectedTask = newTask
                                selectedTaskID = taskID
                                selectedVolunteer = ""
                                volunteerOptions = listOf()

                                println("🎯 Selected Task: $selectedTask (TaskID: $selectedTaskID)")
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 🔹 Text before the volunteers
                        if (volunteerOptions.isNotEmpty()) {
                            Text(
                                text = "Select Volunteer for the Selected Assistance Task :",
                                fontSize = 16.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))


                        // 🔹 List of volunteers
                        Column {
                            if (volunteerOptions.isEmpty() && selectedTask.isNotBlank()) {
                                Text("No volunteers available", fontSize = 16.sp, color = Color.Red)
                            } else {
                                volunteerOptions.forEach { volunteer ->
                                    val isUnavailable = volunteer == "No volunteers available"

                                    Button(
                                        onClick = {
                                            if (isUnavailable) return@Button // botão desabilitado, não faz nada

                                            selectedVolunteer = volunteer

                                            val userId = auth.currentUser?.uid ?: return@Button

                                            val parts = selectedVolunteer.split(" ")
                                            if (parts.size < 2) return@Button
                                            val selectedVolunteerName = parts[0]
                                            val selectedVolunteerSurname = parts.drop(1).joinToString(" ")

                                            // 🔥 Buscar o ID do voluntário no Firestore
                                            db.collection("VolunteeringCancerSurvivor")
                                                .whereEqualTo("VolunteerName", selectedVolunteerName)
                                                .whereEqualTo("VolunteerSurname", selectedVolunteerSurname)
                                                .get()
                                                .addOnSuccessListener { documents ->
                                                    if (!documents.isEmpty) {
                                                        val selectedVolunteerID = documents.first().id

                                                        // 🔥 Buscar perfil do voluntário
                                                        db.collection("VolunteeringSurvivorProfilewithAssistTaskCategory")
                                                            .whereEqualTo("VolunteerID", selectedVolunteerID)
                                                            .whereEqualTo("AssistTaskID", selectedTaskID)
                                                            .whereEqualTo("AssistTaskCatID", selectedCategory)
                                                            .get()
                                                            .addOnSuccessListener { profileDocs ->
                                                                if (!profileDocs.isEmpty) {
                                                                    val volunteerProfileID = volunteerNameToProfileID[selectedVolunteer] ?: ""

                                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                                        val intent = Intent(context, ViewDetailsSelectedVolunteer::class.java)
                                                                        intent.putExtra("volunteerID", selectedVolunteerID)
                                                                        intent.putExtra("selectedTaskID", selectedTaskID)
                                                                        intent.putExtra("selectedTaskCatID", selectedCategory)
                                                                        intent.putExtra("volunteerProfileID", volunteerProfileID)
                                                                        context.startActivity(intent)
                                                                    }, 2000)
                                                                } else {
                                                                    Toast.makeText(context, "Volunteer Profile not found!", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                            .addOnFailureListener {
                                                                Toast.makeText(context, "Failed to load Volunteer Profile ID", Toast.LENGTH_SHORT).show()
                                                            }
                                                    } else {
                                                        println("❌ Volunteer Not Found!")
                                                    }
                                                }
                                                .addOnFailureListener { exception ->
                                                    println("❌ Error to find a volunteer: ${exception.message}")
                                                }
                                        },
                                        enabled = !isUnavailable,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isUnavailable) Color.LightGray else Color(0xFF4CAF50),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = volunteer,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                    }

                    // 🔥 Componente de Dropdown ÚNICO para Categoria e Task
                    @OptIn(ExperimentalMaterial3Api::class)
                    @Composable
                    fun DropdownComponent(
                        selectedValue: String,
                        options: List<String>,
                        taskIDs: List<String> = listOf(), // 🔥 Apenas necessário para Task Dropdown
                        isTaskDropdown: Boolean = false, // 🔥 Indica se é o dropdown de Tasks
                        onValueChange: (String, String) -> Unit // 🔥 Retorna a opção selecionada e o TaskID (caso seja Task)
                    ) {
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
                                options.forEachIndexed { index, option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            val taskID = if (isTaskDropdown) taskIDs.getOrNull(index) ?: "" else "" // 🔥 Obtém TaskID apenas se for uma Task
                                            onValueChange(option, taskID) // 🔥 Atualiza a UI
                                            expanded = false
                                            println("✅ Selecionado: $option (TaskID: $taskID)")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownComponent(
    selectedValue: String,
    options: List<String>,
    taskIDs: List<String> = listOf(),
    onValueChange: (String, String) -> Unit
) {
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
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        val taskID = taskIDs.getOrNull(index) ?: ""
                        onValueChange(option, taskID)
                        expanded = false
                        println("✅ Selected: $option (TaskID: $taskID)")
                    }
                )
            }
        }
    }
}
