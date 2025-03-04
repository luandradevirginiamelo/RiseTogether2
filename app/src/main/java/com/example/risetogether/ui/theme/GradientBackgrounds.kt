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
                        Color(0xFFB3E5FC), // Light Blue
                        Color(0xFF42A5F5), // Blue
                        Color(0xFF9E9E9E)  // Purple
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
                        Color(0xFF42A5F5), // Blue
                        Color(0xFFFFFFFF), // White
                        Color(0xFF00C9A7)  // White
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
                        Color(0xFFFFFFFF), // Branco
                        Color(0xFF8E24AA)  // Roxo
                    )
                )
            )
    ) {
        content()
    }
}


