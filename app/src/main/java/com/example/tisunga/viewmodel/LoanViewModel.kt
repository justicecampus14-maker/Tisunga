package com.example.tisunga.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Loan
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.RejectLoanRequest
import com.example.tisunga.data.remote.dto.RepayLoanRequest
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil

data class LoanUiState(
    val isLoading: Boolean        = false,
    val myLoans: List<Loan>       = emptyList(),
    val groupLoans: List<Loan>    = emptyList(),
    val calculatedInterest: Double   = 0.0,
    val calculatedRepayable: Double  = 0.0,
    val calculatedInterestRate: Double = 0.0,
    val calculatedPeriodicRepayment: Double = 0.0,
    // Separate success/error so the UI can react precisely
    val isSuccess: Boolean        = false,
    val successMessage: String    = "",
    val errorMessage: String      = "",
    val memberLoansTitle: String? = null,
    val memberLoansName: String?  = null,
    // Action-specific loading flags (avoid blocking the whole screen)
    val isApproving: String?      = null,   // loanId being approved
    val isRejecting: String?      = null,   // loanId being rejected
    val isRepaying: Boolean       = false
)

class LoanViewModel(
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanUiState())
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    private val api = ApiClient.getClient()

    // ── Load my loans ─────────────────────────────────────────────────────────

    fun getMyLoans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val loans = api.getMyLoans()
                _uiState.value = _uiState.value.copy(isLoading = false, myLoans = loans)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load loans"
                )
            }
        }
    }

    fun getUserLoans(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = api.getUserLoans(userId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myLoans = response.loans,
                    memberLoansTitle = response.title,
                    memberLoansName = response.borrowerName
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load member loans"
                )
            }
        }
    }

    // ── Load group loans (for CHAIR / SECRETARY review panel) ────────────────

    fun getGroupLoans(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val loans = api.getGroupLoans(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, groupLoans = loans)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = e.message ?: "Failed to load group loans"
                )
            }
        }
    }

    // ── Apply for loan ────────────────────────────────────────────────────────

    fun applyForLoan(groupId: String, amount: Double, durationValue: Int, isWeeks: Boolean, purpose: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading    = true,
                errorMessage = "",
                isSuccess    = false
            )
            try {
                // Backend expects durationMonths (Integer)
                // If weeks, we approximate to months (minimum 1 month for backend consistency)
                val durationMonths = if (isWeeks) {
                    ceil(durationValue / 4.0).toInt().coerceAtLeast(1)
                } else {
                    durationValue
                }
                
                // Track original requested period in purpose if weeks
                val finalPurpose = if (isWeeks) "[$durationValue Weeks] $purpose" else purpose

                val body = mutableMapOf<String, Any>(
                    "groupId"        to groupId,
                    "amount"         to amount,
                    "durationMonths" to durationMonths
                )
                if (finalPurpose.isNotBlank()) body["purpose"] = finalPurpose

                api.applyForLoan(body)
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Loan application submitted. The group will be notified."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    isSuccess    = false,
                    errorMessage = parseError(e)
                )
            }
        }
    }

    // ── Approve loan (CHAIR or SECRETARY only — backend enforces) ─────────────

    fun approveLoan(loanId: String, groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApproving = loanId, errorMessage = "")
            try {
                api.approveLoan(loanId)
                _uiState.value = _uiState.value.copy(
                    isApproving    = null,
                    isSuccess      = true,
                    successMessage = "Loan approved and disbursement initiated. The member will receive an SMS."
                )
                // Refresh list so the approved loan moves to ACTIVE
                getGroupLoans(groupId)
                getMyLoans()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isApproving  = null,
                    errorMessage = parseError(e)
                )
            }
        }
    }

    // ── Reject loan ───────────────────────────────────────────────────────────

    fun rejectLoan(loanId: String, reason: String, groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRejecting = loanId, errorMessage = "")
            try {
                api.rejectLoan(loanId, RejectLoanRequest(reason))
                _uiState.value = _uiState.value.copy(
                    isRejecting    = null,
                    isSuccess      = true,
                    successMessage = "Loan application rejected."
                )
                getGroupLoans(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRejecting  = null,
                    errorMessage = parseError(e)
                )
            }
        }
    }

    // ── Repay loan ────────────────────────────────────────────────────────────

    fun repayLoan(loanId: String, amount: Double, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRepaying = true, errorMessage = "")
            try {
                api.repayLoanTyped(loanId, RepayLoanRequest(amount, phone))
                _uiState.value = _uiState.value.copy(
                    isRepaying     = false,
                    isSuccess      = true,
                    successMessage = "Repayment initiated. You will receive an STK push on your phone to enter your PIN."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRepaying   = false,
                    errorMessage = parseError(e)
                )
            }
        }
    }

    // ── Interest calculator (local — no API call) ─────────────────────────────

    fun calculateInterest(amount: Double, durationValue: Int, isWeeks: Boolean) {
        // Scaling interest rate logic based on the period
        // Base rate is 5% for 1 month. 
        // We'll scale it: 5% per month. 
        // For weeks, it's (5 / 4)% per week = 1.25% per week.
        val monthlyRate = 0.05
        val rate = if (isWeeks) {
            (monthlyRate / 4.0) * durationValue
        } else {
            monthlyRate * durationValue
        }
        
        val interest    = amount * rate
        val totalRepay  = amount + interest
        _uiState.value = _uiState.value.copy(
            calculatedInterest  = interest,
            calculatedRepayable = totalRepay,
            calculatedInterestRate = rate * 100,
            calculatedPeriodicRepayment = if (durationValue > 0) totalRepay / durationValue else totalRepay
        )
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isSuccess      = false,
            successMessage = "",
            errorMessage   = ""
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Extract a clean message from an exception, stripping Retrofit boilerplate */
    private fun parseError(e: Exception): String {
        val raw = e.message ?: "Unknown error"
        // Retrofit wraps HTTP errors as "HTTP 409 ..." — extract the readable part
        return when {
            raw.contains("409") -> "You already have an active or pending loan in this group."
            raw.contains("400") -> "Invalid request. Please check the details and try again."
            raw.contains("403") -> "You don't have permission to perform this action."
            raw.contains("502") -> "Disbursement failed. Please try again or contact support."
            else                -> raw.take(120)
        }
    }
}
