package com.example.tisunga.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Event
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EventUiState(
    val isLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val isSuccess: Boolean = false,
    val errorMessage: String = ""
)

class EventViewModel(
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val api = ApiClient.getClient()

    private fun restoreState(): EventUiState {
        val savedEvents = savedStateHandle.get<List<Event>>("events") ?: emptyList()
        return EventUiState(events = savedEvents)
    }

    private fun saveState(state: EventUiState) {
        savedStateHandle["events"] = state.events
    }

    fun getGroupEvents(groupId: String) {
        // Optional: Skip network call if we already have data
        if (_uiState.value.events.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val events = api.getGroupEvents(groupId)
                val newState = _uiState.value.copy(isLoading = false, events = events)
                _uiState.value = newState
                saveState(newState)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load events"
                )
            }
        }
    }

    fun createEvent(
        groupId: String,
        title: String,
        description: String,
        targetAmount: Double?,
        endDate: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val body = mutableMapOf<String, Any>()
                body["title"] = title
                if (description.isNotBlank()) body["description"] = description
                body["type"] = "OTHER"
                body["contributionType"] = "EVENT"
                
                if (targetAmount != null && targetAmount > 0) body["fixedAmount"] = targetAmount
                
                if (endDate != null) {
                    body["eventDate"] = if (endDate.contains("T")) {
                        // Strip milliseconds if present for consistency
                        if (endDate.contains(".")) endDate.substringBefore(".") + "Z" else endDate
                    } else {
                        "${endDate}T00:00:00Z"
                    }
                }

                api.createEvent(groupId, body)
                getGroupEvents(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to create event"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, errorMessage = "")
    }
}
