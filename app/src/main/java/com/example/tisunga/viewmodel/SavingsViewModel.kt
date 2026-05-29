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
import kotlinx.coroutines.flow.update
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
    val userRole:          String   = "MEMBER",
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

    // â”€â”€ Main Savings Screen loader â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun loadSavingsData(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
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
                    lastSavedDate = "â€”",
                    memberCount   = count,
                    withdrawDate  = endDate,
                    memberSavings = memberRows
                )

                _uiState.update { it.copy(
                    isLoading         = false,
                    totalGroupSavings = groupTotal,
                    mySavings         = myPersonal,
                    memberCount       = count,
                    userRole          = dashboard.myRole ?: "MEMBER",
                    groupSavings      = listOf(summary)
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load savings"
                )}
            }
        }
    }

    fun getGroupSavingsData(groupId: String) = loadSavingsData(groupId)

    // â”€â”€ Contribution history â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun getMyContributions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val contributions = savingsRepository.getMyContributions()
                _uiState.update { it.copy(isLoading = false, contributions = contributions) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getMyHistory(groupId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                val userId = sessionManager.getUserId()
                if (userId.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, myHistory = emptyList()) }
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

                _uiState.update { it.copy(isLoading = false, myHistory = contributions) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load history"
                )}
            }
        }
    }

    fun getGroupHistory(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                val contributions = apiService.getGroupContributions(groupId)
                _uiState.update { it.copy(isLoading = false, groupHistory = contributions) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load group history"
                )}
            }
        }
    }

    fun makeContribution(contribution: Contribution) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                apiService.makeContribution(
                    ContributionRequest(
                        groupId = contribution.groupId,
                        amount  = contribution.amount,
                        phone   = contribution.phoneUsed ?: sessionManager.getUserPhone() ?: "",
                        type    = contribution.type
                    )
                )
                _uiState.update { it.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Contribution request sent. You will receive an SMS to confirm."
                )}
                getMyHistory(contribution.groupId)
                getGroupHistory(contribution.groupId)
                loadSavingsData(contribution.groupId)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to make contribution"
                )}
            }
        }
    }

    // â”€â”€ Disbursement â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun loadDisbursementHistory(groupId: String) {
        viewModelScope.launch {
            try {
                val history = apiService.getDisbursementHistory(groupId)
                val domainList = history.map { it.toDomain() }

                val active = domainList.firstOrNull {
                    it.status == "PENDING" || it.status == "PROCESSING"
                } ?: domainList.firstOrNull()

                _uiState.update { state ->
                    val refreshedCurrent = if (active != null) active
                    else if (state.currentDisbursement != null &&
                        domainList.none { it.id == state.currentDisbursement.id }) null
                    else state.currentDisbursement

                    state.copy(
                        history             = domainList,
                        currentDisbursement = refreshedCurrent
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun requestDisbursement(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                val result = apiService.requestDisbursement(groupId)
                _uiState.update { it.copy(
                    isLoading           = false,
                    currentDisbursement = result.disbursement.toDomain(),
                    isSuccess           = true,
                    successMessage      = "Disbursement requested. Awaiting Treasurer approval."
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = parseError(e)
                )}
                if (e is retrofit2.HttpException && e.code() == 409) {
                    loadDisbursementHistory(groupId)
                }
            }
        }
    }

    private fun parseError(e: Exception): String {
        if (e is retrofit2.HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<com.example.tisunga.data.remote.dto.ApiResponse<Any>>() {}.type
                val response = gson.fromJson<com.example.tisunga.data.remote.dto.ApiResponse<Any>>(errorBody, type)
                return response.message ?: response.error ?: "Error: ${e.code()}"
            } catch (ex: Exception) {
                return "Server error: ${e.code()}"
            }
        }
        return e.message ?: "An unexpected error occurred"
    }

    fun approveDisbursement(groupId: String, disbursementId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                val response = apiService.approveDisbursement(groupId, disbursementId)

                val msg = if (response.failedCount > 0) {
                    "Disbursement initiated with ${response.failedCount} failures. ${response.successCount}/${response.totalMembers} payouts sent."
                } else {
                    "Disbursement initiated for all ${response.totalMembers} members. Awaiting payment confirmation."
                }

                // FIX: mark UI as PROCESSING and immediately reload the full
                // disbursement list from the server so stale local state is replaced.
                // Previously the list was never reloaded after approval, causing
                // the screen to show an outdated status on re-entry.
                _uiState.update { state ->
                    state.copy(
                        isLoading           = false,
                        isSuccess           = true,
                        successMessage      = msg,
                        currentDisbursement = state.currentDisbursement?.copy(status = "PROCESSING")
                    )
                }
                // Reload both disbursement list and savings totals so the UI reflects
                // the server state (balance will be 0 once webhook confirms).
                loadDisbursementHistory(groupId)
                loadSavingsData(groupId)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = parseError(e)
                )}
            }
        }
    }

    fun rejectDisbursement(groupId: String, disbursementId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                val response = apiService.rejectDisbursement(groupId, disbursementId, RejectDisbursementRequest(reason))
                _uiState.update { state ->
                    state.copy(
                        isLoading           = false,
                        isSuccess           = true,
                        successMessage      = response.message ?: "Disbursement request rejected.",
                        currentDisbursement = state.currentDisbursement?.copy(
                            status          = "REJECTED",
                            rejectionReason = reason
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    errorMessage = parseError(e)
                )}
            }
        }
    }

    fun resetState() {
        _uiState.update { it.copy(
            isSuccess      = false,
            successMessage = "",
            errorMessage   = ""
        )}
    }

    fun setErrorMessage(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    // â”€â”€ Mapping helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private fun com.example.tisunga.data.remote.dto.DisbursementResponse.toDomain() = Disbursement(
        id              = id,
        groupId         = groupId,
        amount          = totalAmount ?: amount ?: 0.0,
        status          = status,
        requestedBy     = requestedBy,
        requestedByName = requestedByName,
        requestedAt     = createdAt ?: requestedAt ?: "",
        approvedBy      = approvedBy,
        approvedByName  = approvedByName,
        approvedAt      = approvedAt,
        rejectionReason = rejectionReason,
        memberShares    = memberShares.map {
            MemberSharePayout(
                userId = it.userId,
                userName = it.userName ?: it.name ?: "Unknown",
                userPhone = it.userPhone ?: it.phone ?: "",
                memberSavings = it.memberSavings,
                shareAmount = it.shareAmount,
                status = it.status ?: "PENDING"
            )
        }
    )
}
