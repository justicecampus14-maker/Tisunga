package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Group
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.utils.MockDataProvider
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "Michael",
    val userPhone: String = "0882752624",
    val myGroups: List<Group> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val errorMessage: String = ""
)

class HomeViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                userName = sessionManager.getUserName().ifEmpty { "Michael" },
                userPhone = sessionManager.getUserPhone().ifEmpty { "0882752624" }
            )
            try {
                val groups = apiService.getMyGroups()
                _uiState.value = _uiState.value.copy(isLoading = false, myGroups = groups)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myGroups = MockDataProvider.getMockGroups(),
                    recentTransactions = MockDataProvider.getMockTransactions().take(2)
                )
            }
        }
    }
}
