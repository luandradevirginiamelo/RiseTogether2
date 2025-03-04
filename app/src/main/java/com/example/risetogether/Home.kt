package com.example.risetogether

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.ui.theme.*
import com.example.risetogether.ui.theme.isDarkTheme
import com.example.risetogether.ui.theme.ScrollableScreen

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    ScrollableScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Switch para alternar entre modo claro e escuro
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isDarkTheme.value,
                    onCheckedChange = { isDarkTheme.value = it }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isDarkTheme.value) "Dark Mode On" else "Dark Mode Off",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(250.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.weight(1f)) // Buttons UP

            PatientButton(text = "Patient") {
                context.startActivity(Intent(context, PatientLoginScreen::class.java))
            }

            Spacer(modifier = Modifier.height(20.dp))

            VolunteerButton(text = "Volunteer") {
                context.startActivity(Intent(context, VolunteerLoginScreen::class.java))
            }

            Spacer(modifier = Modifier.height(20.dp))

            DoctorButton(text = "Doctor") {
                context.startActivity(Intent(context, DoctorLoginScreen::class.java))
            }

            Spacer(modifier = Modifier.weight(1f)) // Centralized
        }
    }
}
