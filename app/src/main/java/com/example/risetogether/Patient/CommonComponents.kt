package com.example.risetogether.Patient

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 🔥 Função de Botão Reutilizável
@Composable
fun StyledButton(text: String, isLogout: Boolean = false, onClick: () -> Unit) {
    val gradient = if (isLogout) {
        Brush.linearGradient(colors = listOf(Color(0xFFFFA726), Color.Gray))
    } else {
        Brush.linearGradient(colors = listOf(Color(0xFFFFA726), Color(0xFFFF9800)))
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontSize = 18.sp, color = Color.White)
        }
    }
}

