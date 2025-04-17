package com.example.risetogether.Volunteer

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.PatientLoginScreen
import com.example.risetogether.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.input.key.Key.Companion.Home
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import coil.compose.rememberAsyncImagePainter
import com.example.risetogether.VolunteerLoginScreen

class VolunteerProfileScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolunteerProfileScreenContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolunteerProfileScreenContent() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var volunteerName by remember { mutableStateOf("") }
    var volunteerSurname by remember { mutableStateOf("") }
    var volunteerCancerType by remember { mutableStateOf("") }
    var volunteerGender by remember { mutableStateOf("") }
    var selectedTaskID by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

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
    var showSuccessDialog by remember { mutableStateOf(false) } // ✅ Estado para abrir o pop-up
    var isSaving by remember { mutableStateOf(false) } // ✅ Novo estado para indicar carregamento
    var refreshTrigger by remember { mutableStateOf(0) } //
    // 🔥 Estado para mensagem de erro
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaveEnabled by remember { mutableStateOf(true) }

    // 🔥 Variável para armazenar o ID do perfil do voluntário criado no Firestore
    var volunteerProfileID by remember { mutableStateOf("") }

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
                            colors = listOf(Color(0xFFFFF8E1), Color(0xFF81C784)) // 🔥 Azul Claro → Azul Médio
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

        db.collection("VolunteeringCancerSurvivor").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    volunteerCancerType = document.getString("VolunCancerType") ?: ""
                    volunteerGender = document.getString("VolunGender") ?: ""
                }
            }
    }

    LaunchedEffect(auth.currentUser?.uid) {
        auth.currentUser?.uid?.let { userId ->
            db.collection("VolunteeringCancerSurvivor").document(userId).get()  // ✅ Correct Collection
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        volunteerName = document.getString("VolunteerName") ?: ""
                        volunteerSurname = document.getString("VolunteerSurname") ?: ""  // ✅ Correct Field Name
                    } else {
                        println("🚨 No document found for Volunteer ID: $userId")
                    }
                }
                .addOnFailureListener { exception ->
                    println("❌ Error fetching volunteer data: ${exception.message}")
                }
        }
    }


    var documentList by remember { mutableStateOf(listOf<com.google.firebase.firestore.DocumentSnapshot>()) } // ✅ Store documents

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
                documentList = documents.documents // ✅ Save documents list

                val tasks = documents.mapNotNull { it.getString("AssistanceTaskNam") }
                taskOptions = if (tasks.isNotEmpty()) tasks else listOf("No tasks available")

                selectedTask = "" // 🔥 Keep empty until user clicks
                selectedTaskID = "" // 🔥 Keep empty until user clicks
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
                        Color(0xFF90CAF9), // 🔹 Azul Claro
                        Color(0xFF81C784), // 🔹 Azul Médio
                        Color(0xFFFFF8E1)  // 🔹 Cinza
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

                                    val intent = Intent(context, VolunteerLoginScreen::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

                        Text("Hello, $volunteerName $volunteerSurname", fontSize = 20.sp, color = Color.Black)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            "Please, select an option from each of the following pull-down menus, that best describes the type of assistance you may Provide:",
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

                        // 🔥 Dropdown 3: Number of Days
                        Text("NO of Days/Week Availability for the Selected Assistance Task:", fontSize = 16.sp, color = Color.Black)
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
                                        colors = listOf(Color(0xFFFFF8E1), Color(0xFF81C784)) // 🔥 Azul Claro → Azul
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                fontSize = 15.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }

                // 🎯 Continua igual, sem mudar a lógica
                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    imageUri = uri // Armazena a URI da imagem selecionada
                }

