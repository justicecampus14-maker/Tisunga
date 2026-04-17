package com.example.tisunga.viewmodel

import android.util.Log
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
    val userName: String = "",
    val userPhone: String = "",
    val myGroups: List<Group> = emptyList(),
    val myRole: String? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val errorMessage: String = ""
)

class HomeViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = "",
                userName  = sessionManager.getUserName().ifEmpty { "" },
                userPhone = sessionManager.getUserPhone().ifEmpty { "" }
            )

            try {
                val response = apiService.getMyGroup()

                Log.d(TAG, "getMyGroup response: hasNoGroup=${response.hasNoGroup()}, " +
                        "groupId=${response.groupId}, groupName=${response.groupName}, " +
                        "group=${response.group?.id}, role=${response.role}")

                if (response.hasNoGroup()) {
                    // User genuinely not in a group
                    Log.d(TAG, "User has no group — showing no-group UI")
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

                Log.d(TAG, "Parsed group: id=${group.id}, name=${group.name}, " +
                        "totalSavings=${group.totalSavings}, mySavings=${group.mySavings}")

                // Show group immediately — don't wait for dashboard
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myGroups  = listOf(group),
                    myRole    = role
                )

                sessionManager.saveMyGroupRole(group.id, role)

                // Fire-and-forget: enrich with dashboard transactions
                try {
                    val dashboard = apiService.getGroupDashboard(group.id)
                    _uiState.value = _uiState.value.copy(
                        myRole             = dashboard.myRole ?: role,
                        recentTransactions = dashboard.recentTransactions
                    )
                    Log.d(TAG, "Dashboard loaded: role=${dashboard.myRole}, " +
                            "transactions=${dashboard.recentTransactions.size}")
                } catch (e: Exception) {
                    Log.w(TAG, "Dashboard fetch failed (non-fatal): ${e.message}")
                    // Group card is already shown — this is non-fatal
                }

            } catch (e: Exception) {
                // Only reach here if the network call itself fails
                Log.e(TAG, "getMyGroup FAILED: ${e.javaClass.simpleName}: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading          = false,
                    myGroups           = emptyList(),
                    myRole             = null,
                    recentTransactions = emptyList(),
                    errorMessage       = "Failed to load: ${e.message}"
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
