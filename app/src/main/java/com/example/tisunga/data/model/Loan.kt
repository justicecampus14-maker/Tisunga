package com.example.tisunga.data.model

import com.google.gson.annotations.SerializedName

data class Loan(
    val id: String,
    val groupId: String,
    val borrowerId: String,
    val borrowerName: String,
    val principalAmount: Double,
    val interestRate: Double,
    val totalRepayable: Double,
    val remainingBalance: Double,
    val durationMonths: Int,
    val dueDate: String,
    val status: String, // "PENDING", "ACTIVE", "COMPLETED", "REJECTED"
    val purpose: String?,
    val approverName: String?,
    val approvedAt: String?,
    val createdAt: String,
    val updatedAt: String,
    
    @SerializedName("Group")
    val group: LoanGroupInfo? = null
)

data class LoanGroupInfo(
    val id: String,
    val name: String
)

data class LoanRepaymentResult(
    val transactionRef: String,
    val amount: Double,
    val remainingBalance: Double,
    val status: String
)
