package com.example.tisunga.ui.screens.meetings

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tisunga.ui.theme.*
import com.example.tisunga.viewmodel.buildIsoDateTime
import java.util.*

@Composable
fun CreateMeetingDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, scheduledAt: String, location: String?, agenda: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var agenda by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    
    var selectedDateText by remember { mutableStateOf("Select Date") }
    var selectedTimeText by remember { mutableStateOf("Select Time") }
    
    var year by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var day by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Meeting", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Meeting Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                year = y; month = m + 1; day = d
                                selectedDateText = "$d/${m + 1}/$y"
                            }, year, month - 1, day).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BackgroundLightGray, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(selectedDateText, fontSize = 12.sp)
                    }
                    
                    Button(
                        onClick = {
                            TimePickerDialog(context, { _, h, min ->
                                hour = h; minute = min
                                selectedTimeText = String.format("%02d:%02d", h, min)
                            }, hour, minute, true).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BackgroundLightGray, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(selectedTimeText, fontSize = 12.sp)
                    }
                }
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = agenda,
                    onValueChange = { agenda = it },
                    label = { Text("Agenda (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val isoDate = buildIsoDateTime(year, month, day, hour, minute)
                        onCreate(title, isoDate, location.ifBlank { null }, agenda.ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
