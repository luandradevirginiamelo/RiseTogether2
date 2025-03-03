package com.example.risetogether

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.ui.theme.VolunteerBackground
import com.example.risetogether.ui.theme.VolunteerButton

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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enables scrolling
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 🔥 App Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(15.dp))

        // 🔥 Screen Title
        Text(
            text = "Volunteer Login Screen",
            fontSize = 22.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(15.dp))

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
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = "Email Icon")
                    },
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

                // 🔥 Error message
                errorMessage?.let {
                    Text(text = it, color = Color.Red, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 🔥 Login Button
                VolunteerButton(
                    text = "Login"
                ) {
                    isLoading = true
                    errorMessage = null

                    android.os.Handler().postDelayed({
                        isLoading = false
                        if (email == "test@example.com" && password == "password123") {
                            context.startActivity(Intent(context, MainActivity::class.java))
                        } else {
                            errorMessage = "Username & Password do not match. Please, try again!"
                        }
                    }, 2000)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 First-Time User Link
        Text(
            text = "SCREEN ?",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, RegisterScreen::class.java))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 🔥 Forgot Password Link
        Text(
            text = "Forgotten Password? Reset Here",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, ResetPasswordVolunteerScreen::class.java))
            }
        )
    }
}


