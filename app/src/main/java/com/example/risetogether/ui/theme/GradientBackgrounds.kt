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
                        Color(0xFFB3E5FC), // Azul bebê → Passa leveza e serenidade
                        Color(0xFF64B5F6), // Azul médio → Mantém a confiança sem ser forte demais
                        Color(0xFFA7E6E2)  // Verde água → Tranquilidade e sensação de frescor
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
                        Color(0xFF90CAF9), // Azul bebê → Mantém a confiança, mas com um tom mais suave
                        Color(0xFF81C784), // Verde menta → Mais leve, simboliza esperança e renovação
                        Color(0xFFFFF8E1)  // Off-white amarelado → Menos agressivo que o branco puro, mais acolhedor
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
                        Color(0xFFEDE7F6), // Lilás bem claro → Transmite calma e sofisticação
                        Color(0xFFB39DDB), // Roxo suave → Mantém a identidade sem ser pesado
                        Color(0xFF673AB7)  // Roxo profundo → Passa autoridade e confiança
                    )
                )
            )
    ) {
        content()
    }
}


