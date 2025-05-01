package com.example.risetogether

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.ui.theme.PatientBackground
import com.example.risetogether.ui.theme.PatientButton
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatientBackground { // 🔥 Keeping the same background as the login screen
                ResetPasswordScreenContent()
            }
        }
    }
}

@Composable
fun ResetPasswordScreenContent() {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enables scrolling
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 🔥 App Logo (same size as the login screen)
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(15.dp))

        // 🔥 Screen Title / Description
        Text(
            text = "Reset Your Password",
            fontSize = 22.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(15.dp))

        // 🔥 White Container for the Email Field
        Card(
            modifier = Modifier
                .fillMaxWidth(0.97f) // Same width as the login screen
                .padding(10.dp), // Adjusting spacing
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🔥 Email Input Field with Icon
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail (as per registration)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = "Email Icon")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "Send Reset Password Link" Button
        PatientButton(
            text = "Send Reset Password"
        ) {
            if (email.isNotEmpty()) {
                val auth = FirebaseAuth.getInstance()
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            message = "A reset password link has been sent to $email"
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            context.startActivity(Intent(context, PatientLoginScreen::class.java))
                        } else {
                            message = "Failed to send reset email. Check the email address."
                        }
                    }
            } else {
                message = "Please enter a valid email address!"
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 🔥 "Back to Login" Button (Below the Reset Button)
        PatientButton(
            text = "Back to Login"
        ) {
            context.startActivity(Intent(context, PatientLoginScreen::class.java)) // Redirects to Login
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 Success/Error Message
        message?.let {
            Text(
                text = it,
                color = if (it.contains("sent")) Color.Green else Color.Red,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
