package com.example.tisunga.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.model.Disbursement
import com.example.tisunga.data.model.MemberSharePayout
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.ContributionRequest
import com.example.tisunga.data.remote.dto.RejectDisbursementRequest
import com.example.tisunga.utils.NetworkErrorHandler
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

    fun loadSavingsData(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val dashboard = apiService.getGroupDashboard(groupId)

                val groupTotal  = dashboard.group?.totalSavings ?: 0.0
                val myPersonal  = dashboard.mySavings
                val count       = dashboard.group?.memberCount  ?: 0

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
                    errorMessage = NetworkErrorHandler.getSafeMessage(e)
                )
            }
        }
    }

    fun getGroupSavingsData(groupId: String) = loadSavingsData(groupId)

    // ── Contribution history ──────────────────────────────────────────────────

    fun getMyContributions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val contributions = savingsRepository.getMyContributions()
                _uiState.value = _uiState.value.copy(isLoading = false, contributions = contributions)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = NetworkErrorHandler.getSafeMessage(e)
                )
            }
        }
    }

    fun getMyHistory(groupId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val userId = sessionManager.getUserId()
                if (userId.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, myHistory = emptyList())
                    return@launch
                }

                val contributions = if (!groupId.isNullOrEmpty()) {
                    try {
                        apiService.getGroupContributions(groupId).filter { it.userId == userId }
                    } catch (e: Exception) {
                        savingsRepository.getMyContributions()
                    }
                } else {
                    savingsRepository.getMyContributions()
                }

                _uiState.value = _uiState.value.copy(isLoading = false, myHistory = contributions)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = NetworkErrorHandler.getSafeMessage(e)
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
                    errorMessage = NetworkErrorHandler.getSafeMessage(e)
                )
            }
        }
    }

    fun makeContribution(contribution: Contribution) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.makeContribution(
                    ContributionRequest(
                        groupId = contribution.groupId,
                        amount  = contribution.amount,
                        phone   = contribution.phoneUsed ?: sessionManager.getUserPhone() ?: "",
                        type    = contribution.type
                    )
                )
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Contribution request sent. You will receive an SMS to confirm."
                )
                getMyHistory(contribution.groupId)
                getGroupHistory(contribution.groupId)
                loadSavingsData(contribution.groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = NetworkErrorHandler.getSafeMessage(e)
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
                    errorMessage = NetworkErrorHandler.getSafeMessage(e)
                )
            }
        }
    }

    fun approveDisbursement(groupId: String, disbursementId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = apiService.approveDisbursement(groupId, disbursementId)
                _uiState.value = _uiState.value.copy(
                    isLoading           = false,
                    isSuccess           = true,
                    successMessage      = response.message ?: "Disbursement approved! Funds are being sent to members.",
                    currentDisbursement = _uiState.value.currentDisbursement?.copy(status = "APPROVED")
                )
                loadSavingsData(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = NetworkErrorHandler.getSafeMessage(e)
                )
            }
        }
    }

    fun rejectDisbursement(groupId: String, disbursementId: String, reason: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = apiService.rejectDisbursement(groupId, disbursementId, RejectDisbursementRequest(reason))
                _uiState.value = _uiState.value.copy(
                    isLoading           = false,
                    isSuccess           = true,
                    successMessage      = response.message ?: "Disbursement request rejected.",
                    currentDisbursement = _uiState.value.currentDisbursement?.copy(
                        status          = "REJECTED",
                        rejectionReason = reason
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = NetworkErrorHandler.getSafeMessage(e)
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
