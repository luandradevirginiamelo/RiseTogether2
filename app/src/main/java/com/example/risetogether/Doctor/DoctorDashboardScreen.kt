package com.example.risetogether.Doctor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.risetogether.R
import com.example.risetogether.Volunteer.ViewFeedbackRatingsScreen
import com.example.risetogether.Volunteer.ViewPatientRequestsScreen
import com.example.risetogether.Volunteer.VolunteerProfileScreen
import com.example.risetogether.VolunteerLoginScreen
import com.example.risetogether.ui.theme.DoctorBackground
import com.example.risetogether.ui.theme.DoctorButton
import com.google.firebase.auth.FirebaseAuth

class DoctorDashboardScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DoctorBackground {
                DoctorDashboardScreenContent()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreenContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFEDE7F6),
                        Color(0xFFB39DDB),
                        Color(0xFF673AB7)
                    )
                )
            )
    ) {
        @Composable
        fun SmallGradientButton(text: String, onClick: () -> Unit) {
            Button(
                onClick = onClick,
                modifier = Modifier
                    .height(76.dp) // 🔹 Small button height
                    .width(105.dp) // 🔹 Small button width
                    .padding(end = 8.dp), // 🔹 Adjusts spacing inside TopAppBar
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFFF8E1), Color(0xFFB39DDB))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = 14.sp, // 🔹 Smaller text size
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Doctor`s Home (Dashboard) Screen",
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                    },
                    navigationIcon = { // 🔹 Menu Icon on the Left
                        IconButton(onClick = { /* TODO: Open Menu */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu), // Replace with your actual icon name
                                contentDescription = "Menu",
                                tint = Color.Black
                            )
                        }
                    },
                    actions = {
                        SmallGradientButton(
                            text = "Logout",
                            onClick = {
                                FirebaseAuth.getInstance().signOut() // logout
                                val intent = Intent(context, DoctorLoginScreen::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                context.startActivity(intent)
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
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()), // 🔹 Keeps vertical scrolling
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // 🔲 Multi-Layered Border Around Logo + Buttons
                Box(
                    modifier = Modifier
                        .border(2.dp, Color(0xFFFFF8E1), RoundedCornerShape(12.dp)) // 🟣 Purple Layer
                        .padding(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 🔹 LOGO
                        Box(
                            modifier = Modifier
                                .shadow(12.dp, RoundedCornerShape(8.dp))
                                .background(Color.White, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 🔥 DASHBOARD TITLE
                        Text(
                            text = "Doctor`s Home (Dashboard) Screen",
                            fontSize = 20.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🔥 BUTTONS (Using Standard PatientButton)
                        DoctorButton(
                            text = "Register Patient in Treatment",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp) // ✅
                        ) {
                            context.startActivity(Intent(context, RegisterPatientTreatment::class.java))

                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        DoctorButton(
                            text = "Register Volunteering Cancer Survivor",

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(2.dp)
                        ) {
                            context.startActivity(Intent(context, RegisterVolunteeringCancerSurvivor::class.java))
                        }

                    }
                }
            }
        }
    }
}

// 🔥 STANDARD BUTTON (PatientButton) FOR ALL SCREENS
@Composable
fun DoctorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFFF8E1), Color(0xFFB39DDB))
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
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}


