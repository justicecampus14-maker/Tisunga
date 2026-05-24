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
import kotlin.math.pow

data class LoanUiState(
    val isLoading: Boolean        = false,
    val myLoans: List<Loan>       = emptyList(),
    val groupLoans: List<Loan>    = emptyList(),
    val calculatedInterest: Double   = 0.0,
    val calculatedRepayable: Double  = 0.0,
    val calculatedInterestRate: Double = 0.0,
    val calculatedPeriodicRepayment: Double = 0.0,
    val isSuccess: Boolean        = false,
    val successMessage: String    = "",
    val errorMessage: String      = "",
    val memberLoansTitle: String? = null,
    val memberLoansName: String?  = null,
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
    private val apiService = ApiClient.getClient()

    // ── Data Fetching ─────────────────────────────────────────────────────────

    fun getMyLoans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val loans = apiService.getMyLoansApi()
                _uiState.value = _uiState.value.copy(isLoading = false, myLoans = loans)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Failed to load my loans")
            }
        }
    }

    fun getGroupLoans(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val loans = apiService.getGroupLoans(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, groupLoans = loans)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Failed to load group loans")
            }
        }
    }

    fun getUserLoans(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = apiService.getUserLoans(userId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myLoans = response.loans,
                    memberLoansTitle = response.title,
                    memberLoansName = response.borrowerName
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Failed")
            }
        }
    }

    // ── Loan Actions ──────────────────────────────────────────────────────────

    fun applyForLoan(groupId: String, amount: Double, durationMonths: Int, purpose: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "", isSuccess = false)
            try {
                val body = mapOf(
                    "groupId" to groupId,
                    "amount" to amount,
                    "durationMonths" to durationMonths,
                    "purpose" to purpose
                )
                apiService.applyForLoan(body)
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    isSuccess = true,
                    successMessage = "Loan application submitted successfully."
                )
                getMyLoans()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Error")
            }
        }
    }

    fun approveLoan(loanId: String, groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApproving = loanId, errorMessage = "")
            try {
                apiService.approveLoan(loanId)
                _uiState.value = _uiState.value.copy(
                    isApproving = null,
                    isSuccess = true,
                    successMessage = "Loan approved and disbursement initiated."
                )
                getGroupLoans(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isApproving = null, errorMessage = e.message ?: "Approval failed")
            }
        }
    }

    fun rejectLoan(loanId: String, reason: String, groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRejecting = loanId, errorMessage = "")
            try {
                apiService.rejectLoan(loanId, RejectLoanRequest(reason))
                _uiState.value = _uiState.value.copy(
                    isRejecting = null,
                    isSuccess = true,
                    successMessage = "Loan application rejected."
                )
                getGroupLoans(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRejecting = null, errorMessage = e.message ?: "Rejection failed")
            }
        }
    }

    fun repayLoan(loanId: String, amount: Double, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRepaying = true, errorMessage = "")
            try {
                apiService.repayLoanTyped(loanId, RepayLoanRequest(amount, phone))
                _uiState.value = _uiState.value.copy(
                    isRepaying = false,
                    isSuccess = true,
                    successMessage = "Repayment initiated. Check your phone for the PIN prompt."
                )
                getMyLoans()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRepaying = false, errorMessage = e.message ?: "Repayment failed")
            }
        }
    }

    // ── Calculations ──────────────────────────────────────────────────────────

    /**
     * Local Interest Calculator using Compound Interest
     * Formula: A = P * (1 + r)^n
     */
    fun calculateInterest(amount: Double, durationMonths: Int) {
        val monthlyRate = 0.05 // 5% per month

        // Compound interest factor: (1 + r)^n
        val factor = (1 + monthlyRate).pow(durationMonths.toDouble())
        
        val totalRepay = if (durationMonths > 0) amount * factor else amount
        val interest = totalRepay - amount
        
        // Total effective rate for the entire period as a percentage
        val totalEffectiveRate = if (amount > 0) (interest / amount) * 100 else (factor - 1) * 100
        
        _uiState.value = _uiState.value.copy(
            calculatedInterest  = interest,
            calculatedRepayable = totalRepay,
            calculatedInterestRate = totalEffectiveRate,
            calculatedPeriodicRepayment = if (durationMonths > 0) totalRepay / durationMonths else totalRepay
        )
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isSuccess = false,
            errorMessage = "",
            successMessage = ""
        )
    }
}
