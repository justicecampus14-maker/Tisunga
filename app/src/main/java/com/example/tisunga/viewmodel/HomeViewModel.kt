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
        // If we just created a group in this session, don't let loadHomeData 
        // reset the state to empty or loading.
        if (hasCreatedGroupInSession && _uiState.value.myGroups.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = _uiState.value.myGroups.isEmpty(),
                userName = sessionManager.getUserName().ifEmpty { "Michael" },
                userPhone = sessionManager.getUserPhone().ifEmpty { "0882752624" }
            )
            
            try {
                val groups = apiService.getMyGroups()
                if (groups.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        myGroups = groups,
                        recentTransactions = MockDataProvider.getMockTransactions()
                    )
                } else if (hasCreatedGroupInSession) {
                    showMockData()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, myGroups = emptyList())
                }
            } catch (e: Exception) {
                if (hasCreatedGroupInSession) {
                    showMockData()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, myGroups = emptyList())
                }
            }
        }
    }
    
    private fun showMockData() {
        val mockGroups = MockDataProvider.getMockGroups()
        // Override the first group's name with the one from creation if available
        val displayGroups = if (lastCreatedGroupName != null) {
            listOf(mockGroups.first().copy(name = lastCreatedGroupName!!)) + mockGroups.drop(1)
        } else {
            mockGroups
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            myGroups = displayGroups,
            recentTransactions = MockDataProvider.getMockTransactions()
        )
    }

    fun refreshAfterCreation(groupName: String?) {
        hasCreatedGroupInSession = true
        lastCreatedGroupName = groupName
        showMockData()
    }

    fun logout() {
        sessionManager.clearSession()
    }
}
