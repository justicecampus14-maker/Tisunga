package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Group
import com.example.tisunga.data.remote.ApiService

class GroupRepository(private val apiService: ApiService) {
    suspend fun getMyGroups() = apiService.getMyGroup()
    
    suspend fun getGroupById(id: String) = apiService.getGroupById(id)
    
    suspend fun createGroup(group: Group) = apiService.createGroup(
        mutableMapOf<String, Any>(
            "name" to group.name,
            "minContribution" to group.minContribution,
            "savingPeriodMonths" to group.savingPeriod,
            "maxMembers" to group.maxMembers
        ).apply {
            group.description?.let { if (it.isNotEmpty()) put("description", it) }
            group.location?.let { if (it.isNotEmpty()) put("location", it) }
            group.startDate?.let { if (it.isNotEmpty()) put("startDate", it) }
            group.endDate?.let { if (it.isNotEmpty()) put("endDate", it) }
            group.meetingDay?.let { if (it.isNotEmpty()) put("meetingDay", it) }
            group.meetingTime?.let { if (it.isNotEmpty()) put("meetingTime", it) }
        }
    )

    suspend fun getGroupMembers(groupId: String) = apiService.getGroupMembers(groupId)
    
    suspend fun addMemberWithRole(groupId: String, phone: String, role: String) = 
        apiService.addMember(groupId, mapOf("phone" to phone, "role" to role))

    suspend fun getGroupTransactions(groupId: String) = apiService.getGroupTransactions(groupId)
}
