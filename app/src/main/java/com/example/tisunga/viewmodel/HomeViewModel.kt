package com.example.tisunga.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Group
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

data class HomeUiState(
    val isLoading: Boolean = false,
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val myGroups: List<Group> = emptyList(),
    val myRole: String? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val errorMessage: String = ""
)

class HomeViewModel(
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = "",
                userId    = sessionManager.getUserId().ifEmpty { "" },
                userName  = sessionManager.getUserName().ifEmpty { "" },
                userPhone = sessionManager.getUserPhone().ifEmpty { "" }
            )

            try {
                // 1. Fetch Latest Profile to keep Drawer/UI updated
                try {
                    val profile = apiService.getMyProfile()
                    sessionManager.saveUserData(
                        userId = profile.id,
                        userName = "${profile.firstName ?: ""} ${profile.lastName ?: ""}".trim(),
                        userPhone = profile.phone ?: "",
                        userRole = profile.memberships?.firstOrNull()?.role ?: "member"
                    )
                    _uiState.value = _uiState.value.copy(
                        userName = sessionManager.getUserName(),
                        userPhone = sessionManager.getUserPhone()
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Profile fetch failed (non-fatal): ${e.message}")
                }

                // 2. Fetch Group Data
                val response = apiService.getMyGroup()

                Log.d(TAG, "getMyGroup response: hasNoGroup=${response.hasNoGroup()}, " +
                        "groupId=${response.groupId}, groupName=${response.groupName}, " +
                        "group=${response.group?.id}, role=${response.role}")

                if (response.hasNoGroup()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading          = false,
                        myGroups           = emptyList(),
                        myRole             = null,
                        recentTransactions = emptyList()
                    )
                    return@launch
                }

                val group = response.toGroup()
                val role  = response.role ?: "MEMBER"

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myGroups  = listOf(group),
                    myRole    = role
                )

                sessionManager.saveMyGroupRole(group.id, role)

                // 3. Fetch Dashboard Data
                try {
                    val dashboard = apiService.getGroupDashboard(group.id)
                    _uiState.value = _uiState.value.copy(
                        myRole             = dashboard.myRole ?: role,
                        recentTransactions = dashboard.recentTransactions,
                        myGroups = _uiState.value.myGroups.map {
                            it.copy(
                                totalSavings = if (dashboard.totalSavings > 0) dashboard.totalSavings else it.totalSavings,
                                totalBorrowed = dashboard.totalBorrowed,
                                availableBalance = dashboard.availableBalance
                            )
                        }
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Dashboard fetch failed (non-fatal): ${e.message}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Home data load failed: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load: ${e.message}"
                )
            }
        }
    }

    fun refreshAfterCreation() = loadHomeData()

    fun logout() {
        sessionManager.clearSession()
    }

    fun getUserGroupRole(groupId: String): String = _uiState.value.myRole ?: "MEMBER"
}
