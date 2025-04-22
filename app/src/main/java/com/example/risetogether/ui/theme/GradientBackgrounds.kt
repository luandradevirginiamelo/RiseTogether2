package com.example.risetogether.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 🔹 Background for Patients (Light Blue → Blue → Purple)
@Composable
fun PatientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB3E5FC),
                        Color(0xFF64B5F6),
                        Color(0xFFA7E6E2)
                    )
                )
            )
    ) {
        content()
    }
}

// 🔹 Background for Volunteers (Blue → White)
@Composable
fun VolunteerBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF90CAF9),
                        Color(0xFF81C784),
                        Color(0xFFFFF8E1)
                    )
                )
            )
    ) {
        content()
    }
}

// 🟣 Fundo para Doutores (Branco → Roxo)
@Composable
fun DoctorBackground(content: @Composable () -> Unit) {
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
        content()
    }
}


