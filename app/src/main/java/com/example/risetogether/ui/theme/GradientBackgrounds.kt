package com.example.risetogether.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 🔶 Fundo para Pacientes (Turquesa → Roxo → Laranja)
@Composable
fun PatientBackground(content: @Composable () -> Unit) {
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
        content()
    }
}

// 🟢 Fundo para Voluntários (Green→ Branco)
@Composable
fun VolunteerBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2ECC71), //
                        Color(0xFFFFFFFF)  //
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


