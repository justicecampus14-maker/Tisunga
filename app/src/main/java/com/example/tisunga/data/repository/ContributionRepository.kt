package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.remote.ApiService
import com.example.tisunga.data.remote.dto.ContributionInitResponse
import com.example.tisunga.data.remote.dto.ContributionRequest

class ContributionRepository(
    private val apiService: ApiService,
    private val sessionManager: com.example.tisunga.utils.SessionManager
) {
    suspend fun makeContribution(
        groupId: String,
        amount: Double,
        phone: String,
        type: String, // "SAVINGS", "SHARE_PURCHASE", "SOCIAL_FUND"
        externalRef: String? = null
    ): ContributionInitResponse =
        apiService.makeContribution(
            ContributionRequest(
                groupId = groupId,
                amount = amount,
                phone = phone,
                type = type,
                externalRef = externalRef
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
