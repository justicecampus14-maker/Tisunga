package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Group
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.model.User
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.utils.MockDataProvider
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupUiState(
    val isLoading: Boolean = false,
    val groups: List<Group> = emptyList(),
    val allGroups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val pendingGroup: Group? = null,
    val members: List<User> = emptyList(),
    val joinRequests: List<User> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val currentUserRole: String = "member",
    val isSuccess: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = ""
)

class GroupViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun updatePendingGroup(group: Group) {
        _uiState.value = _uiState.value.copy(pendingGroup = group)
    }

    fun getMyGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val groups = apiService.getMyGroups()
                _uiState.value = _uiState.value.copy(isLoading = false, groups = groups)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, groups = MockDataProvider.getMockGroups())
            }
        }
    }

    fun getAllGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val groups = apiService.getAllGroups()
                _uiState.value = _uiState.value.copy(isLoading = false, allGroups = groups)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, allGroups = MockDataProvider.getMockGroups())
            }
        }
    }

    fun createGroup(group: Group) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val createdGroup = apiService.createGroup(group)
                // Save role as chairperson for this group
                val currentRoles = mutableMapOf<Int, String>()
                currentRoles[createdGroup.id] = "chairperson"
                sessionManager.saveGroupRoles(currentRoles)
                
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, selectedGroup = createdGroup)
            } catch (e: Exception) {
                val mockGroup = MockDataProvider.getMockGroups().first().copy(name = group.name)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, selectedGroup = mockGroup)
            }
        }
    }

    fun joinGroup(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.joinGroup(mapOf("groupCode" to code))
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Join request sent")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Join request sent (Mock)")
            }
        }
    }

    fun getGroupMembers(groupId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val members = apiService.getGroupMembers(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, members = members)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, members = MockDataProvider.getMockMembers())
            }
        }
    }

    fun addMemberWithRole(groupId: Int, phone: String, role: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val members = apiService.addMemberWithRole(groupId, mapOf("phone" to phone, "role" to role))
                _uiState.value = _uiState.value.copy(isLoading = false, members = members, successMessage = "Member added")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, members = MockDataProvider.getMockMembers(), successMessage = "Member added (Mock)")
            }
        }
    }

    fun searchMemberByPhone(phone: String) {
        viewModelScope.launch {
            try {
                apiService.searchMemberByPhone(phone)
            } catch (e: Exception) {}
        }
    }

    fun getGroupTransactions(groupId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val transactions = apiService.getGroupTransactions(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, transactions = transactions)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, transactions = MockDataProvider.getMockTransactions())
            }
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, successMessage = "", errorMessage = "")
    }
}
