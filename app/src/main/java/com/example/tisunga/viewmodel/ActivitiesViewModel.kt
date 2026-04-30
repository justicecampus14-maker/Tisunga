package com.example.tisunga.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Event
import com.example.tisunga.data.model.Meeting
import com.example.tisunga.data.remote.ApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ActivitiesViewModel : ViewModel() {

    var meetings by mutableStateOf<List<Meeting>>(emptyList())
        private set

    var events by mutableStateOf<List<Event>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun clearError() { error = null }

    fun load(groupId: String) {
        viewModelScope.launch {
            loading = true
            error = null
            fetchActivities(groupId)
            loading = false
        }
    }

    private suspend fun fetchActivities(groupId: String) {
        try {
            val api = ApiClient.getClient()
            val fetchedMeetings = api.getGroupMeetings(groupId)
            meetings = fetchedMeetings.distinctBy { "${it.title}-${it.scheduledAt}-${it.location}" }
            
            val fetchedEvents = api.getGroupEvents(groupId)
            events = fetchedEvents.distinctBy { "${it.title}-${it.endDate}" }
        } catch (e: Exception) {
            e.printStackTrace()
            error = "Failed to load activities: ${e.message}"
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
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateStr // Fallback to original if parsing fails
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
                meetings = (listOf(newMeeting) + meetings).distinctBy { "${it.title}-${it.scheduledAt}-${it.location}" }
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
                
                val body = mutableMapOf<String, Any>(
                    "title" to title,
                    "description" to (description ?: ""),
                    "endDate" to isoDate,
                    "type" to type.uppercase()
                )
                if (amount > 0) {
                    body["targetAmount"] = amount
                }

                val newEvent = api.createEvent(groupId, body)
                events = (listOf(newEvent) + events).distinctBy { it.id }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                error = "Failed to create event: ${e.message}"
            } finally {
                loading = false
            }
        }
    }
}
