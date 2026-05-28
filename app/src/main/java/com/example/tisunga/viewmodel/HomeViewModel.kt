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
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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

    private fun handleError(e: Exception): String {
        val rawMessage = when (e) {
            is ConnectException, is UnknownHostException -> "Server connection failed. Please check your internet."
            is SocketTimeoutException -> "Connection timed out. Try again later."
            is HttpException -> {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                    if (json.has("message")) json.get("message").asString
                    else if (json.has("error")) json.get("error").asString
                    else "Server error (${e.code()})"
                } catch (_: Exception) { "Server error (${e.code()})" }
            }
            else -> e.message
        }
        
        val msg = rawMessage ?: "An unexpected error occurred"
        
        return when {
            msg.contains("prisma", ignoreCase = true) -> "A database error occurred."
            msg.contains("Internal Server Error", ignoreCase = true) -> "Something went wrong on our end."
            msg.contains("Cannot ", ignoreCase = true) && msg.contains("/", ignoreCase = true) -> "The requested service is currently unavailable."
            msg.contains("route", ignoreCase = true) -> "Connection issue. Please try again."
            msg.contains("where minimum is", ignoreCase = true) -> {
                val value = msg.substringAfter("where minimum is").trim().takeWhile { it.isDigit() || it == '.' || it == ',' }
                if (value.isNotEmpty()) "The minimum amount required is MWK $value" 
                else "Amount is below the minimum limit."
            }
            else -> msg
        }
    }

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
                    errorMessage = handleError(e)
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
