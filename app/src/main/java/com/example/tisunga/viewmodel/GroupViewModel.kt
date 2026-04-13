package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Group
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.model.User
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.GroupDashboardResponse
import com.example.tisunga.data.remote.dto.MembershipResponse
import com.example.tisunga.data.remote.dto.SearchMemberResponse
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
    minContribution = minContribution,
    savingPeriod = savingPeriodMonths,
    maxMembers = maxMembers,
    startDate = startDate,
    endDate = endDate,
    meetingDay = meetingDay,
    meetingTime = meetingTime,
    visibility = "Public"
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
    val isSuccess: Boolean = false,
    val successMessage: String = "",
    val errorMessage: String = "",
    val searchResult: SearchMemberResponse? = null,
    val groupDashboard: GroupDashboardResponse? = null,
    val draft: GroupCreationDraft = GroupCreationDraft()
)

class GroupViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

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
                // Backend GET /groups/my returns a single object wrapped in { data: ... }
                // We call getMyGroup() and wrap in a list for the UI.
                val myGroupResponse = apiService.getMyGroup()
                val group = myGroupResponse.toGroup()
                _uiState.value = _uiState.value.copy(isLoading = false, groups = listOf(group))
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

    /**
     * Reads from draft state and sends the correct field names to the backend.
     * Key fix: sends "savingPeriodMonths" not "savingPeriod"; drops "visibility".
     */
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
                // Optional fields — only include if non-empty
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
                val message = e.message ?: "Failed to create group"
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
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

    fun getGroupMembers(groupId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val memberships = apiService.getGroupMembers(groupId)
                val users = memberships.map { m ->
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
                _uiState.value = _uiState.value.copy(isLoading = false, members = MockDataProvider.getMockMembers())
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

                // Backend POST /groups/{id}/members returns AddMemberResponse, not List<User>
                apiService.addMember(groupId, body)
                
                // Re-fetch the updated member list
                getGroupMembers(groupId)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    successMessage = "Member added"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    successMessage = "Member added (Mock)"
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
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Search failed")
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
                _uiState.value = _uiState.value.copy(isLoading = false, transactions = MockDataProvider.getMockTransactions())
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
                            description = "",
                            location = "",
                            minContribution = 0.0,
                            savingPeriod = 0,
                            maxMembers = g.memberCount, // Using as proxy if exact field missing
                            startDate = "",
                            endDate = g.endDate ?: "",
                            meetingDay = g.meetingDay ?: "",
                            meetingTime = g.meetingTime ?: "",
                            groupCode = g.groupCode ?: "",
                            totalSavings = g.totalSavings
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
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to update role")
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
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to remove member")
            }
        }
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
