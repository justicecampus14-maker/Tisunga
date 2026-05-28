package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.remote.ApiService
import com.example.tisunga.data.remote.dto.ContributionRequest

class SavingsRepository(
    private val apiService: ApiService,
    private val sessionManager: com.example.tisunga.utils.SessionManager
) {
    suspend fun getMyContributions(): List<Contribution> {
        val userId = sessionManager.getUserId()
        if (userId.isEmpty()) return emptyList()
        return apiService.getMyContributions(userId)
    }

    suspend fun getGroupContributions(groupId: String) = apiService.getGroupContributions(groupId)
    
    suspend fun makeContribution(contribution: Contribution) = apiService.makeContribution(
        ContributionRequest(
            groupId = contribution.groupId,
            amount = contribution.amount,
            phone = contribution.phoneUsed ?: sessionManager.getUserPhone() ?: "",
            type = contribution.type,
            externalRef = contribution.externalRef
        )
    )
    
    suspend fun requestDisbursement(groupId: String) = apiService.requestDisbursement(groupId)
    
    suspend fun approveDisbursement(groupId: String, disbursementId: String) =
        apiService.approveDisbursement(groupId, disbursementId)
}
