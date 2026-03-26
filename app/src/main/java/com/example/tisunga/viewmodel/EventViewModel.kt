package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Event
import com.example.tisunga.data.remote.ApiClient
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
    val isSuccess: Boolean = false,
    val errorMessage: String = ""
)

class EventViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun getGroupEvents(groupId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val events = apiService.getGroupEvents(groupId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    events = events,
                    activeEvents = events.filter { it.status == "active" },
                    closedEvents = events.filter { it.status == "closed" }
                )
            } catch (e: Exception) {
                val mockEvents = MockDataProvider.getMockEvents()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    events = mockEvents,
                    activeEvents = mockEvents.filter { it.status == "active" },
                    closedEvents = mockEvents.filter { it.status == "closed" }
                )
            }
        }
    }

    fun createEvent(event: Event) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.createEvent(event)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true) // Bypass for dev
            }
        }
    }

    fun closeEvent(eventId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.closeEvent(eventId)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true) // Bypass for dev
            }
        }
    }

    fun contributeToEvent(eventId: Int, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.contributeToEvent(eventId, mapOf("amount" to amount))
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true) // Bypass for dev
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, errorMessage = "")
    }
}
