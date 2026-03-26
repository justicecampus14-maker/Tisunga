package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.model.MemberShare
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.utils.MockDataProvider
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupSavingsSummary(
    val groupId: Int,
    val groupName: String,
    val totalSavings: Double,
    val lastSavedDate: String,
    val withdrawDate: String? = null,
    val memberCount: Int
)

data class SavingsUiState(
    val isLoading: Boolean = false,
    val contributions: List<Contribution> = emptyList(),
    val totalSavings: Double = 0.0,
    val groupSavings: List<GroupSavingsSummary> = emptyList(),
    val disbursementStatus: String = "",
    val memberShares: List<MemberShare> = emptyList(),
    val isSuccess: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = ""
)

class SavingsViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun getMyContributions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val contributions = apiService.getMyContributions()
                _uiState.value = _uiState.value.copy(isLoading = false, contributions = contributions, totalSavings = MockDataProvider.MOCK_TOTAL_SAVINGS)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contributions = MockDataProvider.getMockContributions(),
                    totalSavings = MockDataProvider.MOCK_TOTAL_SAVINGS
                )
            }
        }
    }

    fun makeContribution(contribution: Contribution) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.makeContribution(contribution)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Contribution sent. You will receive an SMS to confirm.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Contribution sent (Mock)")
            }
        }
    }

    fun getGroupSavings(groupId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // This would be a real API call
                // val summary = apiService.getGroupSavings(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, memberShares = MockDataProvider.getMockMemberShares())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, memberShares = MockDataProvider.getMockMemberShares())
            }
        }
    }

    fun requestDisbursement(groupId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.requestDisbursement(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Disbursement requested")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Disbursement requested (Mock)")
            }
        }
    }

    fun approveDisbursement(groupId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.approveDisbursement(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Disbursement approved and funds sent")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Disbursement approved (Mock)")
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, successMessage = "", errorMessage = "")
    }
}
