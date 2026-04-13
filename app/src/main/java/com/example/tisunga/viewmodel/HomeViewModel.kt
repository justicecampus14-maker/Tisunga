package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.model.Group
import com.example.tisunga.data.model.Transaction
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.utils.MockDataProvider
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "Michael",
    val userPhone: String = "0882752624",
    val myGroups: List<Group> = emptyList(),
    val myRole: String? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val errorMessage: String = ""
)

class HomeViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()
    
    // Flag to track if we should show mock data (after creation) or stay empty (initial sign-in)
    private var hasCreatedGroupInSession = false
    private var lastCreatedGroupName: String? = null

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                userName = sessionManager.getUserName().ifEmpty { "Michael" },
                userPhone = sessionManager.getUserPhone().ifEmpty { "0882752624" }
            )
            
            try {
                val response = apiService.getMyGroup()
                val group = response.toGroup()
                
                // Fetch dashboard to get role if group exists
                val dashboard = apiService.getGroupDashboard(group.id)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    myGroups = listOf(group),
                    myRole = dashboard.myRole,
                    recentTransactions = MockDataProvider.getMockTransactions() // Still using mock for transactions as requested
                )
            } catch (e: Exception) {
                // If getMyGroup fails (e.g. 404), it means user is not in a group
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    myGroups = emptyList(),
                    myRole = null,
                    recentTransactions = emptyList()
                )
            }
        }
    }

    fun refreshAfterCreation() {
        loadHomeData()
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun getUserGroupRole(groupId: String): String {
        // This should ideally come from the API or session
        return "member"
    }
}
