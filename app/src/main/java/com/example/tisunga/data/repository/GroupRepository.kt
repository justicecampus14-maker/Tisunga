package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Group
import com.example.tisunga.data.remote.ApiService

class GroupRepository(private val apiService: ApiService) {
    suspend fun getMyGroups() = apiService.getMyGroups()
    suspend fun getAllGroups() = apiService.getAllGroups()
    suspend fun getGroupById(id: Int) = apiService.getGroupById(id)
    suspend fun createGroup(group: Group) = apiService.createGroup(group)
    suspend fun joinGroup(code: String) = apiService.joinGroup(mapOf("groupCode" to code))
    suspend fun getGroupMembers(groupId: Int) = apiService.getGroupMembers(groupId)
    suspend fun addMemberWithRole(groupId: Int, phone: String, role: String) = 
        apiService.addMemberWithRole(groupId, mapOf("phone" to phone, "role" to role))
    suspend fun getGroupTransactions(groupId: Int) = apiService.getGroupTransactions(groupId)
}
