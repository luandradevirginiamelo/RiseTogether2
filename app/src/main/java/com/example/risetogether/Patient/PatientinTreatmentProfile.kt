package com.example.risetogether.Patient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.PatientLoginScreen
import com.example.risetogether.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PatientinTreatmentProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatientinTreatmentProfileContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientinTreatmentProfileContent() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var patientName by remember { mutableStateOf("") }
    var patientSurname by remember { mutableStateOf("") }

    // 🔥 Assistance Categories (Static List)
    val assistanceOptions = listOf("Household", "Garden", "Transportation")
    var selectedCategory by remember { mutableStateOf("") } // Começa vazio
    var categoryExpanded by remember { mutableStateOf(false) }

    // 🔥 Tasks within Selected Category (Dynamic List)
    var taskOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedTask by remember { mutableStateOf("") } // Começa vazio
    var taskExpanded by remember { mutableStateOf(false) }

    val daysOptions = listOf("1", "2", "3", "4", "5")
    var selectedDays by remember { mutableStateOf("") } // Começa vazio
    var daysExpanded by remember { mutableStateOf(false) }

    // 🔥 Estado para mensagem de erro
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 🔥 Esconde a mensagem de erro após 3 segundos
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            errorMessage = null
        }
    }

    // 🔥 Fetch Patient Data (Existing)
    LaunchedEffect(auth.currentUser?.uid) {
        auth.currentUser?.uid?.let { userId ->
            db.collection("PatientinTreatment").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        patientName = document.getString("CancerPatientName") ?: ""
                        patientSurname = document.getString("CancerPatientSurname") ?: ""
                    }
                }
        }
    }

    // 🔥 Fetch Tasks when Category is Selected
    LaunchedEffect(selectedCategory) {
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
                val tasks = documents.mapNotNull { it.getString("AssistanceTaskNam") } // Verifique o nome do campo!!
                taskOptions = tasks
                selectedTask = if (tasks.isNotEmpty()) tasks[0] else "Nenhuma tarefa encontrada"

                // Debug: Mostrar no Log do Android
                println("🔥 Firestore encontrou tarefas: $tasks")
            }
            .addOnFailureListener { exception ->
                println("❌ Erro ao buscar tarefas: ${exception.message}")
            }
    }

    Scaffold(
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
                        Button(
                            onClick = {
                                auth.signOut()
                                context.startActivity(Intent(context, PatientLoginScreen::class.java))
                            },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                        ) {
                            Text("Logout", color = Color.White, fontSize = 14.sp)
                        }
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

            Text("Create Profile Record with Required Assistance", fontSize = 18.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))
            Text("Hello, $patientName $patientSurname", fontSize = 20.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Please, select an option from each of the following pull-down menus, that best describes the type of assistance you may require:",
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 🔥 Dropdown 1: Assistance Task Category
            Text("Select Assistance Task Category:", fontSize = 16.sp, color = Color.Black)
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    assistanceOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedCategory = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔥 Dropdown 2: Assistance Task within Selected Category
            Text("Assistance Task within Selected Category:", fontSize = 16.sp, color = Color.Black)
            ExposedDropdownMenuBox(
                expanded = taskExpanded,
                onExpandedChange = { taskExpanded = !taskExpanded }
            ) {
                OutlinedTextField(
                    value = selectedTask,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = taskExpanded,
                    onDismissRequest = { taskExpanded = false }
                ) {
                    taskOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedTask = option
                                taskExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔥 Dropdown 3: Number of Days
            Text("Number of Days for Selected Assistance Task:", fontSize = 16.sp, color = Color.Black)
            ExposedDropdownMenuBox(
                expanded = daysExpanded,
                onExpandedChange = { daysExpanded = !daysExpanded }
            ) {
                OutlinedTextField(
                    value = selectedDays,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = daysExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = daysExpanded,
                    onDismissRequest = { daysExpanded = false }
                ) {
                    daysOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedDays = option
                                daysExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔥 Exibir mensagem de erro no centro da tela
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 🔥 Botão Save com Validação
            StyledButton("Save") {
                val userId = auth.currentUser?.uid ?: return@StyledButton

                // 🔥 Validação: Verifica se todos os campos foram preenchidos
                if (selectedCategory.isBlank() || selectedTask.isBlank() || selectedDays.isBlank()) {
                    errorMessage = "Please fill in all required fields before saving."
                    return@StyledButton
                }

                val patientData = hashMapOf(
                    "CancerPatientCategoryTaskProfileID" to db.collection("CancerPatientProfile").document().id, // ID automático
                    "CancerPatientID" to userId,
                    "CancerType" to "Lung Cancer",
                    "CancerPatientGender" to "Male",
                    "AssistTaskCategoryID" to selectedCategory,
                    "AssistTaskID" to selectedTask,
                    "NoOfDaysPerWeek" to selectedDays
                )

                db.collection("CancerPatientProfile").add(patientData)
                    .addOnSuccessListener {
                        errorMessage = null
                        Toast.makeText(context, "Patient in Treatment Profile Record Successfully Created!", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        errorMessage = "Error saving data."
                    }
            }

            Spacer(modifier = Modifier.height(8.dp))

            StyledButton("Another Assistance Task") {
                selectedCategory = "" // 🔥 Deixa vazio para só aparecer quando o usuário clicar
                categoryExpanded = false

                taskOptions = listOf() // 🔥 Limpa a lista de tarefas
                selectedTask = "" // 🔥 Deixa vazio para só aparecer quando o usuário clicar
                taskExpanded = false

                selectedDays = "" // 🔥 Deixa vazio para só aparecer quando o usuário clicar
                daysExpanded = false
            }

            Spacer(modifier = Modifier.height(8.dp))

            StyledButton("Back to Dashboard") {
                context.startActivity(Intent(context, PatientDashboardScreen::class.java))
            }


        }
        }
    }


