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

    //  Assistance Categories (Static List)
    val assistanceOptions = listOf("Household", "Garden", "Transportation")
    var selectedCategory by remember { mutableStateOf("") } // Começa vazio
    var categoryExpanded by remember { mutableStateOf(false) }


    var taskOptions by remember { mutableStateOf(listOf<String>()) }
    var selectedTask by remember { mutableStateOf("") } // Começa vazio
    var taskExpanded by remember { mutableStateOf(false) }

    val daysOptions = listOf("1", "2", "3", "4", "5")
    var selectedDays by remember { mutableStateOf("") } // Começa vazio
    var daysExpanded by remember { mutableStateOf(false) }
    var AssistTaskCatID by remember { mutableStateOf("") }
    var isSaveEnabled by remember { mutableStateOf(true) }


    var errorMessage by remember { mutableStateOf<String?>(null) }

    @Composable
    fun SmallGradientButton(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .height(76.dp)
                .width(105.dp)
                .padding(end = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.LightGray
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5))
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

    //  Fetch Patient Data (Existing)
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

    var documentList by remember { mutableStateOf(listOf<com.google.firebase.firestore.DocumentSnapshot>()) }

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
                documentList = documents.documents

                val tasks = documents.mapNotNull { it.getString("AssistanceTaskNam") }
                taskOptions = if (tasks.isNotEmpty()) tasks else listOf("No tasks available")

                selectedTask = taskOptions.firstOrNull() ?: ""
                selectedTaskID = documents.firstOrNull()?.getString("AssistTaskID") ?: ""
            }
            .addOnFailureListener { exception ->
                println("❌ Error fetching tasks: ${exception.message}")
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

                        //  Dropdown 1: Assistance Task Category
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

                        //  Dropdown 2: Assistance Task within Selected Category
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
                                taskOptions.forEachIndexed { index, option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedTask = option
                                            selectedTaskID = documentList.getOrNull(index)?.getString("AssistTaskID") ?: "" // ✅ Update Task ID
                                            taskExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        //  Dropdown 3: Number of Days
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
                    }
                    Spacer(modifier = Modifier.height(8.dp))


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
                fun GradientButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
                    Button(
                        onClick = onClick,
                        enabled = enabled,
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
                                    if (enabled) {
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5))
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            colors = listOf(Color.LightGray, Color.Gray) // 👈 degrade cinza claro
                                        )
                                    },
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


                GradientButton(text = "Save", enabled = isSaveEnabled) {
                    val userId = auth.currentUser?.uid ?: return@GradientButton


                    if (selectedCategory.isBlank() || selectedTask.isBlank() || selectedDays.isBlank()) {
                        errorMessage = "Please fill in all required fields before saving."
                        return@GradientButton
                    }


                    val patientData = hashMapOf(
                        "CancerPatientCategoryTaskProfileID" to db.collection("CancerPatientProfile").document().id, //PK
                        "CancerPatientID" to userId, // FK
                        "PatCancerType" to patientCancerType,
                        "CancerPatientGender" to patientGender,
                        "AssistTaskCatID" to selectedCategory, // FK
                        "AssistTaskID" to selectedTaskID,
                        "NoOfDaysPerWeek" to selectedDays
                    )

                    db.collection("CancerPatientProfile").add(patientData)
                        .addOnSuccessListener {
                            errorMessage = null
                            Toast.makeText(context, "Patient in Treatment Profile Record Successfully Created!", Toast.LENGTH_LONG).show()
                            isSaveEnabled = false // 👈 desativa o botão após salvar com sucesso
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error saving data.", Toast.LENGTH_LONG).show()
                        }
                }

                Spacer(modifier = Modifier.height(5.dp))

                GradientButton(text = "Another Assistance Task") {
                    isSaveEnabled = true
                    selectedCategory = ""
                    categoryExpanded = false

                    taskOptions = listOf()
                    selectedTask = ""
                    taskExpanded = false

                    selectedDays = ""
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


