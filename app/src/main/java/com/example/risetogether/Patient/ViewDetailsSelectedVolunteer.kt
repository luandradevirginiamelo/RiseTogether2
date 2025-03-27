package com.example.risetogether.Patient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.Date


@Composable
fun GradientButtonFullWidth(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(75.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5)) // Seu gradiente
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

// 🔹 Cor padrão do app
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
    var error by mutableStateOf<String?>(null)
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMondayPicker by remember { mutableStateOf(false) }

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
    val selectedDays = daySelections.filter { it.isSelectedDay }.map { it.dayLabel }
    val day1 = selectedDays.getOrNull(0) ?: ""
    val day2 = selectedDays.getOrNull(1) ?: ""

    // 🔸 Fetch Data
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

    // 🔸 UI Layout (Modal style)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF4FB))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFB3E5FC), BlueMedium))
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Volunteer Details",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
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
                                        Box(
                                            modifier = Modifier
                                                .height(36.dp)
                                                .defaultMinSize(minWidth = 80.dp)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        colors = if (selection.isSelectedDay)
                                                            listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5), Color(0xFF9E9E9E))
                                                        else listOf(Color(0xFF90CAF9), Color(0xFF1976D2))
                                                    ),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    selection.isSelectedDay = !selection.isSelectedDay
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (selection.isSelectedDay) "Selected" else "Select",
                                                color = Color.Black,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp)
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

                                            Box(
                                                modifier = Modifier
                                                    .height(36.dp)
                                                    .defaultMinSize(minWidth = 80.dp)
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = if (selection.isSelectedAM)
                                                                listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5), Color(0xFF9E9E9E))
                                                            else listOf(Color(0xFF90CAF9), Color(0xFF1976D2))
                                                        ),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable(enabled = !selection.isSelectedPM) {
                                                        if (selection.isSelectedAM) {
                                                            selection.isSelectedAM = false
                                                        } else {
                                                            selection.isSelectedAM = true
                                                            selection.isSelectedPM = false
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (selection.isSelectedAM) "Selected" else "Select",
                                                    color = Color.Black,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.padding(horizontal = 12.dp)
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

                                            Box(
                                                modifier = Modifier
                                                    .height(36.dp)
                                                    .defaultMinSize(minWidth = 80.dp)
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = if (selection.isSelectedPM)
                                                                listOf(Color(0xFFB3E5FC), Color(0xFF42A5F5), Color(0xFF9E9E9E))
                                                            else listOf(Color(0xFF90CAF9), Color(0xFF1976D2))
                                                        ),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable(enabled = !selection.isSelectedAM) {
                                                        if (selection.isSelectedPM) {
                                                            selection.isSelectedPM = false
                                                        } else {
                                                            selection.isSelectedPM = true
                                                            selection.isSelectedAM = false
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (selection.isSelectedPM) "Selected" else "Select",
                                                    color = Color.Black,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.padding(horizontal = 12.dp)
                                                )
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }


                    item {
                        Text("Select Week Commencing Date:", fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showMondayPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueMedium)
                        ) {
                            Text(
                                if (selectedDate.isEmpty()) "📅 Calendar:" else "📅 $selectedDate",
                                color = Color.Black
                            )
                        }

                        if (!errorMessage.isNullOrEmpty()) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color.Red,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    item {
                        GradientButtonFullWidth(text = "Place Request for Assistance") {
                            var hasError = false

                            daySelections.forEach { day ->
                                when {
                                    !day.isSelectedDay && (day.isSelectedAM || day.isSelectedPM) -> {
                                        day.error = "Please select the Day"
                                        hasError = true
                                    }
                                    day.isSelectedDay && !day.isSelectedAM && !day.isSelectedPM -> {
                                        day.error = "Select AM or PM Time"
                                        hasError = true
                                    }
                                    else -> {
                                        day.error = null
                                    }
                                }
                            }

                            if (selectedDate.isEmpty()) {
                                errorMessage = "Please select a date"
                                hasError = true
                            } else {
                                errorMessage = null
                            }

                            // ✅ 🔥 Nova validação: Pelo menos um dia + horário
                            val hasAtLeastOneTimeSlot = daySelections.any {
                                it.isSelectedDay && (it.isSelectedAM || it.isSelectedPM)
                            }

                            if (!hasAtLeastOneTimeSlot) {
                                errorMessage = "Please select at least one time slot."
                                hasError = true
                            }

                            if (!hasError) {
                                val selectedAvailability = daySelections.filter {
                                    it.isSelectedDay && (it.isSelectedAM || it.isSelectedPM)
                                }.map {
                                    mapOf(
                                        "Day" to it.dayLabel,
                                        "Time" to if (it.isSelectedAM) "AM" else "PM"
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
                        }

                    GradientButtonFullWidth(
                            text = "Back to List of Volunteers"
                        ) {
                            (context as? ComponentActivity)?.finish()
                        }


                        GradientButtonFullWidth(
                            text = "Back to Dashboard"
                        ) {
                            context.startActivity(Intent(context, PatientDashboardScreen::class.java))
                        }

                    }
                }
            }
        }

        if (showMondayPicker) {
            CustomMondayPicker(
                onDateSelected = { date ->
                    val calendar = Calendar.getInstance().apply { time = date }
                    if (calendar.before(Calendar.getInstance())) {
                        errorMessage = "You cannot select a past date."
                        selectedDate = ""
                    } else {
                        val formatted = "%02d/%02d/%02d".format(
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR) % 100
                        )
                        selectedDate = formatted
                        errorMessage = null
                    }
                    showMondayPicker = false
                },
                onDismiss = { showMondayPicker = false }
            )
        }
    }
}

// 🔹 Modal calendar picker - only Mondays
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMondayPicker(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = utcTimeMillis
                return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
            }
        }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier.wrapContentHeight().fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Monday (Week Commencing Date)", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                DatePicker(state = datePickerState)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(Date(millis))
                        }
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

// 🔹 Read-only input fields
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
