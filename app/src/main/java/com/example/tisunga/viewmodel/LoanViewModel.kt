package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Loan
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.utils.MockDataProvider
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoanUiState(
    val isLoading: Boolean = false,
    val myLoans: List<Loan> = emptyList(),
    val groupLoans: List<Loan> = emptyList(),
    val pendingLoans: List<Loan> = emptyList(),
    val calculatedInterest: Double = 0.0,
    val calculatedRepayable: Double = 0.0,
    val maxLoanAmount: Double = 0.0,
    val isSuccess: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = ""
)

class LoanViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(LoanUiState())
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun getMyLoans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val loans = apiService.getMyLoans()
                _uiState.value = _uiState.value.copy(isLoading = false, myLoans = loans)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading  = false,
                    myLoans    = MockDataProvider.getMockLoans(),
                    groupLoans = MockDataProvider.getMockPendingLoans()
                )
            }
        }
    }

    fun getGroupLoans(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val loans = apiService.getGroupLoans(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, groupLoans = loans)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading  = false,
                    groupLoans = MockDataProvider.getMockLoans() + MockDataProvider.getMockPendingLoans()
                )
            }
        }
    }

    fun applyForLoan(groupId: String, amount: Double, duration: Int, purpose: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val body = mapOf(
                    "groupId" to groupId,
                    "amount" to amount,
                    "period" to duration,
                    "purpose" to purpose,
                    "interestRate" to 5.0 // 5% per month
                )
                apiService.applyForLoan(body)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Loan application submitted for approval"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Loan application submitted for approval (Mock)"
                )
            }
        }
    }

    fun applyForLoan(loan: Loan) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // FIX 9: applyForLoan expects Map<String, Any> — convert Loan fields to map
                val body = mutableMapOf<String, Any>(
                    "groupId"     to loan.groupId,
                    "amount"      to loan.principalAmount,
                    "interestRate" to loan.interestRate,
                    "dueDate"     to loan.dueDate
                )
                loan.purpose?.let { body["purpose"] = it }
                loan.durationMonths.let { body["period"] = it }
                apiService.applyForLoan(body)
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Loan application submitted for approval"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Loan application submitted for approval (Mock)"
                )
            }
        }
    }

    fun approveLoan(loanId: String, groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.approveLoan(loanId)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan approved successfully")
                getGroupLoans(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan approved successfully (Mock)")
            }
        }
    }

    fun rejectLoan(loanId: String, reason: String, groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.rejectLoan(loanId, com.example.tisunga.data.remote.dto.RejectLoanRequest(reason))
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan rejected")
                getGroupLoans(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan rejected (Mock)")
            }
        }
    }

    fun repayLoan(loanId: String, amount: Double, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.repayLoanTyped(loanId, com.example.tisunga.data.remote.dto.RepayLoanRequest(amount, phone))
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Repayment submitted. You will receive an SMS.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Repayment submitted (Mock)")
            }
        }
    }

    fun calculateInterest(amount: Double, duration: Int) {
        val interest = amount * 0.05 * duration
        _uiState.value = _uiState.value.copy(
            calculatedInterest  = interest,
            calculatedRepayable = amount + interest
        )
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, successMessage = "", errorMessage = "")
    }
}
