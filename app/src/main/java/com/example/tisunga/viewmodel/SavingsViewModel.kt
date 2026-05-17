package com.example.tisunga.viewmodel

import androidx.lifecycle.SavedStateHandle
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

/** One row on the Savings screen member-list */
data class MemberSavingsRow(
    val userId:    String,
    val userName:  String,
    val userPhone: String,
    val role:      String  = "MEMBER",
    val amount:    Double
)

data class GroupSavingsSummary(
    val groupId:      String,
    val groupName:    String,
    val totalSavings: Double,
    val mySavings:    Double,
    val lastSavedDate: String,
    val memberCount:  Int,
    val withdrawDate: String? = null,
    val memberSavings: List<MemberSavingsRow> = emptyList()
)

data class SavingsUiState(
    val isLoading:         Boolean  = false,
    val contributions:     List<Contribution> = emptyList(),
    val myHistory:         List<Contribution> = emptyList(),
    val groupHistory:      List<Contribution> = emptyList(),
    val totalGroupSavings: Double   = 0.0,
    val mySavings:         Double   = 0.0,
    val groupSavings:      List<GroupSavingsSummary> = emptyList(),
    val memberCount:       Int      = 0,
    val currentDisbursement: Disbursement? = null,
    val history:           List<Disbursement> = emptyList(),
    val isSuccess:         Boolean  = false,
    val successMessage:    String   = "",
    val errorMessage:      String   = ""
)

class SavingsViewModel(
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val savingsRepository = com.example.tisunga.data.repository.SavingsRepository(ApiClient.getClient(), sessionManager)

    private val _uiState = MutableStateFlow(SavingsUiState())
    val uiState: StateFlow<SavingsUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    // ── Main Savings Screen loader ────────────────────────────────────────────

    /**
     * Loads everything the Savings screen needs:
     *   1. GET /groups/{groupId}/dashboard   → totalSavings, mySavings, memberCount
     *      (backend now computes reliable totals from confirmed contributions when
     *       group.totalSavings is 0 due to webhook not having fired)
     *   2. GET /groups/{groupId}/members/savings → per-member savings list
     *      (NEW endpoint — replaces the disbursement workaround)
     */
    fun loadSavingsData(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val dashboard = apiService.getGroupDashboard(groupId)

                val groupTotal  = dashboard.group?.totalSavings ?: 0.0
                val myPersonal  = dashboard.mySavings
                val count       = dashboard.group?.memberCount  ?: 0

                // ── Per-member savings via the new dedicated endpoint ──────────
                // This replaces the old disbursement-data workaround which only
                // returned data when an active disbursement existed.
                val memberRows: List<MemberSavingsRow> = try {
                    apiService.getMemberSavings(groupId).map { dto ->
                        MemberSavingsRow(
                            userId    = dto.userId,
                            userName  = dto.userName,
                            userPhone = dto.userPhone,
                            role      = dto.role,
                            amount    = dto.amount
                        )
                    }
                } catch (e: Exception) {
                    // Endpoint not yet deployed — fall back to an empty list so the
                    // screen still renders the group-total and my-savings cards.
                    emptyList()
                }

                val endDate: String? = try {
                    apiService.getGroupById(groupId).endDate
                } catch (_: Exception) { null }

                val summary = GroupSavingsSummary(
                    groupId       = groupId,
                    groupName     = dashboard.group?.name ?: "",
                    totalSavings  = groupTotal,
                    mySavings     = myPersonal,
                    lastSavedDate = "–",
                    memberCount   = count,
                    withdrawDate  = endDate,
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

    /** Alias kept for compatibility with call-sites that used this name */
    fun getGroupSavingsData(groupId: String) = loadSavingsData(groupId)

    // ── Contribution history ──────────────────────────────────────────────────

    fun getMyContributions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val contributions = savingsRepository.getMyContributions()
                _uiState.value = _uiState.value.copy(isLoading = false, contributions = contributions)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun getMyHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val contributions = savingsRepository.getMyContributions()
                _uiState.value = _uiState.value.copy(isLoading = false, myHistory = contributions)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
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
                    isLoading    = false,
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
                getMyHistory()
                getGroupHistory(contribution.groupId)
                loadSavingsData(contribution.groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to make contribution"
                )
            }
        }
    }

    // ── Disbursement ──────────────────────────────────────────────────────────

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
                    isLoading           = false,
                    currentDisbursement = result.toDomain(),
                    isSuccess           = true,
                    successMessage      = "Disbursement requested. Treasurer has been notified."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to request disbursement"
                )
            }
        }
    }

    fun approveDisbursement(groupId: String, disbursementId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                apiService.approveDisbursement(groupId, disbursementId)
                _uiState.value = _uiState.value.copy(
                    isLoading           = false,
                    isSuccess           = true,
                    successMessage      = "Disbursement approved! Funds are being sent to members.",
                    currentDisbursement = _uiState.value.currentDisbursement?.copy(status = "APPROVED")
                )
                loadSavingsData(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to approve disbursement"
                )
            }
        }
    }

    fun rejectDisbursement(groupId: String, disbursementId: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                apiService.rejectDisbursement(groupId, disbursementId, RejectDisbursementRequest(reason))
                _uiState.value = _uiState.value.copy(
                    isLoading           = false,
                    isSuccess           = true,
                    successMessage      = "Disbursement request rejected.",
                    currentDisbursement = _uiState.value.currentDisbursement?.copy(
                        status          = "REJECTED",
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
        _uiState.value = _uiState.value.copy(
            isSuccess      = false,
            successMessage = "",
            errorMessage   = ""
        )
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private fun com.example.tisunga.data.remote.dto.DisbursementResponse.toDomain() = Disbursement(
        id              = id,
        groupId         = groupId,
        amount          = amount,
        status          = status,
        requestedBy     = requestedBy,
        requestedByName = requestedByName,
        requestedAt     = requestedAt,
        approvedBy      = approvedBy,
        approvedByName  = approvedByName,
        approvedAt      = approvedAt,
        rejectionReason = rejectionReason,
        memberShares    = memberShares.map {
            MemberSharePayout(
                it.userId, it.userName, it.userPhone,
                it.memberSavings, it.shareAmount, it.status
            )
        }
    )
}
