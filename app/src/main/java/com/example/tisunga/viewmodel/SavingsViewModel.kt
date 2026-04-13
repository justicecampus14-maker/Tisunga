package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.model.Disbursement
import com.example.tisunga.data.model.MemberSharePayout
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.RejectDisbursementRequest
import com.example.tisunga.utils.MockDataProvider
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupSavingsSummary(
    val groupId: String,
    val groupName: String,
    val totalSavings: Double,
    val lastSavedDate: String,
    val memberCount: Int,
    val withdrawDate: String? = null
)

data class SavingsUiState(
    val isLoading: Boolean = false,
    val contributions: List<Contribution> = emptyList(),
    val myHistory: List<Contribution> = emptyList(),
    val groupHistory: List<Contribution> = emptyList(),
    val totalSavings: Double = 0.0,
    val groupSavings: List<GroupSavingsSummary> = emptyList(),
    val memberCount: Int = 0,
    val currentDisbursement: Disbursement? = null,
    val history: List<Disbursement> = emptyList(),
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
                // Placeholder for fetching personal contributions
                val contributions = apiService.getMyContributions()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contributions = contributions,
                    totalSavings = MockDataProvider.MOCK_TOTAL_SAVINGS,
                    groupSavings = listOf(
                        GroupSavingsSummary("1", "Mphatso Group", 1000090.0, "02/20/26", 15, "21st May 2027"),
                        GroupSavingsSummary("2", "Doman Group", 200000.0, "03/15/26", 20, "31st Dec 2026")
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contributions = MockDataProvider.getMockContributions(),
                    totalSavings = MockDataProvider.MOCK_TOTAL_SAVINGS,
                    groupSavings = listOf(
                        GroupSavingsSummary("1", "Mphatso Group", 1000090.0, "02/20/26", 15, "21st May 2027"),
                        GroupSavingsSummary("2", "Doman Group", 200000.0, "03/15/26", 20, "31st Dec 2026")
                    )
                )
            }
        }
    }

    fun getMyHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val contributions = apiService.getMyContributions()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myHistory = contributions
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load contribution history"
                )
            }
        }
    }

    fun getGroupHistory(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val contributions = apiService.getGroupContributions(groupId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    groupHistory = contributions
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load group history"
                )
            }
        }
    }

    fun makeContribution(contribution: Contribution) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.makeContribution(
                    mapOf(
                        "groupId" to contribution.groupId,
                        "amount" to contribution.amount,
                        "type" to contribution.type
                    )
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Contribution request sent. You will receive an SMS to confirm."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to make contribution"
                )
            }
        }
    }

    fun getGroupSavingsData(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val dashboard = apiService.getGroupDashboard(groupId)
                val current = try {
                    apiService.getCurrentDisbursement(groupId)
                } catch (e: Exception) {
                    null
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalSavings = dashboard.group?.totalSavings ?: 0.0,
                    memberCount = dashboard.group?.memberCount ?: 0,
                    currentDisbursement = current?.toDomain()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    errorMessage = e.message ?: "Failed to load savings data"
                )
            }
        }
    }

    fun loadDisbursementHistory(groupId: String) {
        viewModelScope.launch {
            try {
                val history = apiService.getDisbursementHistory(groupId)
                _uiState.value = _uiState.value.copy(history = history.map { it.toDomain() })
            } catch (e: Exception) {}
        }
    }

    fun requestDisbursement(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val result = apiService.requestDisbursement(groupId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentDisbursement = result.toDomain(),
                    isSuccess = true,
                    successMessage = "Disbursement requested. Treasurer has been notified."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to request disbursement"
                )
            }
        }
    }

    fun approveDisbursement(groupId: String, disbursementId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                apiService.approveDisbursement(groupId, disbursementId.toString())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Disbursement approved! Funds are being sent to members.",
                    currentDisbursement = _uiState.value.currentDisbursement?.copy(status = "APPROVED")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to approve disbursement"
                )
            }
        }
    }

    fun rejectDisbursement(groupId: String, disbursementId: Int, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                apiService.rejectDisbursement(groupId, disbursementId.toString(), RejectDisbursementRequest(reason))
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Disbursement request rejected.",
                    currentDisbursement = _uiState.value.currentDisbursement?.copy(
                        status = "REJECTED",
                        rejectionReason = reason
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to reject disbursement"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, successMessage = "", errorMessage = "")
    }

    // Helper to map DTO to Domain
    private fun com.example.tisunga.data.remote.dto.DisbursementResponse.toDomain() = Disbursement(
        id = id.toInt(),
        groupId = groupId.toInt(),
        amount = amount,
        status = status,
        requestedBy = requestedBy.toInt(),
        requestedByName = requestedByName,
        requestedAt = requestedAt,
        approvedBy = approvedBy?.toInt(),
        approvedByName = approvedByName,
        approvedAt = approvedAt,
        rejectionReason = rejectionReason,
        memberShares = memberShares.map { 
            MemberSharePayout(it.userId.toInt(), it.userName, it.userPhone, it.memberSavings, it.shareAmount, it.status)
        }
    )
}
