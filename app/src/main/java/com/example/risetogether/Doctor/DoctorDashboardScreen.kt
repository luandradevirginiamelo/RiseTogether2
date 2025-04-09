package com.example.risetogether.Doctor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.R
import com.example.risetogether.ui.theme.DoctorBackground
import com.example.risetogether.ui.theme.DoctorButton

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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // 🔥 App Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 Screen Title
        Text(
            text = "Doctor`s Home (Dashboard) Screen",
            fontSize = 26.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(30.dp))



        // 🔥 Register Vetted Cancer Survivors Button
        DoctorButton(
            text = "Register Patient in Treatment"
        ) {
            context.startActivity(Intent(context, RegisterPatientTreatment::class.java))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 Home Doctor Screen Button
        DoctorButton(
            text = "Register Volunteering Cancer Survivor"
        ) {
            context.startActivity(Intent(context, RegisterVolunteeringCancerSurvivor::class.java))
        }
    }
}


