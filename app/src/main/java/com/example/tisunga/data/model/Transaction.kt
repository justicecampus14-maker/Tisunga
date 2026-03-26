package com.example.tisunga.data.model

data class Transaction(
    val transId: String,
    val type: String, // "Contribution", "Loan Withdrawal", "Loan Repayment", "Join", etc.
    val description: String,
    val amount: Double,
    val balanceAfter: Double,
    val timestamp: String,
    val groupId: Int
)
