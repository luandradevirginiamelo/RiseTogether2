package com.example.risetogether.Patient

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


val BlueMedium = Color(0xFF007AFF)

data class VolunteerDayAvailability(
    val dayLabel: String,
    val amTime: String,
    val pmTime: String
)

class DaySelection(val dayLabel: String) {
    var isSelectedDay by mutableStateOf(false)
    var isSelectedAM by mutableStateOf(false)
    var isSelectedPM by mutableStateOf(false)
    var error by mutableStateOf<String?>(null) // <-- AQUI
}



class ViewDetailsSelectedVolunteer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val volunteerID = intent.getStringExtra("volunteerID") ?: ""
        val volunteerProfileID = intent.getStringExtra("volunteerProfileID") ?: ""
        val selectedTaskID = intent.getStringExtra("selectedTaskID") ?: ""
        val selectedTaskCatID = intent.getStringExtra("selectedTaskCatID") ?: ""

        setContent {
            ViewDetailsSelectedVolunteerScreen(
                volunteerID = volunteerID,
                volunteerProfileID = volunteerProfileID,
                selectedTaskID = selectedTaskID,
                selectedTaskCatID = selectedTaskCatID
            )
        }
    }
}

@Composable
fun ViewDetailsSelectedVolunteerScreen(
    volunteerID: String,
    volunteerProfileID: String,
    selectedTaskID: String,
    selectedTaskCatID: String
) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var voluncancertype by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf("") }
    var volunteerNameDisplayed by remember { mutableStateOf("") }

    // Find the selected days
    val daySelections = remember {
        mutableStateListOf(
            DaySelection("Monday"),
            DaySelection("Tuesday"),
            DaySelection("Wednesday"),
            DaySelection("Thursday"),
            DaySelection("Friday")
        )
    }

    val availabilityList = remember { mutableStateListOf<VolunteerDayAvailability>() }

