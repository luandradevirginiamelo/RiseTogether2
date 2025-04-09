package com.example.risetogether.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 🔹 Botão base com gradiente
@Composable
fun ElevatedGradientButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.87f)
            .height(54.dp)
            .background(brush = gradient, shape = RoundedCornerShape(14.dp))
    ) {
        ElevatedButton(
            onClick = onClick,
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.elevatedButtonColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 🔹 Button for Patients (Light Blue → Blue)
@Composable
fun PatientButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFB3E5FC), Color(0xFF64B5F6))
        ),
        onClick = onClick,
        modifier = modifier
    )
}

// 🔹 Button for Volunteers (Green → Light Blue)
@Composable
fun VolunteerButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF90CAF9), Color(0xFF81C784))
        ),
        onClick = onClick,
        modifier = modifier
    )
}

// 🔹 Button for Doctors (Lilac → Purple)
@Composable
fun DoctorButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFEDE7F6), Color(0xFFB39DDB))
        ),
        onClick = onClick,
        modifier = modifier
    )
}

// 🔹 Small Logout Button
@Composable
fun LogoutButton(
    text: String = "Logout",
    onClick: () -> Unit
) {
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
                        colors = listOf(Color(0xFFEDE7F6), Color(0xFFB39DDB)) // Light Blue → Blue
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






