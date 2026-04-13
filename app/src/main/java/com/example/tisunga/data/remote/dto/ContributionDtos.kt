package com.example.tisunga.data.remote.dto

data class ContributionInitResponse(
    val transactionRef: String,
    val externalRef: String?,
    val status: String,
    val amount: Double,
    val message: String? = null
)
