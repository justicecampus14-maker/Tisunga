package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.model.Disbursement
import com.example.tisunga.data.model.MemberSharePayout
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.RejectDisbursementRequest
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Represents one member's savings row on the Savings screen */
data class MemberSavingsRow(
    val userId: String,
    val userName: String,
    val userPhone: String,
    val amount: Double
)

data class GroupSavingsSummary(
    val groupId: String,
    val groupName: String,
    val totalSavings: Double,       // sum of all contributions (not affected by loans)
    val mySavings: Double,          // current user's personal savings
    val lastSavedDate: String,
    val memberCount: Int,
    val withdrawDate: String? = null,
    val memberSavings: List<MemberSavingsRow> = emptyList()
)

data class SavingsUiState(
    val isLoading: Boolean = false,
    val contributions: List<Contribution> = emptyList(),
    val myHistory: List<Contribution> = emptyList(),
    val groupHistory: List<Contribution> = emptyList(),
    val totalGroupSavings: Double = 0.0,    // real group total from backend
    val mySavings: Double = 0.0,            // real personal savings from backend
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

    /**
     * Load the Savings screen data for the given group.
     * Sources:
     *   - GET /groups/{groupId}/dashboard  → totalSavings (group), mySavings (user), memberCount
     *   - GET /groups/{groupId}/members    → per-member savings list
     */
    fun loadSavingsData(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val dashboard = apiService.getGroupDashboard(groupId)
                val groupTotal = dashboard.group?.totalSavings ?: 0.0
                val myPersonal = dashboard.mySavings
                val count      = dashboard.group?.memberCount ?: 0

                // Per-member savings: from disbursement memberShares if available,
                // otherwise fall back to the members list (no individual amounts available)
                val memberRows: List<MemberSavingsRow> = try {
                    val disbursement = apiService.getCurrentDisbursement(groupId)
                    disbursement.memberShares.map { share ->
                        MemberSavingsRow(
                            userId    = share.userId,
                            userName  = share.userName,
                            userPhone = share.userPhone,
                            amount    = share.memberSavings
                        )
                    }.sortedByDescending { it.amount }
                } catch (_: Exception) {
                    // No active disbursement — show members with 0 savings as placeholder
                    try {
                        apiService.getGroupMembers(groupId).map { m ->
                            MemberSavingsRow(
                                userId    = m.user?.id ?: "",
                                userName  = listOfNotNull(m.user?.firstName, m.user?.lastName).joinToString(" "),
                                userPhone = m.user?.phone ?: "",
                                amount    = 0.0
                            )
                        }
                    } catch (_: Exception) { emptyList() }
                }

                // End date as withdraw date from group's endDate
                val endDate = try {
                    apiService.getGroupById(groupId).endDate
                } catch (_: Exception) { null }

                val summary = GroupSavingsSummary(
                    groupId      = groupId,
                    groupName    = dashboard.group?.name ?: "",
                    totalSavings = groupTotal,
                    mySavings    = myPersonal,
                    lastSavedDate = "–",
                    memberCount  = count,
                    withdrawDate = endDate,
                    memberSavings = memberRows
                )

                _uiState.value = _uiState.value.copy(
                    isLoading         = false,
                    totalGroupSavings = groupTotal,
                    mySavings         = myPersonal,
                    memberCount       = count,
                    groupSavings      = listOf(summary)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load savings"
                )
            }
        }
    }

    fun getMyContributions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val contributions = apiService.getMyContributions()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contributions = contributions
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun getMyHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val contributions = apiService.getMyContributions()
                _uiState.value = _uiState.value.copy(isLoading = false, myHistory = contributions)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load history"
                )
            }
        }
    }

    fun getGroupHistory(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val contributions = apiService.getGroupContributions(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, groupHistory = contributions)
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
                        "amount"  to contribution.amount,
                        "type"    to contribution.type
                    )
                )
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Contribution request sent. You will receive an SMS to confirm."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to make contribution"
                )
            }
        }
    }

    fun getGroupSavingsData(groupId: String) {
        loadSavingsData(groupId)
    }

    fun loadDisbursementHistory(groupId: String) {
        viewModelScope.launch {
            try {
                val history = apiService.getDisbursementHistory(groupId)
                _uiState.value = _uiState.value.copy(history = history.map { it.toDomain() })
            } catch (_: Exception) {}
        }
    }

    fun requestDisbursement(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val result = apiService.requestDisbursement(groupId)
                _uiState.value = _uiState.value.copy(
                    isLoading  = false,
                    currentDisbursement = result.toDomain(),
                    isSuccess  = true,
                    successMessage = "Disbursement requested. Treasurer has been notified."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
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
                    isLoading  = false,
                    isSuccess  = true,
                    successMessage = "Disbursement approved! Funds are being sent to members.",
                    currentDisbursement = _uiState.value.currentDisbursement?.copy(status = "APPROVED")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
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
                    isLoading  = false,
                    isSuccess  = true,
                    successMessage = "Disbursement request rejected.",
                    currentDisbursement = _uiState.value.currentDisbursement?.copy(
                        status = "REJECTED",
                        rejectionReason = reason
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to reject disbursement"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, successMessage = "", errorMessage = "")
    }

    private fun com.example.tisunga.data.remote.dto.DisbursementResponse.toDomain() = Disbursement(
        id              = id.toInt(),
        groupId         = groupId.toInt(),
        amount          = amount,
        status          = status,
        requestedBy     = requestedBy.toInt(),
        requestedByName = requestedByName,
        requestedAt     = requestedAt,
        approvedBy      = approvedBy?.toInt(),
        approvedByName  = approvedByName,
        approvedAt      = approvedAt,
        rejectionReason = rejectionReason,
        memberShares    = memberShares.map {
            MemberSharePayout(it.userId.toInt(), it.userName, it.userPhone, it.memberSavings, it.shareAmount, it.status)
        }
    )
}
