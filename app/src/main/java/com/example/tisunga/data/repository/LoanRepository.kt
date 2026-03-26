package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Loan
import com.example.tisunga.data.remote.ApiService

class LoanRepository(private val apiService: ApiService) {
    suspend fun getMyLoans() = apiService.getMyLoans()
    suspend fun getGroupLoans(groupId: Int) = apiService.getGroupLoans(groupId)
    suspend fun applyForLoan(loan: Loan) = apiService.applyForLoan(loan)
    suspend fun approveLoan(id: Int) = apiService.approveLoan(id)
    suspend fun rejectLoan(id: Int) = apiService.rejectLoan(id)
    suspend fun repayLoan(id: Int, amount: Double) = apiService.repayLoan(id, mapOf("amount" to amount))
}
