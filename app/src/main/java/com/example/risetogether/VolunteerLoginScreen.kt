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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.Volunteer.VolunteerDashboardScreen
import com.example.risetogether.ui.theme.VolunteerBackground
import com.example.risetogether.ui.theme.VolunteerButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context

class VolunteerLoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VolunteerBackground {
                VolunteerLoginScreenContent()
            }
        }
    }
}

@Composable
fun VolunteerLoginScreenContent() {
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 🔹 Logo
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
            text = "Volunteer Login",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Black,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 🔥 White Container
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
                // 🔥 Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email Icon") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))

                // 🔥 Password Input
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

                // 🔥 Error Message
                errorMessage?.let {
                    Text(text = it, color = Color.Red, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 🔥 Progress Indicator
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 🔥 Login Button
                VolunteerButton(text = "Login") {
                    isLoading = true
                    errorMessage = null

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                                db.collection("VolunteeringCancerSurvivor").document(userId)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        isLoading = false
                                        if (document.exists()) {
                                            val role = document.getString("role") ?: "Unknown"

                                            if (role.lowercase() == "volunteer") {
                                                val volunteerID = document.getString("VolunteerID") ?: ""
                                                val volunteerGender = document.getString("VolunGender") ?: ""
                                                val volunteerCancerType = document.getString("VolunCancerType") ?: ""

                                                // Store Volunteer Data in SharedPreferences
                                                val sharedPref = context.getSharedPreferences("VolunteerData", Context.MODE_PRIVATE)
                                                with(sharedPref.edit()) {
                                                    putString("VolunteerID", volunteerID)
                                                    putString("VolunteerGender", volunteerGender)
                                                    putString("VolunCancerType", volunteerCancerType)
                                                    apply()
                                                }

                                                // ✅ Redirect to Dashboard
                                                context.startActivity(Intent(context, VolunteerDashboardScreen::class.java))
                                            } else {
                                                errorMessage = "❌ Access Denied: You are not a Volunteer."
                                                auth.signOut()
                                            }
                                        } else {
                                            errorMessage = "❌ No Volunteer record found. Please contact support."
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

                Spacer(modifier = Modifier.height(20.dp))

                // 🔥 Forgot Password Link
                Text(
                    text = "Re-set Password Here",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, ResetPasswordVolunteerScreen::class.java))
                    }
                )
            }
        }
    }
}

