package com.example.tisunga.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Event
import com.example.tisunga.data.model.Meeting
import com.example.tisunga.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ActivitiesViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _meetings = MutableStateFlow<List<Meeting>>(savedStateHandle.get<List<Meeting>>("meetings") ?: emptyList())
    val meetings: StateFlow<List<Meeting>> = _meetings.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(savedStateHandle.get<List<Event>>("events") ?: emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _meetingFilter = MutableStateFlow("ALL")
    val meetingFilter: StateFlow<String> = _meetingFilter.asStateFlow()

    private val _eventFilter = MutableStateFlow("ALL")
    val eventFilter: StateFlow<String> = _eventFilter.asStateFlow()

    private fun saveState() {
        savedStateHandle.set("meetings", _meetings.value)
        savedStateHandle.set("events", _events.value)
    }

    fun clearError() { _error.value = null }

    fun setMeetingFilter(filter: String) {
        _meetingFilter.value = filter
    }

    fun setEventFilter(filter: String) {
        _eventFilter.value = filter
    }

    fun loadData(groupId: String, forceRefresh: Boolean = false) {
        // If we already have data and not forcing refresh, don't reload
        if (!forceRefresh && (_meetings.value.isNotEmpty() || _events.value.isNotEmpty())) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            fetchActivities(groupId)
            _isLoading.value = false
        }
    }

    private suspend fun fetchActivities(groupId: String) {
        val api = ApiClient.getClient()
        
        try {
            val fetchedMeetings = api.getGroupMeetings(groupId)
            _meetings.value = fetchedMeetings.distinctBy { it.id }
            saveState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            val fetchedEvents = api.getGroupEvents(groupId)
            _events.value = fetchedEvents.distinctBy { it.id }
            saveState()
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "Failed to load events: ${e.message}"
        }
    }

    private fun formatToIso(dateStr: String, isDateTime: Boolean): String {
        return try {
            val inputFormat = if (isDateTime) {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            }
            val date = inputFormat.parse(dateStr)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun createMeeting(
        groupId: String,
        title: String,
        agenda: String?,
        scheduledAt: String,
        location: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val isoDate = formatToIso(scheduledAt, true)
                val api = ApiClient.getClient()
                val body = mapOf(
                    "title" to title,
                    "scheduledAt" to isoDate,
                    "location" to (location ?: ""),
                    "agenda" to (agenda ?: "")
                )
                val newMeeting = api.createMeeting(groupId, body)
                _meetings.value = (listOf(newMeeting) + _meetings.value).distinctBy { it.id }
                saveState()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to create meeting: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createEvent(
        groupId: String,
        title: String,
        description: String?,
        type: String,
        date: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val isoDate = formatToIso(date, false)
                val api = ApiClient.getClient()
                
                val body = mutableMapOf<String, Any>(
                    "title" to title,
                    "type" to type,
                    "eventDate" to isoDate,
                    "contributionType" to "FLEXIBLE"
                )
                
                if (!description.isNullOrBlank()) {
                    body["description"] = description
                }

                val newEvent = api.createEvent(groupId, body)
                _events.value = (listOf(newEvent) + _events.value).distinctBy { it.id }
                saveState()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Create Event Failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
