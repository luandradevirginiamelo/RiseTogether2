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
                    patientCancerType = document.getString("PatCancerType") ?: ""
                    patientGender = document.getString("CancerPatientGender") ?: ""

                    println("🔥 Paciente encontrado: $patientCancerType, $patientGender")
                } else {
                    println("❌ Paciente não encontrado!")
                }
            }
            .addOnFailureListener { exception ->
                println("❌ Erro ao buscar paciente: ${exception.message}")
            }
    }

    // Fetch Tasks when Category is Selected
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
                val tasks = documents.mapNotNull { it.getString("AssistanceTaskNam") } // 🔥 Certifique-se de que esse é o nome correto no Firestore.
                taskOptions = if (tasks.isNotEmpty()) tasks else listOf("No tasks available")
                selectedTask = taskOptions.firstOrNull() ?: ""
                selectedTaskID = documents.firstOrNull()?.getString("AssistTaskID") ?: ""

                // 🔥 Debugging: Mostrar no Logcat os dados encontrados
                println("🔥 Firestore Tasks Encontradas: $tasks")
            }
            .addOnFailureListener { exception ->
                println("❌ Erro ao buscar tarefas: ${exception.message}")
            }
    }


    // Fetch Volunteers when Task is Selected
// Fetch Volunteers when Task is Selected
    LaunchedEffect(selectedTaskID) {
        if (selectedTaskID.isBlank() || patientCancerType.isBlank() || patientGender.isBlank()) return@LaunchedEffect

        println("🔥 Fetching volunteers for taskID: $selectedTaskID in category: $selectedCategory")
        println("🚀 DEBUG: Categoria selecionada: $selectedCategory")
        println("🚀 DEBUG: Task selecionada: $selectedTask")
        println("🚀 DEBUG: TaskID selecionado: $selectedTaskID")
        println("🚀 DEBUG: Paciente - Cancer Type: $patientCancerType, Gender: $patientGender")
        println("🚀 DEBUG: Tentando buscar voluntários para AssistTaskID: $selectedTaskID na categoria: $selectedCategory")

        db.collection("VolunteeringSurvivorProfilewithAssistTask&Category")
            .whereEqualTo("AssistTaskID", selectedTaskID)
            .whereEqualTo("AssistTaskCategoryID", selectedCategory) // ✅ Nome do campo corrigido
            .get()
            .addOnSuccessListener { documents ->
                val volunteerIDs = documents.mapNotNull { it.getString("VolunteerID") }

                if (volunteerIDs.isEmpty()) {
                    println("❌ ERRO: Nenhum voluntário encontrado para TaskID: $selectedTaskID na categoria $selectedCategory")
                    volunteerOptions = listOf("No volunteers available")
                    return@addOnSuccessListener
                }

                println("🔥 DEBUG: Volunteer IDs encontrados: $volunteerIDs") // ✅ Mostra se achou algum ID

                db.collection("VolunteeringCancerSurvivor")
                    .whereIn("VolunteerID", volunteerIDs)
                    .whereEqualTo("VolunCancerType", patientCancerType.trim()) // ✅ Corrigido para evitar erro de formatação
                    .whereEqualTo("VolunGender", patientGender.trim()) // ✅ Corrigido para evitar erro de formatação
                    .get()
                    .addOnSuccessListener { volunteerDocs ->
                        val volunteers = volunteerDocs.mapNotNull {
                            val name = it.getString("VolunteerName")
                            val surname = it.getString("VolunteerSurname")
                            if (name != null && surname != null) "$name $surname" else null
                        }

                        if (volunteers.isEmpty()) {
                            println("❌ ERRO: Nenhum voluntário atende aos filtros de gênero e câncer!")
                        } else {
                            println("🔥 DEBUG: Voluntários filtrados: $volunteers") // ✅ Verifica se os nomes aparecem
                        }

                        volunteerOptions = if (volunteers.isNotEmpty()) {
                            listOf("Select a volunteer") + volunteers
                        } else {
                            listOf("No volunteers available")
                        }
                    }
                    .addOnFailureListener { exception ->
                        println("❌ ERRO ao buscar voluntários: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                println("❌ ERRO ao buscar IDs de voluntários: ${exception.message}")
            }
    }

    // Save only when a valid volunteer is selected (ignores "Select a volunteer")
    LaunchedEffect(selectedVolunteer) {
        if (selectedVolunteer.isNotBlank() && selectedVolunteer != "Select a volunteer" && selectedVolunteer != "No volunteers available") {
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

                        // Category selection
                        Text("Select Assistance Task Category:", fontSize = 16.sp, color = Color.Black)
                        DropdownComponent(selectedCategory, assistanceOptions) { newCategory ->
                            selectedCategory = newCategory
                            selectedTask = "" // Reset task when changing category
                            selectedVolunteer = "" // Reset volunteer selection
                            volunteerOptions = listOf() // Reset volunteers list
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Task selection
                        Text("Assistance Task within Selected Category:", fontSize = 16.sp, color = Color.Black)
                        DropdownComponent(selectedTask, taskOptions) { newTask ->
                            selectedTask = newTask
                            selectedVolunteer = "" // Reset volunteer selection when task changes
                            volunteerOptions = listOf() // Reset volunteers list
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Volunteer selection
                        Text("Available Volunteers for Selected Task:", fontSize = 16.sp, color = Color.Black)

                        // Ensure volunteer is not auto-selected
                        DropdownComponent(
                            selectedVolunteer,
                            if (volunteerOptions.isEmpty()) listOf("No volunteers available") else listOf("Select a volunteer") + volunteerOptions
                        ) { newSelection ->
                            if (newSelection != "Select a volunteer" && newSelection != "No volunteers available") {
                                selectedVolunteer = newSelection

                                // Show confirmation message
                            Toast.makeText(context, "Volunteer selected: $selectedVolunteer", Toast.LENGTH_SHORT).show()

                            // Wait 2 seconds before opening the details screen
                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(context, ViewDetailsSelectedVolunteer::class.java)
                                intent.putExtra("volunteerName", selectedVolunteer) // Sending the selected volunteer's name
                                context.startActivity(intent) // Opening the details screen

                                // After opening the new screen, reset everything
                                selectedCategory = ""
                                selectedTask = ""
                                selectedVolunteer = ""
                                volunteerOptions = listOf() // Clear volunteers for the next selection
                            }, 2000) // 2000 milliseconds = 2 seconds
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
}@OptIn(ExperimentalMaterial3Api::class)
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
