package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Loan
import com.example.tisunga.data.remote.ApiService
import com.example.tisunga.data.remote.dto.ApplyLoanRequest
import com.example.tisunga.data.remote.dto.RejectLoanRequest
import com.example.tisunga.data.remote.dto.RepayLoanRequest

class LoanRepository(private val apiService: ApiService) {
    suspend fun getMyLoans() = apiService.getMyLoans()
    
    suspend fun getGroupLoans(groupId: String) = apiService.getGroupLoans(groupId)
    
    suspend fun applyForLoan(groupId: String, amount: Double, durationMonths: Int, purpose: String?) = 
        apiService.applyForLoanTyped(
            ApplyLoanRequest(
                groupId = groupId,
                amount = amount,
                durationMonths = durationMonths,
                purpose = purpose
            )
        )

    suspend fun approveLoan(loanId: String) = apiService.approveLoan(loanId)
    
    suspend fun rejectLoan(loanId: String, reason: String) = 
        apiService.rejectLoan(loanId, RejectLoanRequest(reason))
    
    suspend fun repayLoan(loanId: String, amount: Double, phone: String) = 
        apiService.repayLoanTyped(loanId, RepayLoanRequest(amount, phone))
}
