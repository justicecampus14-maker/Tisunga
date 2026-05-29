package com.example.tisunga.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Group
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.model.User
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.GroupDashboardResponse
import com.example.tisunga.data.remote.dto.MembershipResponse
import com.example.tisunga.data.remote.dto.SearchMemberResponse
import com.example.tisunga.utils.NetworkErrorHandler
import com.example.tisunga.utils.MockDataProvider
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Holds in-progress group creation data across Step1 → Step2 → Summary.
 */
data class GroupCreationDraft(
    val name: String = "",
    val description: String = "",
    val location: String = "",
    val minContribution: Double = 0.0,
    val savingPeriodMonths: Int = 6,
    val maxMembers: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val meetingDay: String = "",
    val meetingTime: String = ""
)

fun GroupCreationDraft.toGroup(id: String = "0") = Group(
    id = id,
    name = name,
    description = description,
    location = location,
    groupCode = "",
    minContribution = minContribution,
    savingPeriod = savingPeriodMonths,
    maxMembers = maxMembers,
    startDate = startDate,
    endDate = endDate,
    meetingDay = meetingDay,
    meetingTime = meetingTime,
    totalSavings = 0.0,
    isActive = true,
    mySavings = 0.0
)

data class GroupUiState(
    val isLoading: Boolean = false,
    val groups: List<Group> = emptyList(),
    val allGroups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val members: List<User> = emptyList(),
    val joinRequests: List<User> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val currentUserRole: String = "member",
    val currentUserId: String = "",
    val isSuccess: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = "",
    val searchResult: SearchMemberResponse? = null,
    val groupDashboard: GroupDashboardResponse? = null,
    val draft: GroupCreationDraft = GroupCreationDraft()
)

class GroupViewModel(
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupUiState(currentUserId = sessionManager.getUserId()))
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    private fun handleApiError(e: Exception): String {
        return NetworkErrorHandler.getSafeMessage(e)
    }

    // ── Draft helpers ─────────────────────────────────────────────────────

    fun updateDraft(update: GroupCreationDraft.() -> GroupCreationDraft) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.update())
    }

    fun clearDraft() {
        _uiState.value = _uiState.value.copy(draft = GroupCreationDraft())
    }

    // ── API calls ─────────────────────────────────────────────────────────

    fun getMyGroups() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val myGroupResponse = apiService.getMyGroup()
                val group = myGroupResponse.toGroup()
                _uiState.value = _uiState.value.copy(isLoading = false, groups = listOf(group))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
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
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun createGroup() {
        viewModelScope.launch {
            val draft = _uiState.value.draft
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val body = mutableMapOf<String, Any>(
                    "name"               to draft.name,
                    "minContribution"    to draft.minContribution,
                    "savingPeriodMonths" to draft.savingPeriodMonths,
                    "maxMembers"         to draft.maxMembers
                )
                if (draft.description.isNotEmpty()) body["description"] = draft.description
                if (draft.location.isNotEmpty())    body["location"]    = draft.location
                if (draft.startDate.isNotEmpty())   body["startDate"]   = draft.startDate
                if (draft.endDate.isNotEmpty())     body["endDate"]     = draft.endDate
                if (draft.meetingDay.isNotEmpty())  body["meetingDay"]  = draft.meetingDay
                if (draft.meetingTime.isNotEmpty()) body["meetingTime"] = draft.meetingTime

                val createdGroup = apiService.createGroup(body)
                val currentRoles = mapOf(createdGroup.id to "chairperson")
                sessionManager.saveGroupRoles(currentRoles)
                clearDraft()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    selectedGroup = createdGroup
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleApiError(e))
            }
        }
    }

    fun updateGroup(groupId: String, name: String, description: String, location: String, meetingTime: String, meetingDay: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val body = mutableMapOf<String, Any>(
                    "name" to name,
                    "description" to description,
                    "location" to location,
                    "meetingTime" to meetingTime
                )
                meetingDay?.let { body["meetingDay"] = it }

                val updatedGroup = apiService.updateGroup(groupId, body)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    successMessage = "Group updated successfully",
                    selectedGroup = updatedGroup
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = handleApiError(e)
                )
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
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleApiError(e))
            }
        }
    }

    fun getGroupMembers(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val memberships = apiService.getGroupMembers(groupId)
                val users = memberships
                    .filter { it.status.uppercase() == "ACTIVE" }
                    .map { m ->
                        User(
                            id        = m.user?.id ?: "",
                            firstName = m.user?.firstName ?: "",
                            lastName  = m.user?.lastName ?: "",
                            phone     = m.user?.phone ?: "",
                            role      = m.role.lowercase()
                        )
                    }
                _uiState.value = _uiState.value.copy(isLoading = false, members = users)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun addMemberWithRole(groupId: String, phone: String, role: String, firstName: String? = null, lastName: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val body = mutableMapOf(
                    "phone" to phone,
                    "role" to role
                )
                firstName?.let { body["firstName"] = it }
                lastName?.let { body["lastName"] = it }

                apiService.addMember(groupId, body)

                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    successMessage = "Member added",
                    searchResult = null
                )
                
                getGroupMembers(groupId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    errorMessage = handleApiError(e)
                )
            }
        }
    }

    fun searchMemberByPhone(phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, searchResult = null)
            try {
                val result = apiService.searchMemberByPhone(phone)
                _uiState.value = _uiState.value.copy(isLoading = false, searchResult = result)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleApiError(e))
            }
        }
    }

    fun getGroupTransactions(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val transactions = apiService.getGroupTransactions(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, transactions = transactions)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun getGroupDashboard(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val dashboard = apiService.getGroupDashboard(groupId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    groupDashboard = dashboard,
                    selectedGroup = dashboard.group?.let { g ->
                        Group(
                            id = g.id,
                            name = g.name,
                            description = g.description,
                            location = g.location,
                            groupCode = g.groupCode ?: "",
                            minContribution = 0.0,
                            savingPeriod = 0,
                            maxMembers = g.memberCount,
                            startDate = null,
                            endDate = g.endDate,
                            meetingDay = g.meetingDay,
                            meetingTime = g.meetingTime,
                            totalSavings = g.totalSavings,
                            isActive = true,
                            mySavings = dashboard.mySavings
                        )
                    },
                    currentUserRole = dashboard.myRole?.lowercase() ?: "member"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun updateMemberRole(groupId: String, userId: String, newRole: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.updateMember(groupId, userId, mapOf("role" to newRole))
                getGroupMembers(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Role updated")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleApiError(e))
            }
        }
    }

    fun removeMember(groupId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.removeMember(groupId, userId)
                getGroupMembers(groupId)
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Member removed")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleApiError(e))
            }
        }
    }

    fun seedSelectedGroup(group: com.example.tisunga.data.model.Group, role: String) {
        _uiState.value = _uiState.value.copy(
            selectedGroup   = group,
            currentUserRole = role.lowercase()
        )
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isSuccess = false,
            successMessage = "",
            errorMessage = "",
            searchResult = null
        )
    }
}
