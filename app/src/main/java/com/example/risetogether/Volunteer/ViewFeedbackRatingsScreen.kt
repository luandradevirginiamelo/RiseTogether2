package com.example.risetogether.Volunteer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.risetogether.R
import com.example.risetogether.VolunteerLoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.apache.http.client.utils.DateUtils.formatDate
import java.text.SimpleDateFormat
import java.util.*

class ViewFeedbackRatingsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ViewFeedbackRatingsContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewFeedbackRatingsContent() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val volunteerId = auth.currentUser?.uid ?: ""

    var selectedWeek by remember { mutableStateOf("") }
    var weekExpanded by remember { mutableStateOf(false) }
    var availableWeeks by remember { mutableStateOf(listOf<String>()) }
    var feedbackList by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var selectedFeedback by remember { mutableStateOf<Map<String, String>?>(null) }

    fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return timestamp?.toDate()?.let { sdf.format(it) } ?: ""
    }

    fun formatTime(timestamp: com.google.firebase.Timestamp?): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timestamp?.toDate()?.let { sdf.format(it) } ?: ""
    }

    // Buscar semanas disponíveis com feedbacks para o voluntário logado
    LaunchedEffect(volunteerId) {
        db.collection("FeedbackRatings")
            .whereEqualTo("VolunteerID", volunteerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val weeks = snapshot.documents.mapNotNull {
                    it.getString("WeekCommenceDate")
                }.distinct()
                availableWeeks = weeks.sorted()
            }
    }

    // Buscar feedbacks da semana selecionada
    fun fetchFeedbacksForWeek(week: String) {
        db.collection("FeedbackRatings")
            .whereEqualTo("VolunteerID", volunteerId)
            .whereEqualTo("WeekCommenceDate", week)
            .get()
            .addOnSuccessListener { docs ->
                val list = docs.map {
                    mapOf(
                        "id" to it.id,
                        "taskName" to (it.getString("AssistTaskName") ?: ""),
                        "date" to (formatDate(it.getTimestamp("FeedbackDateTime"))),
                        "time" to (formatTime(it.getTimestamp("FeedbackDateTime"))),
                        "rating" to it.getLong("RatingStars")?.toString().orEmpty()
                    )
                }
                feedbackList = list
            }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF90CAF9), Color(0xFF81C784), Color(0xFFFFF8E1))
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(MaterialTheme.shapes.small)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            SmallGradientButton("Logout") {
                                auth.signOut()
                                context.startActivity(
                                    Intent(context, VolunteerLoginScreen::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "View List of Feedback and Ratings for Requested Tasks",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )


                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Select Week Commence Date:", fontSize = 16.sp, color = Color.Black)

                        Spacer(modifier = Modifier.height(8.dp))


                        ExposedDropdownMenuBox(
                            expanded = weekExpanded,
                            onExpandedChange = { weekExpanded = !weekExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedWeek,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Week") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = weekExpanded,
                                onDismissRequest = { weekExpanded = false }
                            ) {
                                availableWeeks.forEach { week ->
                                    DropdownMenuItem(
                                        text = { Text(week) },
                                        onClick = {
                                            selectedWeek = week
                                            weekExpanded = false
                                            fetchFeedbacksForWeek(week)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (feedbackList.isNotEmpty()) {
                    val sorted = feedbackList.sortedBy { it["time"] }
                    val earliest = sorted.firstOrNull()
                    val latest = sorted.lastOrNull()

                    if (latest != null) {
                        FeedbackCardStyled("Latest Feedback", latest) {
                            selectedFeedback = latest
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (earliest != null && earliest != latest) {
                        FeedbackCardStyled("Earliest Feedback", earliest) {
                            selectedFeedback = earliest
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                GradientButton("Select Another Week Commence Date") {
                    selectedWeek = ""
                    feedbackList = emptyList()
                }

                Spacer(modifier = Modifier.height(12.dp))

                GradientButton("Back to Dashboard") {
                    context.startActivity(Intent(context, VolunteerDashboardScreen::class.java))
                }
            }



    // POPUP DETAIL
            if (selectedFeedback != null) {
                FeedbackDetailPopup(
                    feedbackId = selectedFeedback!!["id"] ?: "",
                    onClose = { selectedFeedback = null }
                )
            }
        }

    }

}


    @Composable
    fun FeedbackCardStyled(title: String, data: Map<String, String>, onClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            Text("Task Name: ${data["taskName"]}")
            Text("Feedback Date: ${data["date"]}")
            Text("Feedback Time: ${data["time"]}")
            if (data.containsKey("rating")) {
                val rating = data["rating"]?.toIntOrNull() ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(rating) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Yellow
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$rating", color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                Text("View", color = Color.White)
            }
        }
    }

    @Composable
    fun SmallGradientButton(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .height(76.dp)
                .width(105.dp)
                .padding(end = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFF8E1), Color(0xFF81C784))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
@Composable
fun FeedbackDetailPopup(feedbackId: String, onClose: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var feedback by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(feedbackId) {
        db.collection("FeedbackRatings").document(feedbackId).get()
            .addOnSuccessListener { doc ->
                feedback = doc.data
            }
    }

    if (feedback != null) {
        AlertDialog(
            onDismissRequest = onClose,
            confirmButton = {
                Button(onClick = onClose) {
                    Text("Close")
                }
            },
            title = {
                Text("Feedback & Ratings Details")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Week Commence Date: ${feedback?.get("WeekCommenceDate")}")
                    Text("Assistance Task Name: ${feedback?.get("AssistTaskName")}")
                    Text("Volunteer: ${feedback?.get("VolunteerName")}")
                    Text("Day Provided: ${feedback?.get("DayProvided")}")
                    Text("Time Provided: ${feedback?.get("TimeProvided")}")
                    Text("Patient: ${feedback?.get("PatientName")}")
                    Text("Rating: ${feedback?.get("RatingStars")}")
                    Text("Feedback Text:\n${feedback?.get("FeedbackText")}")
                }
            }
        )
    }

}




