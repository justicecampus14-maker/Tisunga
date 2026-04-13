package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Group
import com.example.tisunga.data.remote.ApiService

class GroupRepository(private val apiService: ApiService) {
    suspend fun getMyGroups() = apiService.getMyGroup()
    
    suspend fun getGroupById(id: String) = apiService.getGroupById(id)
    
    suspend fun createGroup(group: Group) = apiService.createGroup(
        mapOf(
            "name" to group.name,
            "description" to group.description,
            "location" to group.location,
            "minContribution" to group.minContribution,
            "savingPeriod" to group.savingPeriod,
            "maxMembers" to group.maxMembers,
            "visibility" to group.visibility,
            "startDate" to group.startDate,
            "endDate" to group.endDate,
            "meetingDay" to group.meetingDay,
            "meetingTime" to group.meetingTime
        )
    )

    suspend fun getGroupMembers(groupId: String) = apiService.getGroupMembers(groupId)
    
    suspend fun addMemberWithRole(groupId: String, phone: String, role: String) = 
        apiService.addMember(groupId, mapOf("phone" to phone, "role" to role))

    suspend fun getGroupTransactions(groupId: String) = apiService.getGroupTransactions(groupId)
}
