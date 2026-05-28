package com.example.tisunga.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
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

class MeetingViewModel(
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingUiState())
    val uiState: StateFlow<MeetingUiState> = _uiState.asStateFlow()

    private val api = ApiClient.getClient()

    fun getGroupMeetings(groupId: String, status: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val meetings = api.getGroupMeetings(groupId, status)
                    .distinctBy { "${it.title}-${it.scheduledAt}-${it.location}" }
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
        if (_uiState.value.selectedMeeting?.id == meetingId && _uiState.value.errorMessage.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = "", 
                selectedMeeting = null,
                attendance = emptyList()
            )
            try {
                val meeting = api.getMeeting(groupId, meetingId)
                _uiState.value = _uiState.value.copy(
                    isLoading       = false,
                    selectedMeeting = meeting,
                    attendance      = meeting.attendance
                )
            } catch (e: Exception) {
                // FALLBACK: If the detail API fails (common for SCHEDULED meetings on some backends),
                // try to load the meeting from the group meetings list.
                try {
                    val meetings = api.getGroupMeetings(groupId)
                    val found = meetings.find { it.id == meetingId }
                    if (found != null) {
                        val detailFallback = MeetingDetailResponse(
                            id = found.id,
                            title = found.title,
                            agenda = found.agenda,
                            location = found.location,
                            scheduledAt = found.scheduledAt,
                            status = found.status,
                            notes = found.notes,
                            image = found.image,
                            imageUrl = found.imageUrl,
                            presentCount = found.presentCount,
                            totalCount = found.totalCount,
                            attendancePercent = found.attendancePercent,
                            attendance = emptyList()
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            meetings = meetings,
                            selectedMeeting = detailFallback,
                            attendance = emptyList()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading    = false,
                            errorMessage = e.message ?: "Meeting not found"
                        )
                    }
                } catch (e2: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = e.message ?: "Failed to load meeting details"
                    )
                }
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
                api.updateMeetingStatus(groupId, meetingId, body)
                
                // Refresh full meeting detail to get updated counts and attendance list
                val updatedMeeting = api.getMeeting(groupId, meetingId)
                
                // Update meetings list for the summary view
                val meetings = _uiState.value.meetings.map {
                    if (it.id == meetingId) {
                        it.copy(
                            status = updatedMeeting.status,
                            presentCount = updatedMeeting.presentCount,
                            totalCount = updatedMeeting.totalCount,
                            attendancePercent = updatedMeeting.attendancePercent,
                            notes = updatedMeeting.notes,
                            image = updatedMeeting.image,
                            imageUrl = updatedMeeting.imageUrl
                        )
                    } else it
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading       = false,
                    isSuccess       = true,
                    meetings        = meetings,
                    selectedMeeting = updatedMeeting,
                    attendance      = updatedMeeting.attendance,
                    successMessage  = "Meeting marked as $status"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to update status"
                )
            }
        }
    }

    fun completeMeeting(groupId: String, meetingId: String, notes: String, imageUri: Uri?, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                // 1. Update status to COMPLETED
                api.updateMeetingStatus(groupId, meetingId, mapOf("status" to "COMPLETED"))

                // 2. Update meeting notes using dedicated endpoint
                api.updateMeetingNotes(groupId, meetingId, mapOf("notes" to notes))

                // 3. Upload image if present
                if (imageUri != null) {
                    val file = try {
                        uriToFile(imageUri, context)
                    } catch (e: Exception) {
                        null
                    }
                    if (file != null) {
                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                        api.uploadMeetingImage(groupId, meetingId, imagePart)
                    }
                }

                // 3. Refresh full meeting detail
                val updatedMeeting = api.getMeeting(groupId, meetingId)
                
                // Update meetings list for the summary view
                val meetings = _uiState.value.meetings.map {
                    if (it.id == meetingId) {
                        it.copy(
                            status = updatedMeeting.status,
                            presentCount = updatedMeeting.presentCount,
                            totalCount = updatedMeeting.totalCount,
                            attendancePercent = updatedMeeting.attendancePercent,
                            notes = updatedMeeting.notes,
                            image = updatedMeeting.image,
                            imageUrl = updatedMeeting.imageUrl
                        )
                    } else it
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    meetings = meetings,
                    selectedMeeting = updatedMeeting,
                    attendance = updatedMeeting.attendance,
                    successMessage = "Meeting completed and notes saved"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to complete meeting"
                )
            }
        }
    }

    fun uploadMeetingImage(groupId: String, meetingId: String, uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val file = try {
                    uriToFile(uri, context)
                } catch (e: Exception) {
                    null
                }
                
                if (file == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Could not process image file"
                    )
                    return@launch
                }

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                val updatedMeeting = api.uploadMeetingImage(groupId, meetingId, body)
                
                // Update the meeting in the list as well
                val meetings = _uiState.value.meetings.map {
                    if (it.id == meetingId) {
                        it.copy(
                            notes = updatedMeeting.notes,
                            image = updatedMeeting.image,
                            imageUrl = updatedMeeting.imageUrl
                        )
                    } else it
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    meetings = meetings,
                    selectedMeeting = updatedMeeting,
                    successMessage = "Meeting image uploaded successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to upload image"
                )
            }
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val file = File(context.cacheDir, "meeting_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        } ?: throw Exception("Failed to open input stream from URI")
        return file
    }

    fun updateMeetingNotes(groupId: String, meetingId: String, notes: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val body = mapOf("notes" to notes)
                val updatedMeeting = api.updateMeetingNotes(groupId, meetingId, body)
                
                // Update the meeting in the list
                val meetings = _uiState.value.meetings.map {
                    if (it.id == meetingId) {
                        it.copy(
                            notes = updatedMeeting.notes,
                            image = updatedMeeting.image,
                            imageUrl = updatedMeeting.imageUrl
                        )
                    } else it
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    meetings = meetings,
                    selectedMeeting = updatedMeeting,
                    successMessage = "Notes updated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to update notes"
                )
            }
        }
    }

    fun updateMeetingAgenda(groupId: String, meetingId: String, agenda: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val body = mapOf("agenda" to agenda)
                api.updateMeetingStatus(groupId, meetingId, body)
                
                // Refresh
                val updatedMeeting = api.getMeeting(groupId, meetingId)
                _uiState.value = _uiState.value.copy(
                    isLoading       = false,
                    isSuccess       = true,
                    selectedMeeting = updatedMeeting,
                    successMessage  = "Agenda updated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to update agenda"
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
                val entry = AttendanceEntry(userId = userId, status = status, note = note)
                val request = BulkAttendanceRequest(listOf(entry))
                api.submitBulkAttendance(groupId, meetingId, request)
                
                // Refresh attendance list
                getMeetingAttendance(groupId, meetingId)
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
                
                // Immediately update local state from result if possible, 
                // but we also need the full meeting object for other fields.
                // We'll perform the refresh call, but we can optimize by merging the result.
                val updatedMeeting = api.getMeeting(groupId, meetingId)
                
                _uiState.value = _uiState.value.copy(
                    isLoading       = false,
                    isSuccess       = true,
                    selectedMeeting = updatedMeeting,
                    attendance      = updatedMeeting.attendance,
                    successMessage  = "Attendance saved. ${result.presentCount} present."
                )

                // Update the meetings list in the background or separately if needed, 
                // but for now, prioritize the current screen's state.
                val meetings = _uiState.value.meetings.map {
                    if (it.id == meetingId) {
                        it.copy(
                            presentCount = updatedMeeting.presentCount,
                            totalCount = updatedMeeting.totalCount,
                            attendancePercent = updatedMeeting.attendancePercent,
                            notes = updatedMeeting.notes,
                            image = updatedMeeting.image,
                            imageUrl = updatedMeeting.imageUrl
                        )
                    } else it
                }
                _uiState.value = _uiState.value.copy(meetings = meetings)
                
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
