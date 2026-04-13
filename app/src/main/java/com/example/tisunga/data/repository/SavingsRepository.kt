package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.remote.ApiService

class SavingsRepository(private val apiService: ApiService) {
    suspend fun getMyContributions() = apiService.getMyContributions()
    suspend fun getGroupContributions(groupId: String) = apiService.getGroupContributions(groupId)
    suspend fun makeContribution(contribution: Contribution) = apiService.makeContribution(
        mapOf(
            "groupId" to contribution.groupId,
            "amount" to contribution.amount,
            "type" to contribution.type
        )
    )
    suspend fun requestDisbursement(groupId: String) = apiService.requestDisbursement(groupId)
    suspend fun approveDisbursement(groupId: String, disbursementId: Int) =
        apiService.approveDisbursement(groupId, disbursementId.toString())
}
