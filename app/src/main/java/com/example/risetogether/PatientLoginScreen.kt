package com.example.risetogether

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.ui.theme.PatientBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.risetogether.Patient.PatientDashboardScreen
import com.example.risetogether.ui.theme.PatientButton


class PatientLoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatientBackground {
                PatientLoginScreenContent()
            }
        }
    }
}

@Composable
fun PatientLoginScreenContent() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 🔹 Enables scrolling
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 🔹 Logo com fundo branco e sombra
        Box(
            modifier = Modifier
                .shadow(12.dp, RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Text(
            text = "Patient Login",
            style = MaterialTheme.typography.headlineLarge, // Uses Material 3 headline style
            color = Color.Black,
            modifier = Modifier
                .padding(vertical = 16.dp) // Adds breathing space
                .fillMaxWidth(),
            textAlign = TextAlign.Center // Centers the title
        )

        // 🔹 White Container for Fields
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .padding(10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🔹 Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email Icon") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))

                // 🔹 Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))

                // 🔹 Error Message (if login fails)
                errorMessage?.let {
                    Text(text = it, color = Color.Red, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 🔹 Progress Indicator
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 🔹 Login Button (Patient Authentication & Verification)
                PatientButton(
                    text = "Login",
                ) {
                    isLoading = true
                    errorMessage = null

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                                // 🔥 Fetch the Patient record from Firestore
                                db.collection("PatientinTreatment").document(userId).get()
                                    .addOnSuccessListener { document ->
                                        isLoading = false
                                        if (document.exists()) {
                                            val role = document.getString("role") ?: "Unknown"

                                            if (role.lowercase() == "patient") {  // ✅ Ensure it's "Patient"
                                                context.startActivity(Intent(context, PatientDashboardScreen::class.java))
                                            } else {
                                                errorMessage = "❌ Access Denied: Not authorized as a Patient."
                                                auth.signOut()  // 🔒 Log out unauthorized users
                                            }
                                        } else {
                                            errorMessage = "❌ No Patient record found. Please contact support."
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "🔥 Firestore Error: ${e.message}"
                                    }
                            } else {
                                isLoading = false
                                errorMessage = "⚠️ Login Failed: ${task.exception?.localizedMessage}"
                            }
                        }

                }
            }
        }



        Spacer(modifier = Modifier.height(10.dp))

        // 🔹 Forgot Password Link
        Text(
            text = "Forgotten Password? Reset Here",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, ResetPasswordScreen::class.java))
            }
        )
    }
}

// 🔥 Gradient Button
@Composable
fun GradientButton(text: String, gradient: Brush, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(brush = gradient, shape = RoundedCornerShape(12.dp))
    ) {
        Text(text, fontSize = 18.sp, color = Color.Black)

    }
}

