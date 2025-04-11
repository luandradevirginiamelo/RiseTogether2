package com.example.risetogether.Doctor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.R
import com.example.risetogether.ui.theme.DoctorButton
import com.example.risetogether.ui.theme.LogoutButton
import com.example.risetogether.ui.theme.RisetogetherTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*

class RegisterVolunteeringCancerSurvivor : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RisetogetherTheme {
                RegisterVolunteerFormContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterVolunteerFormContent(onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFEDE7F6), // Lilás bem claro → Transmite calma e sofisticação
                        Color(0xFFB39DDB), // Roxo suave → Mantém a identidade sem ser pesado
                        Color(0xFF673AB7)  // Roxo profundo → Passa autoridade e confiança
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Register Volunteer", color = Color.Black, fontSize = 16.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu),
                                contentDescription = "Menu",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        LogoutButton(
                            text = "Logout",
                            onClick = {
                                coroutineScope.launch {
                                    context.startActivity(Intent(context, DoctorLoginScreen::class.java))
                                }
                            }
                        )
                    }, // 🔥 Closed 'actions' properly
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)

                ) // 🔥 Closed 'TopAppBar' properly
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(10.dp))
                        .padding(8.dp)
                        .border(2.dp, Color(0xFFB39DDB), RoundedCornerShape(10.dp)) // opcional: contorno
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.Center)
                    )
                }

                // 👇 AQUI ENTRA O TÍTULO ENTRE O LOGO E O FORMULÁRIO
                Text(
                    text = "Register Volunteer in Treatment",
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .border(10.dp, Color(0xFFB39DDB), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        RegisterVolunteerFormContent(context, auth, db, onLogout)
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterVolunteerFormContent(
    context: android.content.Context,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onLogout: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var cancerType by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var registerSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSendButton by remember { mutableStateOf(false) }
    var lastRegisteredVolunteerId by remember { mutableStateOf<String?>(null) }
    var sendButtonEnabled by remember { mutableStateOf(true) }

    var nameError by remember { mutableStateOf(false) }
    var surnameError by remember { mutableStateOf(false) }
    var genderError by remember { mutableStateOf(false) }
    var cancerTypeError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female", "Other")
    val cancerTypes = listOf(
        "Lung Cancer", "Breast Cancer", "Colorectal Cancer", "Prostate Cancer",
        "Skin Cancer", "Stomach Cancer", "Liver Cancer", "Cervical Cancer",
        "Thyroid Cancer", "Esophageal Cancer", "Other"
    )
    val validPrefixes = listOf("083", "085", "086", "087", "089")

    Column {
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = false
            },
            label = { Text("Volunteer`s Name:") },
            isError = nameError
        )
        if (nameError) {
            Text("Volunteer name is required", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = surname,
            onValueChange = {
                surname = it
                surnameError = false
            },
            label = { Text("Volunteer`s  Surname:") },
            isError = surnameError
        )
        if (surnameError) {
            Text("Volunteer surname is required", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuFieldVolunteer("Gender", gender, genderOptions) {
            gender = it
            genderError = false
        }
        if (genderError) {
            Text("Gender is required", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuFieldVolunteer("Cancer Type", cancerType, cancerTypes) {
            cancerType = it
            cancerTypeError = false
        }
        if (cancerTypeError) {
            Text("Cancer type is required", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        val mobileHasError = remember(mobile) {
            mobile.length >= 3 && validPrefixes.none { mobile.startsWith(it) } ||
                    (mobile.length in 8..10 && !isValidIrishPhoneVolunteer(mobile))
        }

        if (mobileHasError) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB39DDB), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "A valid Irish Mobile Number starts with 083, 085, 086, 087, 089 and should be 10 digits in length.",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        OutlinedTextField(
            value = mobile,
            onValueChange = { if (it.length <= 10) mobile = it },
            label = { Text("Volunteer`s  MobileNo:") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = mobileHasError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        val emailHasError = remember(email) {
            email.isNotEmpty() && !isValidEmailVolunteer(email)
        }

        if (emailHasError) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB39DDB), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "A valid email address should contain a valid domain (e.g., gmail.com, outlook.com, hotmail.com, yahoo.com, live.com) and the “@” symbol.",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Volunteer`s Username (email address):") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailHasError,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            label = { Text("Volunteer`s  Initial Password:") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError
        )
        if (passwordError) {
            Text("Initial password is required", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!registerSuccess) {
            DoctorButton(
                text = "Register Volunteer",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                errorMessage = ""

                nameError = name.isBlank()
                surnameError = surname.isBlank()
                genderError = gender.isBlank()
                cancerTypeError = cancerType.isBlank()
                passwordError = password.isBlank()

                val hasEmpty = nameError || surnameError || genderError || cancerTypeError || passwordError
                if (hasEmpty) {
                    errorMessage = "Please fill in all required fields."
                    return@DoctorButton
                }

                if (!isValidIrishPhoneVolunteer(mobile)) {
                    errorMessage = "A valid Irish Mobile Number starts with 083, 085, 086, 087, 089 and should be 10 digits in length."
                } else if (!isValidEmailVolunteer(email)) {
                    errorMessage = "A valid Email Address should contain a valid domain and the @ sign."
                } else {
                    registerVolunteer(
                        name, surname, gender, cancerType, mobile, email, password,
                        auth.currentUser?.uid ?: "UnknownDoctorID",
                        context, auth, db,
                        onSuccess = { uid ->
                            registerSuccess = true
                            showSendButton = true
                            lastRegisteredVolunteerId = uid
                            sendButtonEnabled = true
                        },
                        onError = { errorMessage = it }
                    )
                }
            }
        }

        if (registerSuccess && sendButtonEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD0F0C0), shape = RoundedCornerShape(8.dp)) // Verde claro
                    .padding(12.dp)
            ) {
                Text(
                    "Cancer Volunteer Record Successfully created.\nPress the Send button to send a reset password link.",
                    color = Color(0xFF2E7D32), // Verde escuro
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showSendButton && sendButtonEnabled) {
            DoctorButton(
                text = "Send",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Reset link sent to volunteer.", Toast.LENGTH_SHORT).show()
                        sendButtonEnabled = false
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error sending reset link.", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        if (!sendButtonEnabled && showSendButton) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                DoctorButton("Register Another Volunteer") {
                    name = ""
                    surname = ""
                    gender = ""
                    cancerType = ""
                    mobile = ""
                    email = ""
                    password = ""
                    errorMessage = ""
                    registerSuccess = false
                    showSendButton = false
                    sendButtonEnabled = true
                    lastRegisteredVolunteerId = null
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.width(250.dp)) {
                    DoctorButton(
                        text = "Back to Dashboard",
                        modifier = Modifier.fillMaxWidth(0.87f)
                            .align(Alignment.Center)
                    ) {
                        val intent = Intent(context, DoctorDashboardScreen::class.java)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }
                }
            }
        }
    }
}

// ✅ VALIDATIONS
fun isValidIrishPhoneVolunteer(phone: String): Boolean {
    return phone.matches(Regex("^08[3-9]\\d{7}$"))
}

fun isValidEmailVolunteer(email: String): Boolean {
    val validDomains = listOf("gmail.com", "hotmail.com", "outlook.com", "yahoo.com", "live.com")
    return Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            validDomains.any { email.endsWith(it) } &&
            email.contains("@")
}


fun registerVolunteer(
    name: String,
    surname: String,
    gender: String,
    cancerType: String,
    mobile: String,
    email: String,
    password: String,
    doctorId: String,
    context: android.content.Context,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { authResult ->
            val uid = authResult.user?.uid ?: UUID.randomUUID().toString()
            val volunteer = hashMapOf(
                "VolunteerID" to uid,
                "VolunteerName" to name,
                "VolunteerSurname" to surname,
                "VolunGender" to gender,
                "VolunCancerType" to cancerType,
                "VolunMobile" to mobile,
                "VolunteerUsername" to email,
                "VolunteerPassword" to password,
                "DoctorID" to doctorId,
                "role" to "Volunteer"
            )
            db.collection("VolunteeringCancerSurvivor").document(uid).set(volunteer)
                .addOnSuccessListener { onSuccess(uid) }
                .addOnFailureListener { onError("Failed to save volunteer to database.") }
        }
        .addOnFailureListener {
            onError("Failed to create user: ${it.localizedMessage}")
        }
}

// ✅ DROPDOWN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuFieldVolunteer(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }

        }

    }


}

