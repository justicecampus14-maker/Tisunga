package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Meeting
import com.example.tisunga.data.model.MeetingAttendance
import com.example.tisunga.data.model.AttendanceSummary
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.AttendanceEntry
import com.example.tisunga.data.remote.dto.BulkAttendanceRequest
import com.example.tisunga.data.remote.dto.MeetingDetailResponse
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MeetingUiState(
    val isLoading: Boolean = false,
    val meetings: List<Meeting> = emptyList(),
    val selectedMeeting: MeetingDetailResponse? = null,
    val attendance: List<MeetingAttendance> = emptyList(),
    val summary: AttendanceSummary? = null,
    val isSuccess: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = ""
)

class MeetingViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingUiState())
    val uiState: StateFlow<MeetingUiState> = _uiState.asStateFlow()

    private val api = ApiClient.getClient()

    fun getGroupMeetings(groupId: String, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val meetings = api.getGroupMeetings(groupId, status)
                _uiState.value = _uiState.value.copy(isLoading = false, meetings = meetings)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load meetings"
                )
            }
        }
    }

    fun getMeeting(groupId: String, meetingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val meeting = api.getMeeting(groupId, meetingId)
                _uiState.value = _uiState.value.copy(
                    isLoading       = false,
                    selectedMeeting = meeting,
                    attendance      = meeting.attendance
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load meeting"
                )
            }
        }
    }

    fun createMeeting(
        groupId: String,
        title: String,
        scheduledAt: String,
        location: String?,
        agenda: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val body = buildMap<String, Any> {
                    put("title", title)
                    put("scheduledAt", scheduledAt)
                    if (!location.isNullOrBlank()) put("location", location)
                    if (!agenda.isNullOrBlank())   put("agenda",   agenda)
                }
                val meeting = api.createMeeting(groupId, body)
                val updated = _uiState.value.meetings.toMutableList()
                updated.add(0, meeting)
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    meetings       = updated,
                    successMessage = "Meeting scheduled. Members notified by SMS."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to create meeting"
                )
            }
        }
    }

    fun updateStatus(groupId: String, meetingId: String, status: String, notes: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val body = buildMap<String, String> {
                    put("status", status)
                    if (!notes.isNullOrBlank()) put("notes", notes)
                }
                val updated = api.updateMeetingStatus(groupId, meetingId, body)
                val meetings = _uiState.value.meetings.map {
                    if (it.id == meetingId) it.copy(status = updated.status) else it
                }
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    meetings       = meetings,
                    successMessage = "Meeting marked as $status"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to update status"
                )
            }
        }
    }

    fun markSingleAttendance(
        groupId: String,
        meetingId: String,
        userId: String,
        status: String,
        note: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = "")
            try {
                val body = buildMap<String, Any> {
                    put("userId", userId)
                    put("status", status)
                    if (!note.isNullOrBlank()) put("note", note)
                }
                val result = api.markAttendance(groupId, meetingId, body)
                val updated = _uiState.value.attendance.map {
                    if (it.userId == result.userId) result else it
                }
                _uiState.value = _uiState.value.copy(attendance = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to mark attendance"
                )
            }
        }
    }

    fun submitBulkAttendance(groupId: String, meetingId: String, entries: List<AttendanceEntry>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val result = api.submitBulkAttendance(
                    groupId, meetingId, BulkAttendanceRequest(entries)
                )
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Attendance saved. ${result.presentCount} present."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to submit attendance"
                )
            }
        }
    }

    fun getMeetingAttendance(groupId: String, meetingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = api.getMeetingAttendance(groupId, meetingId)
                _uiState.value = _uiState.value.copy(
                    isLoading  = false,
                    attendance = response.attendance,
                    summary    = response.summary
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load attendance"
                )
            }
        }
    }

    fun sendReminder(groupId: String, meetingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val result = api.sendReminder(groupId, meetingId)
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Reminder sent to ${result.sentTo} members"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to send reminder"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isSuccess      = false,
            successMessage = "",
            errorMessage   = ""
        )
    }
}

fun buildIsoDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): String {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, day, hour, minute, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(cal.time)
}
