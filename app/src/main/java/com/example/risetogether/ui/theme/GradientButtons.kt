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
fun PatientButton(
    text: String,
    modifier: Modifier = Modifier, // ✅ O Modifier precisa estar aqui
    onClick: () -> Unit
) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFB3E5FC), Color(0xFF64B5F6))
        ),
        onClick = onClick,
        modifier = modifier // ✅ O Modifier precisa ser passado para ElevatedGradientButton
    )
}



// 🔹 Button for Volunteers (Blue → White)
@Composable
fun VolunteerButton(text: String, onClick: () -> Unit) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF90CAF9), Color(0xFF81C784)) // Blue → White
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
            colors = listOf(Color(0xFFEDE7F6), Color(0xFFB39DDB)) // Blue → Purple
        ),
        onClick = onClick
    )
}

// ✨ Base Button with Gradient
@Composable
fun ElevatedGradientButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // ✅ Precisa estar aqui!
) {
    Box( // ✅ Usamos Box para permitir aplicar o Modifier corretamente
        modifier = modifier
            .fillMaxWidth(0.85f)
            .height(52.dp)
            .background(brush = gradient, shape = RoundedCornerShape(14.dp))
    ) {
        ElevatedButton(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.elevatedButtonColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxSize() // ✅ Permite que o botão ocupe toda a Box
        ) {
            Text(
                text = text,
                fontSize = 19.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}






