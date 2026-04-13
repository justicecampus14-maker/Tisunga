package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.AppNotification
import com.example.tisunga.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val errorMessage: String = ""
)

class NotificationViewModel : ViewModel() {

    private val repo = NotificationRepository()

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            repo.getNotifications().fold(
                onSuccess = { (list, count) ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notifications = list,
                        unreadCount = count
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Failed to load notifications"
                    )
                }
            )
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            repo.markAllRead().onSuccess {
                _uiState.value = _uiState.value.copy(
                    unreadCount = 0,
                    notifications = _uiState.value.notifications.map { it.copy(isRead = true) }
                )
            }
        }
    }

    fun markOneRead(notifId: String) {
        viewModelScope.launch {
            repo.markOneRead(notifId).onSuccess {
                _uiState.value = _uiState.value.copy(
                    notifications = _uiState.value.notifications.map { n ->
                        if (n.id == notifId) n.copy(isRead = true) else n
                    },
                    unreadCount = maxOf(0, _uiState.value.unreadCount - 1)
                )
            }
        }
    }
}
