package com.example.risetogether.Patient

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.PatientLoginScreen
import com.example.risetogether.R
import kotlinx.coroutines.launch
import com.example.risetogether.Patient.PatientinTreatmentProfile

class PatientDashboardScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatientDashboardScreenContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDashboardScreenContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 🔥 Adicionando Background direto na tela
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF40E0D0), // Turquesa
                        Color(0xFF8E24AA), // Roxo
                        Color(0xFFFFA726)  // Laranja
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Patient Dashboard", color = Color.White, fontSize = 25.sp) },
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            PatientStyledLogoutButton("Logout") {
                                coroutineScope.launch {
                                    context.startActivity(Intent(context, PatientLoginScreen::class.java))
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFFFF))
                )
            },
            containerColor = Color.Transparent // 🔥 Deixa o Scaffold transparente para o background aparecer
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

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

                Spacer(modifier = Modifier.height(30.dp))

                // 🔥 Título do Dashboard
                Text(
                    text = "Patient in Treatment Home",
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                // 🔥 Botões do Dashboard
                PatientStyledButton("Create Profile With Required Assistance Tasks") {
                    context.startActivity(Intent(context, PatientinTreatmentProfile::class.java))
                }

                PatientStyledButton("View Volunteers for Required Task and Send Request") {
                    context.startActivity(Intent(context, ViewListVolunteersScreen::class.java))
                }

                PatientStyledButton("View Notifications & Reminders for Requested Task") {
                    context.startActivity(Intent(context, NotificationsScreen::class.java))
                }

                PatientStyledButton("Provide Feedback & Ratings for Received Assistance") {
                    context.startActivity(Intent(context, FeedbackScreen::class.java))
                }
            }
        }
    }
}

// 🔥 Botão estilizado para os pacientes
@Composable
fun PatientStyledButton(text: String, onClick: () -> Unit) {
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
                        colors = listOf(Color(0xFFFF9800), Color(0xFFF57C00))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

// 🔥 Botão de Logout maior para caber o texto corretamente
@Composable
fun PatientStyledLogoutButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(start = 12.dp)
            .width(140.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFF9800), Color(0xFFF57C00))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
