package com.wakeymatey.waker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeymatey.waker.ui.theme.WakeyMateyTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WakeyMateyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WakerApp()
                }
            }
        }
    }
}

@Composable
fun WakerApp() {
    var selectedWakeUpTime by remember { mutableStateOf(Date()) }
    var calculatedWakeUpTime by remember { mutableStateOf<Date?>(null) }
    val sleepDurations = mapOf(
        "3 Hours" to 3 * 3600 * 1000L,
        "5 Hours" to 5 * 3600 * 1000L,
        "7 Hours" to 7 * 3600 * 1000L,
        "9 Hours" to 9 * 3600 * 1000L
    )
    var selectedDuration by remember { mutableStateOf(sleepDurations["7 Hours"]!!) }
    val sleepManager = SleepManager()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            "Waker",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        DatePickerExample(
            selectedDate = selectedWakeUpTime,
            onDateSelected = { selectedWakeUpTime = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Select Sleep Duration",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        DropdownMenuExample(sleepDurations, selectedDuration) {
            selectedDuration = it
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                calculatedWakeUpTime = sleepManager.calculateWakeUpTime(selectedWakeUpTime, selectedDuration)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Calculate Wake-Up Time")
        }

        calculatedWakeUpTime?.let {
            Text(
                "Recommended Sleep Time: ${formatTime(it)}",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuExample(
    options: Map<String, Long>,
    selectedOption: Long,
    onOptionSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(options.entries.firstOrNull { it.value == selectedOption }?.key ?: "Select Duration")
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.rotate(if (expanded) 180f else 0f)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { (key, value) ->
                DropdownMenuItem(
                    text = { Text(key) },
                    onClick = {
                        onOptionSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DatePickerExample(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        time = selectedDate
    }

    Text(
        "Select Wake-Up Time",
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    var showDatePicker by remember { mutableStateOf(false) }

    Button(
        onClick = { showDatePicker = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Select Date")
    }

    if (showDatePicker) {
        DatePickerDialog(
            initialYear = calendar.get(Calendar.YEAR),
            initialMonth = calendar.get(Calendar.MONTH),
            initialDay = calendar.get(Calendar.DAY_OF_MONTH),
            onDateSet = { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.time)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Text(
        formatTime(selectedDate),
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
    )
}

@Composable
fun DatePickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onDateSet: (view: android.widget.DatePicker?, year: Int, month: Int, dayOfMonth: Int) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.platform.LocalContext.current.let { context ->
        android.app.DatePickerDialog(
            context,
            { view, year, month, dayOfMonth -> onDateSet(view, year, month, dayOfMonth) },
            initialYear, initialMonth, initialDay
        ).apply {
            setOnDismissListener { onDismiss() }
            show()
        }
    }
}

fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(date)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WakeyMateyTheme {
        WakerApp()
    }
}

class SleepManager {
    fun calculateWakeUpTime(selectedWakeUpTime: Date, sleepDuration: Long): Date {
        return Calendar.getInstance().apply {
            time = selectedWakeUpTime
            add(Calendar.MILLISECOND, -sleepDuration.toInt())
        }.time
    }
}
