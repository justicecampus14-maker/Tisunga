package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Contribution
import com.example.tisunga.data.remote.ApiService

class SavingsRepository(private val apiService: ApiService) {
    suspend fun getMyContributions() = apiService.getMyContributions()
    suspend fun getGroupContributions(groupId: Int) = apiService.getGroupContributions(groupId)
    suspend fun makeContribution(contribution: Contribution) = apiService.makeContribution(contribution)
    suspend fun requestDisbursement(groupId: Int) = apiService.requestDisbursement(groupId)
    suspend fun approveDisbursement(groupId: Int) = apiService.approveDisbursement(groupId)
}
