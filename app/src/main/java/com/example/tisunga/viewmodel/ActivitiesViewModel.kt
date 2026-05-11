package com.example.tisunga.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Event
import com.example.tisunga.data.model.Meeting
import com.example.tisunga.data.remote.ApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ActivitiesViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    var meetings by mutableStateOf<List<Meeting>>(savedStateHandle.get<List<Meeting>>("meetings") ?: emptyList())
        private set

    var events by mutableStateOf<List<Event>>(savedStateHandle.get<List<Event>>("events") ?: emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    private fun saveState() {
        savedStateHandle.set("meetings", meetings)
        savedStateHandle.set("events", events)
    }

    fun clearError() { error = null }

    fun load(groupId: String, forceRefresh: Boolean = false) {
        // If we already have data and not forcing refresh, don't reload
        if (!forceRefresh && (meetings.isNotEmpty() || events.isNotEmpty())) return
        
        viewModelScope.launch {
            loading = true
            error = null
            fetchActivities(groupId)
            loading = false
        }
    }

    private suspend fun fetchActivities(groupId: String) {
        val api = ApiClient.getClient()
        
        try {
            val fetchedMeetings = api.getGroupMeetings(groupId)
            meetings = fetchedMeetings.distinctBy { it.id }
            saveState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            val fetchedEvents = api.getGroupEvents(groupId)
            events = fetchedEvents.distinctBy { it.id }
            saveState()
        } catch (e: Exception) {
            e.printStackTrace()
            error = "Failed to load events: ${e.message}"
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
        scheduledAt: String,
        location: String?,
        description: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                val isoDate = formatToIso(scheduledAt, true)
                val api = ApiClient.getClient()
                val body = mapOf(
                    "title" to title,
                    "scheduledAt" to isoDate,
                    "location" to (location ?: ""),
                    "agenda" to (description ?: "")
                )
                val newMeeting = api.createMeeting(groupId, body)
                meetings = (listOf(newMeeting) + meetings).distinctBy { it.id }
                saveState()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                error = "Failed to create meeting: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun createEvent(
        groupId: String,
        type: String,
        title: String,
        date: String,
        amountType: String,
        amount: Double,
        description: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                val isoDate = formatToIso(date, false)
                val api = ApiClient.getClient()
                
                val normalizedType = if (type.uppercase().contains("SAVING")) "SAVINGS" else "OTHER"
                val normalizedContrib = if (amountType.equals("FIXED", ignoreCase = true)) "EVENT" else "FLEXIBLE"

                val body = mutableMapOf<String, Any>(
                    "title" to title,
                    "type" to normalizedType,
                    "eventDate" to isoDate,
                    "contributionType" to normalizedContrib
                )
                
                if (normalizedContrib == "EVENT" && amount > 0) {
                    body["fixedAmount"] = amount
                }
                
                if (!description.isNullOrBlank()) {
                    body["description"] = description
                }

                val newEvent = api.createEvent(groupId, body)
                events = (listOf(newEvent) + events).distinctBy { it.id }
                saveState()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                error = "Create Event Failed: ${e.message}"
            } finally {
                loading = false
            }
        }
    }
}
