package com.example.tisunga.ui.screens.meetings

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.tisunga.R
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
    
    val selectDateLabel = stringResource(R.string.select_date_label)
    var selectedDateText by remember { mutableStateOf(selectDateLabel) }
    var selectedTimeText by remember { mutableStateOf(context.getString(R.string.select_time_label)) }
    
    var year by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var day by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.schedule_meeting_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.meeting_title_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Unified Schedule Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (selectedDateText == selectDateLabel) "" else "$selectedDateText $selectedTimeText",
                        onValueChange = { },
                        label = { Text(stringResource(R.string.schedule_datetime_label)) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                DatePickerDialog(context, { _, y, m, d ->
                                    year = y; month = m + 1; day = d
                                    selectedDateText = "$d/${m + 1}/$y"
                                    TimePickerDialog(context, { _, h, min ->
                                        hour = h; minute = min
                                        selectedTimeText = String.format("%02d:%02d", h, min)
                                    }, hour, minute, true).show()
                                }, year, month - 1, day).show()
                            }
                    )
                }
                
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.location_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = agenda,
                    onValueChange = { agenda = it },
                    label = { Text(stringResource(R.string.agenda_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedDateText != selectDateLabel && location.isNotBlank() && agenda.isNotBlank()) {
                        val isoDate = buildIsoDateTime(year, month, day, hour, minute)
                        onCreate(title, isoDate, location, agenda)
                    }
                },
                enabled = title.isNotBlank() && selectedDateText != selectDateLabel && location.isNotBlank() && agenda.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
            ) {
                Text(stringResource(R.string.schedule_button))
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button), color = TextSecondary)
            }
        }
    )
}
