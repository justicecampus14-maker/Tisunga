package com.example.tisunga.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Loan
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.repository.LoanRepository
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
    // Separate success/error so the UI can react precisely
    val isSuccess: Boolean        = false,
    val successMessage: String    = "",
    val errorMessage: String      = "",
    val memberLoansTitle: String? = null,
    val memberLoansName: String?  = null,
    // Action-specific loading flags (avoid blocking the whole screen)
    val isApproving: String?      = null,   // loanId being approved
    val isRejecting: String?      = null,   // loanId being rejected
    val isRepaying: Boolean       = false,
    // Calculator
    val calcInterest: Double      = 0.0,
    val calcRepayable: Double     = 0.0,
    val calcRate: Double          = 0.0,
    val calcMonthly: Double       = 0.0,
    val isCalculating: Boolean    = false
)

class LoanViewModel(
    private val loanRepository: LoanRepository,
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanUiState())
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    // Keep an instance of the apiService for direct calls if repository is too high-level
    private val apiService = ApiClient.getClient()

    // ── Load my loans ─────────────────────────────────────────────────────────

    fun getMyLoans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val loans = loanRepository.getMyLoans()
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
                val response = loanRepository.getUserLoans(userId)
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
                val loans = loanRepository.getGroupLoans(groupId)
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

    fun applyForLoan(groupId: String, amount: Double, durationMonths: Int, purpose: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading    = true,
                errorMessage = "",
                isSuccess    = false
            )
            try {
                loanRepository.applyForLoan(
                    groupId = groupId,
                    amount = amount,
                    durationMonths = durationMonths,
                    purpose = purpose.ifBlank { null }
                )
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Loan application submitted. The group will be notified."
                )
                getMyLoans()
                getGroupLoans(groupId)
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
                loanRepository.approveLoan(loanId)
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
                loanRepository.rejectLoan(loanId, reason)
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
                loanRepository.repayLoan(loanId, amount, phone)
                _uiState.value = _uiState.value.copy(
                    isRepaying     = false,
                    isSuccess      = true,
                    successMessage = "Repayment initiated. You will receive an STK push on your phone to enter your PIN."
                )
                getMyLoans()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRepaying   = false,
                    errorMessage = parseError(e)
                )
            }
        }
    }

    // ── Interest calculator (Server-driven) ──────────────────────────────────

    fun calculateInterest(amount: Double, durationMonths: Int) {
        if (amount <= 0) {
            _uiState.value = _uiState.value.copy(
                calcInterest = 0.0, calcRepayable = 0.0, calcRate = 0.0, calcMonthly = 0.0
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalculating = true)
            try {
                val res = loanRepository.calculateLoanPreview(amount, durationMonths)
                _uiState.value = _uiState.value.copy(
                    isCalculating = false,
                    calcInterest  = res.interestAmount,
                    calcRepayable = res.totalRepayable,
                    calcRate      = res.interestRate,
                    calcMonthly   = res.monthlyRepayment
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCalculating = false)
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
