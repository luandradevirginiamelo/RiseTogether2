
package com.example.risetogether.Patient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

class SendRequestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SendRequestScreen()
        }
    }
}

@Composable
fun SendRequestScreen() {
    // Aqui você pode montar a UI da janela 1.5B
    Text("You are in Window 1.5B: Send Request for Selected Assistance Task")
}


