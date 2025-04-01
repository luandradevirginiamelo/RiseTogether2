package com.example.risetogether.Patient

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    var volunteerProfileScheduleID by remember { mutableStateOf("") }
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
                                    val scheduleDoc = scheduleDocs.first()
                                    val scheduleRaw = scheduleDoc.get("Schedule")
                                    volunteerProfileScheduleID = scheduleDoc.getString("VolunteerCalendarProfileScheduleID") ?: ""
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
                                ReadOnlyField("MobileNo", mobile)
                                ReadOnlyField("Gender", gender)
                                ReadOnlyField("Cancer Type", voluncancertype)
                                ReadOnlyField("Selected Task for Assistance", selectedTask)
                            }
                        }
                    }

                    item {
                        Text("Volunteers' Availability by Day of the Week", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    itemsIndexed(availabilityList.take(5)) { index, item ->
                        val selection = daySelections[index]

                        availabilityList.sortBy {
                            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday").indexOf(it.dayLabel)
                        }

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

                                                    // ⚠️ Desmarcar AM/PM se o dia for desmarcado
                                                    if (!selection.isSelectedDay) {
                                                        selection.isSelectedAM = false
                                                        selection.isSelectedPM = false
                                                    }
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
                                            Text("Time AM: ${item.amTime}", modifier = Modifier.weight(1f))

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
                                                        selection.isSelectedAM = !selection.isSelectedAM

                                                        if (selection.isSelectedAM) {
                                                            selection.isSelectedPM = false
                                                            selection.isSelectedDay = true // ✅ Força marcar o dia também
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
                                            Text("Time PM: ${item.pmTime}", modifier = Modifier.weight(1f))

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
                                                        selection.isSelectedPM = !selection.isSelectedPM

                                                        if (selection.isSelectedPM) {
                                                            selection.isSelectedAM = false
                                                            selection.isSelectedDay = true // ✅ Força marcar o dia também
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
                            data class SelectedDayData(
                                val dayLabel: String,
                                val isAM: Boolean,
                                val isPM: Boolean,
                                val time: String
                            )

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
                                    else -> day.error = null
                                }
                            }

                            if (selectedDate.isEmpty()) {
                                errorMessage = "Please select a date"
                                hasError = true
                            } else {
                                errorMessage = null
                            }

                            // 🔥 Mapeia seleção + disponibilidade corretamente
                            val selectedDayDataList = availabilityList.zip(daySelections)
                                .filter { it.second.isSelectedDay && (it.second.isSelectedAM || it.second.isSelectedPM) }
                                .map { (availability, selection) ->
                                    val selectedTime = when {
                                        selection.isSelectedAM -> availability.amTime
                                        selection.isSelectedPM -> availability.pmTime
                                        else -> ""
                                    }

                                    SelectedDayData(
                                        dayLabel = availability.dayLabel,
                                        isAM = selection.isSelectedAM,
                                        isPM = selection.isSelectedPM,
                                        time = selectedTime
                                    )
                                }

                            val day1Data = selectedDayDataList.getOrNull(0)
                            val day2Data = selectedDayDataList.getOrNull(1)

                            // ✅ Logs pra garantir
                            Log.d("SELECTED_INTENT", "DAY1 = ${day1Data?.dayLabel} | TIME=${day1Data?.time} | AM=${day1Data?.isAM} | PM=${day1Data?.isPM}")
                            Log.d("SELECTED_INTENT", "DAY2 = ${day2Data?.dayLabel} | TIME=${day2Data?.time} | AM=${day2Data?.isAM} | PM=${day2Data?.isPM}")

                            if (!hasError) {
                                val intent = Intent(context, SendRequestSelectedAssistanceTask::class.java)
                                intent.putExtra("taskId", selectedTaskID)
                                intent.putExtra("volunteerId", volunteerID)
                                intent.putExtra("weekCommenceDate", selectedDate)
                                intent.putExtra("volunteerName", volunteerNameDisplayed.split(" ").firstOrNull() ?: "")
                                intent.putExtra("volunteerSurname", volunteerNameDisplayed.split(" ").getOrNull(1) ?: "")
                                intent.putExtra("assistTaskName", selectedTask)
                                intent.putExtra("volunteerProfileID", volunteerProfileID)
                                intent.putExtra("volunteerProfileScheduleID", volunteerProfileScheduleID)
                                intent.putExtra("selectedTaskCatID", selectedTaskCatID)

                                // ✅ Dias e horários reais
                                intent.putExtra("day1", day1Data?.dayLabel ?: "")
                                intent.putExtra("day2", day2Data?.dayLabel ?: "")
                                intent.putExtra("day1AM", day1Data?.isAM ?: false)
                                intent.putExtra("day1PM", day1Data?.isPM ?: false)
                                intent.putExtra("day2AM", day2Data?.isAM ?: false)
                                intent.putExtra("day2PM", day2Data?.isPM ?: false)
                                intent.putExtra("day1Time", day1Data?.time ?: "")
                                intent.putExtra("day2Time", day2Data?.time ?: "")

                                context.startActivity(intent)
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
