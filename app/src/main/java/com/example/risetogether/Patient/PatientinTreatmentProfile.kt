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
import androidx.compose.ui.text.style.TextAlign
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
    var patientCancerType by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf("") }
    var selectedTaskID by remember { mutableStateOf("") }

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
    var AssistTaskCatID by remember { mutableStateOf("") }

    // 🔥 Estado para mensagem de erro
    var errorMessage by remember { mutableStateOf<String?>(null) }

    @Composable
    fun SmallGradientButton(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .height(76.dp) // 🔹 Mantém o botão pequeno como no Dashboard
                .width(105.dp) // 🔹 Largura específica do Dashboard
                .padding(end = 8.dp), // 🔹 Adiciona espaçamento para alinhar corretamente
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
                    fontSize = 14.sp, // 🔹 Mesmo tamanho de texto do Dashboard
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // 🔥 Esconde a mensagem de erro após 3 segundos
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            errorMessage = null
        }
    }

    LaunchedEffect(auth.currentUser?.uid) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect

        db.collection("PatientinTreatment").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    patientCancerType = document.getString("PatCancerType") ?: ""
                    patientGender = document.getString("CancerPatientGender") ?: ""
                }
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

    LaunchedEffect(selectedCategory) {
        if (selectedCategory.isBlank()) return@LaunchedEffect

        db.collection("AssistanceTaskCategories")
            .whereEqualTo("AssistTaskCategoryName", selectedCategory)
            .get()
            .addOnSuccessListener { documents ->
                val categoryID = documents.firstOrNull()?.getString("AssistTaskCategoryID") ?: ""
                AssistTaskCatID = categoryID // ✅ Agora temos o ID correto da categoria de assistência

                // 🔥 Exibir no console para depuração
                println("✅ AssistTaskCatID encontrado: $AssistTaskCatID")
            }
            .addOnFailureListener { exception ->
                println("❌ Erro ao buscar AssistTaskCatID: ${exception.message}")
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
            .whereEqualTo("AssistTaskCat", categoryID)
            .get()
            .addOnSuccessListener { documents ->
                val tasks = documents.mapNotNull { it.getString("AssistanceTaskNam") } // Verifique o nome do campo!!
                taskOptions = if (tasks.isNotEmpty()) tasks else listOf("No tasks available")
                selectedTask = taskOptions.firstOrNull() ?: ""
                selectedTaskID = documents.firstOrNull()?.getString("AssistTaskID") ?: ""

                // Debug: Mostrar no Log do Android
                println("🔥 Firestore Find Tasks: $tasks")
            }
            .addOnFailureListener { exception ->
                println("❌ Error no Task Founded: ${exception.message}")
            }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB3E5FC), // 🔹 Azul Claro
                        Color(0xFF42A5F5), // 🔹 Azul Médio
                        Color(0xFF9E9E9E)  // 🔹 Cinza
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent, // 🔥 Isso impede que o Scaffold cubra o fundo azul
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
                    .padding(paddingValues) // 🔥 Aplica padding corretamente!
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 🔥 CONTAINER BRANCO ENVOLVENDO O CONTEÚDO
                Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(12.dp)) // 🔹 Adiciona sombra para destaque
                    .background(Color.White, RoundedCornerShape(12.dp)) // 🔹 Fundo branco arredondado
                    .padding(16.dp) // 🔹 Espaçamento interno
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            Text("Number of Days Selected Assistance Task Required per Week::", fontSize = 16.sp, color = Color.Black)
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
                } // 🔹 FECHA O BOX AQUI!
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
                }
            Spacer(modifier = Modifier.height(10.dp))
                @Composable
                fun GradientButton(text: String, onClick: () -> Unit) {
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(75.dp)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5)) // 🔥 Azul Claro → Azul
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                fontSize = 20.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }


// 🔥 Botão Save com Validação
                GradientButton(text = "Save") {
                    val userId = auth.currentUser?.uid ?: return@GradientButton

                    // 🔥 Validação: Verifica se todos os campos foram preenchidos
                    if (selectedCategory.isBlank() || selectedTask.isBlank() || selectedDays.isBlank()) {
                        errorMessage = "Please fill in all required fields before saving."
                        return@GradientButton
                    }


                    val patientData = hashMapOf(
                        "CancerPatientCategoryTaskProfileID" to db.collection("CancerPatientProfile").document().id, // PK correto
                        "CancerPatientID" to userId, // FK correto
                        "CancerType" to patientCancerType,
                        "CancerPatientGender" to patientGender,
                        "AssistTaskCategoryID" to selectedCategory, // FK correto
                        "AssistTaskID" to selectedTaskID, // ✅ Agora estamos salvando o ID da tarefa, não o nome!
                        "NoOfDaysPerWeek" to selectedDays
                    )

                    db.collection("CancerPatientProfile").add(patientData)
                        .addOnSuccessListener {
                            errorMessage = null
                            Toast.makeText(context, "Patient in Treatment Profile Record Successfully Created!", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error saving data.", Toast.LENGTH_LONG).show()
                        }
                }

                Spacer(modifier = Modifier.height(5.dp))

                GradientButton(text = "Another Assistance Task") {
                    selectedCategory = "" // 🔥 Deixa vazio para só aparecer quando o usuário clicar
                    categoryExpanded = false

                    taskOptions = listOf() // 🔥 Limpa a lista de tarefas
                    selectedTask = "" // 🔥 Deixa vazio para só aparecer quando o usuário clicar
                    taskExpanded = false

                    selectedDays = "" // 🔥 Deixa vazio para só aparecer quando o usuário clicar
                    daysExpanded = false
                }

                Spacer(modifier = Modifier.height(5.dp))

                GradientButton(text = "Back to Dashboard") {
                    context.startActivity(Intent(context, PatientDashboardScreen::class.java))
                }


        }
        }
    }
}


