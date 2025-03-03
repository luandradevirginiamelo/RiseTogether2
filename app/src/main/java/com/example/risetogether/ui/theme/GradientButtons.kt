package com.example.risetogether.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding

// 🔶 Botão Elevado para Pacientes (Gradiente Laranja)
@Composable
fun PatientButton(text: String, onClick: () -> Unit) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFFFA726), Color(0xFFFF9800)) // Tons de laranja
        ),
        onClick = onClick
    )
}

// 🟢 Botão Elevado para Voluntários (Gradiente Verde)
@Composable
fun VolunteerButton(text: String, onClick: () -> Unit) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFF66BB6A), Color(0xFF43A047)) // Tons de verde
        ),
        onClick = onClick
    )
}

// 🟣 Botão Elevado para Doutores (Gradiente Roxo)
@Composable
fun DoctorButton(text: String, onClick: () -> Unit) {
    ElevatedGradientButton(
        text = text,
        gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFAB47BC), Color(0xFF8E24AA)) // Tons de roxo
        ),
        onClick = onClick
    )
}

// 🔥 Componente Base para Criar Botões Elevados com Gradiente
@Composable
fun ElevatedGradientButton(text: String, gradient: Brush, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.elevatedButtonColors(containerColor = Color.Transparent), // Deixa o fundo transparente para o gradiente
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(50.dp)
            .background(brush = gradient, shape = RoundedCornerShape(12.dp)) // Aplica o gradiente
            .padding(4.dp) // Ajuste para suavizar o visual
    ) {
        Text(text, fontSize = 18.sp, color = Color.White)
    }
}
