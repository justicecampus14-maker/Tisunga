package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.remote.ApiService
import com.example.tisunga.data.remote.dto.RejectDisbursementRequest

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
        mapOf(
            "groupId" to contribution.groupId,
            "amount"  to contribution.amount,
            "type"    to contribution.type
        )
    )

    suspend fun requestDisbursement(groupId: String) = apiService.requestDisbursement(groupId)

    // disbursementId is a String UUID â€” fixed from incorrect Int type
    suspend fun approveDisbursement(groupId: String, disbursementId: String) =
        apiService.approveDisbursement(groupId, disbursementId)

    suspend fun rejectDisbursement(groupId: String, disbursementId: String, reason: String) =
        apiService.rejectDisbursement(groupId, disbursementId, RejectDisbursementRequest(reason))
}
