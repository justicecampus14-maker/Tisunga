package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Event
import com.example.tisunga.data.model.EventDetail
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.ContributeRequest
import com.example.tisunga.utils.MockDataProvider
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventUiState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val activeEvents: List<Event> = emptyList(),
    val closedEvents: List<Event> = emptyList(),
    val eventDetail: EventDetail? = null,
    val contributionMessage: String = "",
    val isSuccess: Boolean = false,
    val errorMessage: String = ""
)

class EventViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun getGroupEvents(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

            try {
                val events = apiService.getGroupEvents(groupId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    events = events,
                    activeEvents = events.filter { it.status.equals("OPEN", ignoreCase = true) || it.status.equals("active", ignoreCase = true) },
                    closedEvents = events.filter { it.status.equals("CLOSED", ignoreCase = true) || it.status.equals("closed", ignoreCase = true) }
                )
            } catch (e: Exception) {
                // Fallback to mock data if API fails
                val mockEvents = MockDataProvider.getMockEvents()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    events = mockEvents,
                    activeEvents = mockEvents.filter { it.status.equals("OPEN", ignoreCase = true) },
                    closedEvents = mockEvents.filter { it.status.equals("CLOSED", ignoreCase = true) },
                    errorMessage = e.message ?: "Failed to fetch events"
                )
            }
        }
    }

    fun createEvent(groupId: String, title: String, description: String, targetAmount: Double?, endDate: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

            try {
                val body = mutableMapOf<String, Any>(
                    "title" to title,
                    "description" to description,
                    "status" to "OPEN"
                )
                targetAmount?.let { body["targetAmount"] = it }
                endDate?.let { body["endDate"] = it }

                apiService.createEvent(groupId, body)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = e.message ?: "Error creating event"
                )
            }
        }
    }

    fun getEventDetail(eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val detail = apiService.getEvent(eventId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    eventDetail = detail
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to fetch event details"
                )
            }
        }
    }

    fun closeEvent(eventId: String, reason: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

            try {
                apiService.closeEvent(eventId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                // Refresh detail
                getEventDetail(eventId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = e.message ?: "Error closing event"
                )
            }
        }
    }

    fun contribute(eventId: String, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

            try {
                apiService.contributeToEvent(
                    eventId,
                    ContributeRequest(amount = amount)
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contributionMessage = "Successfully contributed ${amount}!"
                )
                // Refresh detail
                getEventDetail(eventId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error contributing"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isSuccess = false,
            errorMessage = "",
            contributionMessage = ""
        )
    }
}