// ✅ Só depois disso você pode fazer o filter/map:
    val selectedDays = daySelections.filter { it.isSelectedDay }.map { it.dayLabel }

    val day1 = selectedDays.getOrNull(0) ?: ""
    val day2 = selectedDays.getOrNull(1) ?: ""
    var selectedDate by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch Volunteer Data from Firestore
    LaunchedEffect(volunteerID) {
        if (volunteerID.isNotBlank()) {
            db.collection("VolunteeringCancerSurvivor")
                .whereEqualTo("VolunteerID", volunteerID)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.first()
                        volunteerNameDisplayed = "${document.getString("VolunteerName") ?: ""} ${document.getString("VolunteerSurname") ?: ""}"
                        email = document.getString("VolunteerUsername") ?: "Not Available"
                        mobile = document.getString("VolunMobile") ?: "Not Available"
                        gender = document.getString("VolunGender") ?: "Not Available"
                        voluncancertype = document.getString("VolunCancerType") ?: "Not Available"

                        db.collection("AssistanceTaskwithinCategory")
                            .whereEqualTo("AssistTaskID", selectedTaskID)
                            .get()
                            .addOnSuccessListener { taskDocs ->
                                selectedTask = taskDocs.firstOrNull()?.getString("AssistanceTaskNam") ?: "Task Not Found"
                            }

                        db.collection("VolunteeringSurvivorAssistTaskCategoryPersonalProfileCalendarSchedule")
                            .whereEqualTo("VolunteerProfileID", volunteerProfileID)
                            .whereEqualTo("AssistTaskID", selectedTaskID)
                            .whereEqualTo("AssistTaskCatID", selectedTaskCatID)
                            .get()
                            .addOnSuccessListener { scheduleDocs ->
                                if (!scheduleDocs.isEmpty) {
                                    val scheduleRaw = scheduleDocs.first().get("Schedule")
                                    val scheduleMap = scheduleRaw as? Map<String, *>
                                    availabilityList.clear()
                                    scheduleMap?.forEach { (_, dayDataAny) ->
                                        val dayData = dayDataAny as? Map<String, *>
                                        val dayLabel = dayData?.get("Day") as? String ?: ""
                                        val am = dayData?.get("AMTime") as? String ?: ""
                                        val pm = dayData?.get("PMTime") as? String ?: ""
                                        if (dayLabel.isNotEmpty()) {
                                            availabilityList.add(VolunteerDayAvailability(dayLabel, am, pm))
                                        }
                                    }
                                }
                            }
                    } else {
                        Toast.makeText(context, "No volunteer found!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to load volunteer details", Toast.LENGTH_SHORT).show()
                }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                "View Details of Selected Volunteer",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF007AFF)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    ReadOnlyField("Name", volunteerNameDisplayed)
                    ReadOnlyField("Email", email)
                    ReadOnlyField("Mobile", mobile)
                    ReadOnlyField("Gender", gender)
                    ReadOnlyField("Cancer Type", voluncancertype)
                    ReadOnlyField("Selected Task", selectedTask)
                }
            }
        }


        item {
            Text("Availability", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        itemsIndexed(availabilityList.take(5)) { index, item ->
            val selection = daySelections[index]

            Column(modifier = Modifier.fillMaxWidth()) {

                // 🔴 Mensagem de erro (caso exista)
                if (!selection.error.isNullOrEmpty()) {
                    Text(
                        text = selection.error ?: "",
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .fillMaxWidth()
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {

                        // DIA + BOTÃO SELECT
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.dayLabel,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    selection.isSelectedDay = !selection.isSelectedDay
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selection.isSelectedDay) BlueMedium else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    if (selection.isSelectedDay) "Selected" else "Select",
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // AM TIME + SELECT (se tiver valor)
                        if (item.amTime.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("AM: ${item.amTime}", modifier = Modifier.weight(1f))

                                Button(
                                    onClick = {
                                        if (selection.isSelectedAM) {
                                            selection.isSelectedAM = false
                                        } else {
                                            selection.isSelectedAM = true
                                            selection.isSelectedPM = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selection.isSelectedAM) BlueMedium else MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.height(36.dp),
                                    enabled = !selection.isSelectedPM
                                ) {
                                    Text(
                                        if (selection.isSelectedAM) "Selected" else "Select",
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // PM TIME + SELECT (se tiver valor)
                        if (item.pmTime.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("PM: ${item.pmTime}", modifier = Modifier.weight(1f))

                                Button(
                                    onClick = {
                                        if (selection.isSelectedPM) {
                                            selection.isSelectedPM = false
                                        } else {
                                            selection.isSelectedPM = true
                                            selection.isSelectedAM = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selection.isSelectedPM) BlueMedium else MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.height(36.dp),
                                    enabled = !selection.isSelectedAM
                                ) {
                                    Text(
                                        if (selection.isSelectedPM) "Selected" else "Select",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        item {
            Spacer(Modifier.height(12.dp))
            Text("Week Commencing Date", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            Column {
                Button(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(selectedYear, selectedMonth, selectedDayOfMonth)
                                }

                                val dayOfWeek = selectedCal.get(Calendar.DAY_OF_WEEK)
                                if (dayOfWeek != Calendar.MONDAY) {
                                    errorMessage = "Please select a Monday only."
                                } else if (selectedCal.before(Calendar.getInstance())) {
                                    errorMessage = "You cannot select a past date."
                                } else {
                                    val formatted = "%02d/%02d/%02d".format(
                                        selectedDayOfMonth,
                                        selectedMonth + 1,
                                        selectedYear % 100
                                    )
                                    selectedDate = formatted
                                    errorMessage = null
                                }
                            },
                            year,
                            month,
                            day
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueMedium)
                ) {
                    Text(
                        if (selectedDate.isEmpty()) "📅 Choose Monday" else "📅 $selectedDate",
                        color = Color.White
                    )
                }

                // Mensagem de erro visível
                if (!errorMessage.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .background(Color(0xFFFFCDD2)) // vermelho clarinho
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFD32F2F), // vermelho mais forte pro texto
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    var hasError = false

                    // Validação dos dias selecionados
                    daySelections.forEach { day ->
                        if (day.isSelectedDay && !day.isSelectedAM && !day.isSelectedPM) {
                            day.error = "Select AM or PM Time"
                            hasError = true
                        } else {
                            day.error = null
                        }
                    }

                    // Validação de data
                    if (selectedDate.isEmpty()) {
                        errorMessage = "Please select a date"
                        hasError = true
                    } else {
                        errorMessage = null
                    }

                    if (!hasError) {
                        val db = FirebaseFirestore.getInstance()

                        val selectedAvailability = daySelections
                            .filter { it.isSelectedDay && (it.isSelectedAM || it.isSelectedPM) }
                            .map {
                                mapOf(
                                    "Day" to it.dayLabel,
                                    "Time" to when {
                                        it.isSelectedAM -> "AM"
                                        it.isSelectedPM -> "PM"
                                        else -> "None"
                                    }
                                )
                            }

                        val requestData = hashMapOf(
                            "VolunteerID" to volunteerID,
                            "SelectedTaskID" to selectedTaskID,
                            "WeekStartDate" to selectedDate,
                            "Day1" to day1,
                            "Day2" to day2,
                            "SelectedAvailability" to selectedAvailability
                        )

                        db.collection("VolunteerRequests")
                            .add(requestData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Request submitted successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(context, SendRequestActivity::class.java)
                                context.startActivity(intent)
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to submit request", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Place Request for Assistance")
            }

            OutlinedButton(
                onClick = {
                    (context as? ComponentActivity)?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Back to List of Volunteers")
            }

            OutlinedButton(
                onClick = {
                    (context as? ComponentActivity)?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Back to Dashboard")
            }
        }

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadOnlyField(label: String, value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        enabled = false,
        readOnly = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color.LightGray,
            disabledLabelColor = Color.Gray,
            disabledPlaceholderColor = Color.Gray
        )
    )
}
