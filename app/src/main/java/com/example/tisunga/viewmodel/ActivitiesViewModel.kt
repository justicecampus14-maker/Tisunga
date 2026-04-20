package com.example.tisunga.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Event
import com.example.tisunga.data.model.Meeting
import com.example.tisunga.data.remote.ApiClient
import kotlinx.coroutines.launch

class ActivitiesViewModel : ViewModel() {

    var meetings by mutableStateOf<List<Meeting>>(emptyList())
        private set

    var events by mutableStateOf<List<Event>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    fun load(groupId: String) {
        viewModelScope.launch {
            loading = true
            try {
                val api = ApiClient.getClient()
                meetings = api.getGroupMeetings(groupId)
                events = api.getGroupEvents(groupId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            loading = false
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
            try {
                val api = ApiClient.getClient()
                val body = mapOf(
                    "title" to title,
                    "scheduledAt" to scheduledAt,
                    "location" to (location ?: ""),
                    "description" to (description ?: "")
                )
                api.createMeeting(groupId, body)
                load(groupId)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
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
            try {
                val api = ApiClient.getClient()
                val body = mapOf(
                    "type" to type,
                    "title" to title,
                    "date" to date,
                    "amountType" to amountType,
                    "amount" to amount,
                    "description" to (description ?: "")
                )
                api.createEvent(groupId, body)
                load(groupId)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
