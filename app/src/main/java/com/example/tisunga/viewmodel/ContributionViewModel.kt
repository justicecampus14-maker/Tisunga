package com.example.tisunga.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.ContributionInitResponse
import com.example.tisunga.data.repository.ContributionRepository
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ContributionUiState(
    val isLoading: Boolean = false,
    val myHistory: List<Contribution> = emptyList(),
    val groupHistory: List<Contribution> = emptyList(),
    val initResponse: ContributionInitResponse? = null,
    val errorMessage: String? = null,
    val showPendingDialog: Boolean = false
)

class ContributionViewModel(
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository = ContributionRepository(ApiClient.getClient(), sessionManager)

    private val _uiState = MutableStateFlow(ContributionUiState())
    val uiState: StateFlow<ContributionUiState> = _uiState.asStateFlow()

    fun makeContribution(groupId: String, amount: Double, phone: String, type: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = repository.makeContribution(groupId, amount, phone, type)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    initResponse = response,
                    showPendingDialog = true
                )
                getMyHistory()
                getGroupHistory(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to initiate contribution"
                )
            }
        }
    }

    fun getMyHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val history = repository.getMyHistory()
                _uiState.value = _uiState.value.copy(isLoading = false, myHistory = history)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load your history"
                )
            }
        }
    }

    fun getGroupHistory(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val history = repository.getGroupHistory(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, groupHistory = history)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load group history"
                )
            }
        }
    }

    fun dismissPendingDialog() {
        _uiState.value = _uiState.value.copy(showPendingDialog = false, initResponse = null)
    }

    fun getUserPhone(): String = sessionManager.getUserPhone() ?: ""
}
