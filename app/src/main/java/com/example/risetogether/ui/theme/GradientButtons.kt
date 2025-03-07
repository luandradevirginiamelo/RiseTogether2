package com.example.risetogether.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight

// 🔹 Button for Patients (Light Blue → Blue)
@Composable
fun PatientButton(text: String, onClick: () -> Unit) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5)) // Light Blue → Blue
        ),
        onClick = onClick
    )
}

// 🔹 Button for Volunteers (Blue → White)
@Composable
fun VolunteerButton(text: String, onClick: () -> Unit) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF42A5F5), Color(0xFF00C9A7)) // Blue → White
        ),
        onClick = onClick
    )
}

// 🔹 Button for Doctors (Blue → Purple)
@Composable
fun DoctorButton(text: String, onClick: () -> Unit) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF42A5F5), Color(0xFF7E57C2)) // Blue → Purple
        ),
        onClick = onClick
    )
}

// ✨ Base Button with Gradient
@Composable
fun ElevatedGradientButton(text: String, gradient: Brush, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp), // Softer rounded edges
        colors = ButtonDefaults.elevatedButtonColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth(0.85f) // Slightly wider for better presence
            .height(52.dp) // Increased height for better usability
            .background(brush = gradient, shape = RoundedCornerShape(14.dp)) // Gradient effect
            .padding(4.dp)
    ) {
        Text(text, fontSize = 19.sp, color = Color.Black, fontWeight = FontWeight.Bold )
    }
}