// ✅ Container branco que envolve o botão de upload ou a imagem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (imageUri == null) {
                            // ✅ Antes do upload – mostra texto + botão
                            Text(
                                text = "Upload your recent photo:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp) // Mais colado no botão
                            )

                            GradientButton(text = "Open Phone Gallery") {
                                galleryLauncher.launch("image/*")
                            }
                        } else {
                            // ✅ Depois do upload – mostra imagem selecionada
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUri),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .size(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .shadow(4.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // ✅ Função de upload sem nenhuma mudança
                fun uploadImageToFirebaseStorage(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
                    val uri = imageUri ?: return
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("volunteer_images/${UUID.randomUUID()}.jpg") // Nome aleatório para a imagem

                    storageRef.putFile(uri)
                        .addOnSuccessListener { taskSnapshot ->
                            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                onSuccess(downloadUri.toString()) // Retorna a URL da imagem
                            }
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception)
                        }
                }
                GradientButton(text = if (isSaving) "Saving..." else "Save") {
                    val userId = auth.currentUser?.uid ?: return@GradientButton

                    if (selectedCategory.isBlank()) {
                        errorMessage = "Please select an Assistance Task Category."
                        return@GradientButton
                    }
                    if (selectedTask.isBlank()) {
                        errorMessage = "Please select an Assistance Task."
                        return@GradientButton
                    }
                    if (selectedDays.isBlank()) {
                        errorMessage = "Please select the number of days per week."
                        return@GradientButton
                    }
                    if (imageUri == null) {
                        errorMessage = "Please upload a photo before saving."
                        return@GradientButton
                    }

                    isSaving = true
                    errorMessage = null

                    uploadImageToFirebaseStorage(
                        onSuccess = { imageUrl ->
                            db.collection("AssistanceTaskwithinCategory")
                                .whereEqualTo("AssistTaskID", selectedTaskID)
                                .get()
                                .addOnSuccessListener { taskDocs ->
                                    val assistTaskName = taskDocs.firstOrNull()?.getString("AssistanceTaskNam") ?: ""

                                    val profileRef = db.collection("VolunteeringSurvivorProfilewithAssistTaskCategory").document()
                                    val newVolunteerProfileID = profileRef.id // 🔥 Pegando ID correto

                                    val volunteerData = hashMapOf(
                                        "VolunteerProfileID" to newVolunteerProfileID,
                                        "VolunteerID" to userId,
                                        "AssistTaskCatID" to selectedCategory,
                                        "AssistTaskID" to selectedTaskID,
                                        "AssistTaskNam" to assistTaskName,
                                        "NoOfDaysPerWeek" to selectedDays,
                                        "VolunCancerType" to volunteerCancerType,
                                        "VolunGender" to volunteerGender,
                                        "VolunteerProfilePic" to imageUrl
                                    )

                                    profileRef.set(volunteerData)
                                        .addOnSuccessListener {
                                            isSaving = false
                                            errorMessage = null
                                            showSuccessDialog = true
                                            volunteerProfileID = newVolunteerProfileID // 🔥 Salva o ID para passar pra próxima tela!

                                            Log.d("Firestore", "✅ Volunteer Profile saved: $newVolunteerProfileID")
                                        }
                                        .addOnFailureListener {
                                            isSaving = false
                                            errorMessage = "Error saving data."
                                        }
                                }
                        },
                        onFailure = { exception ->
                            isSaving = false
                            errorMessage = "Failed to upload image: ${exception.message}"
                        }
                    )
                }


// 🔥 Exibir barra de carregamento enquanto os dados estão sendo salvos
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = Color.Blue
                    )
                }

// 🔥 Exibir mensagem de erro na tela, se houver
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
// 🔥 Exibir pop-up de sucesso
                if (showSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = { showSuccessDialog = false },
                        title = { Text("Success", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                        text = {
                            Text("Volunteer's profile record with selected assistance task & category successfully created.\n\nPress 'Continue' to create a profile calendar schedule for that task.")
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSuccessDialog = false

                                    if (volunteerProfileID.isNotEmpty()) {
                                        val intent = Intent(context, CreateProfileCalendarScheduleActivity::class.java)
                                        intent.putExtra("VolunteerProfileID", volunteerProfileID) // ✅ Passando ID corretamente
                                        context.startActivity(intent)
                                    } else {
                                        errorMessage = "Error: Volunteer Profile ID is missing."
                                    }
                                }
                            ) {
                                Text("Continue")
                            }
                        }
                    )
                }

                GradientButton(text = "Another Assistance Task") {
                    selectedCategory = "" // 🔥 Reset category
                    categoryExpanded = false

                    taskOptions = listOf() // 🔥 Clear task list
                    selectedTask = ""
                    selectedTaskID = ""
                    taskExpanded = false

                    selectedDays = "" // 🔥 Reset days selection
                    daysExpanded = false

                    imageUri = null // 🔥 Faz o botão reaparecer

                    refreshTrigger++ // 🔥 Forces the app to reload fresh data next time!
                }

                Spacer(modifier = Modifier.height(5.dp))

                GradientButton(text = "Back to Dashboard") {
                    context.startActivity(Intent(context, VolunteerDashboardScreen::class.java))
                }
            }
        }

    }

}
