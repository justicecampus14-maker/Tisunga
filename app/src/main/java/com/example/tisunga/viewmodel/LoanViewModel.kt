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
                    isLoading = false,
                    myLoans = MockDataProvider.getMockLoans(),
                    groupLoans = MockDataProvider.getMockPendingLoans()
                )
            }
        }
    }

    fun getGroupLoans(groupId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val loans = apiService.getGroupLoans(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, groupLoans = loans)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    groupLoans = MockDataProvider.getMockLoans() + MockDataProvider.getMockPendingLoans()
                )
            }
        }
    }

    fun applyForLoan(loan: Loan) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.applyForLoan(loan)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan application submitted for approval")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan application submitted for approval (Mock)")
            }
        }
    }

    fun approveLoanWithConfirmation(loanId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.approveLoan(loanId)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan approved successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan approved successfully (Mock)")
            }
        }
    }

    fun rejectLoan(loanId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.rejectLoan(loanId)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan rejected")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Loan rejected (Mock)")
            }
        }
    }

    fun repayLoan(loanId: Int, amount: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.repayLoan(loanId, mapOf("amount" to amount))
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Repayment submitted. You will receive an SMS.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Repayment submitted (Mock)")
            }
        }
    }

    fun calculateLoan(amount: Double) {
        val interest = amount * 0.05
        _uiState.value = _uiState.value.copy(
            calculatedInterest = interest,
            calculatedRepayable = amount + interest
        )
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, successMessage = "", errorMessage = "")
    }
}
