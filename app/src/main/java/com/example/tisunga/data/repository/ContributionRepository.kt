package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.remote.ApiService
import com.example.tisunga.data.remote.dto.ContributionInitResponse

class ContributionRepository(
    private val apiService: ApiService,
    private val sessionManager: com.example.tisunga.utils.SessionManager
) {
    suspend fun makeContribution(
        groupId: String,
        amount: Double,
        phone: String,
        type: String, // "REGULAR", "SHARE_PURCHASE", "SOCIAL_FUND"
        externalRef: String? = null
    ): ContributionInitResponse =
        apiService.makeContribution(
            mapOf(
                "groupId" to groupId,
                "amount" to amount,
                "phone" to phone,
                "type" to type,
                "externalRef" to (externalRef ?: "")
            )
        )

    suspend fun getMyHistory(page: Int = 1): List<Contribution> {
        val userId = sessionManager.getUserId()
        if (userId.isEmpty()) return emptyList()
        return apiService.getMyContributions(userId = userId, page = page)
    }

    suspend fun getGroupHistory(groupId: String, page: Int = 1): List<Contribution> =
        apiService.getGroupContributions(id = groupId, page = page)
}
