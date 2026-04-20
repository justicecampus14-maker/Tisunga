package com.example.tisunga.viewmodel

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

class EventViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val api = ApiClient.getClient()

    fun getGroupEvents(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val events = api.getGroupEvents(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, events = events)
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
                body["description"] = description
                if (targetAmount != null) body["targetAmount"] = targetAmount
                if (endDate != null) body["endDate"] = endDate

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
