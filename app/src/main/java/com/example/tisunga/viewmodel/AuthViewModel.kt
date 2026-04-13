package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.LoginRequest
import com.example.tisunga.data.remote.dto.RegisterRequest
import com.example.tisunga.data.repository.AuthRepository
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    // Stored across the register → OTP → set-password steps
    val pendingUserId: String = "",
    // Populated after login / set-password
    val token: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val userRole: String = "MEMBER"
)

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val repo = AuthRepository(ApiClient.getClient())

    private fun handleError(e: Exception): String {
        return when (e) {
            is ConnectException, is UnknownHostException -> 
                "Unable to connect to the server. Please check your internet connection and ensure the backend is running."
            is SocketTimeoutException -> 
                "Connection timed out. Please try again later."
            is HttpException -> {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    // Try to extract "message" from the backend error JSON
                    val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                    if (json.has("message")) json.get("message").asString
                    else "Server error (${e.code()})"
                } catch (_: Exception) {
                    "Server error (${e.code()})"
                }
            }
            else -> e.message ?: "An unexpected error occurred"
        }
    }

    // ── Step 1: Register ─────────────────────────────────────────────────

    fun register(firstName: String, middleName: String?, lastName: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = repo.register(
                    RegisterRequest(firstName, middleName, lastName, phone)
                )
                // Store userId so subsequent OTP / set-password steps can use it
                _uiState.value = _uiState.value.copy(
                    isLoading     = false,
                    isSuccess     = true,
                    pendingUserId = response.userId,
                    userPhone     = phone,
                    userName      = "$firstName $lastName"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = handleError(e)
                )
            }
        }
    }

    // ── Step 2: Verify OTP ───────────────────────────────────────────────

    fun verifyOtp(otp: String, purpose: String = "REGISTRATION") {
        val userId = _uiState.value.pendingUserId
        if (userId.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please register again.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                repo.verifyOtp(userId, otp, purpose)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = handleError(e)
                )
            }
        }
    }

    fun resendOtp(purpose: String = "REGISTRATION") {
        val userId = _uiState.value.pendingUserId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            try {
                repo.resendOtp(userId, purpose)
                _uiState.value = _uiState.value.copy(successMessage = "Code resent")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to resend")
            }
        }
    }

    // ── Step 3: Set password ─────────────────────────────────────────────

    fun setPassword(password: String) {
        val userId = _uiState.value.pendingUserId
        if (userId.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please register again.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = repo.setPassword(userId, password)
                sessionManager.saveAuthToken(response.accessToken)
                sessionManager.saveRefreshToken(response.refreshToken)
                sessionManager.saveUserData(
                    response.userId,
                    response.userName,
                    response.userPhone,
                    "MEMBER"
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    token     = response.accessToken,
                    userId    = response.userId,
                    userName  = response.userName,
                    userPhone = response.userPhone
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = handleError(e)
                )
            }
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = repo.login(LoginRequest(phone, password))
                sessionManager.saveAuthToken(response.accessToken)
                sessionManager.saveRefreshToken(response.refreshToken)
                sessionManager.saveUserData(
                    response.userId,
                    response.userName,
                    response.userPhone,
                    "MEMBER"
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    token     = response.accessToken,
                    userId    = response.userId,
                    userName  = response.userName,
                    userPhone = response.userPhone
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = handleError(e)
                )
            }
        }
    }

    // ── Forgot / reset password ──────────────────────────────────────────

    fun forgotPassword(phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = repo.forgotPassword(phone)
                _uiState.value = _uiState.value.copy(
                    isLoading     = false,
                    isSuccess     = true,
                    pendingUserId = response.userId ?: "",
                    successMessage = "OTP sent to your phone"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = handleError(e)
                )
            }
        }
    }

    fun resetPassword(newPassword: String) {
        val userId = _uiState.value.pendingUserId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                repo.resetPassword(userId, newPassword)
                _uiState.value = _uiState.value.copy(
                    isLoading      = false,
                    isSuccess      = true,
                    successMessage = "Password reset. Please sign in."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = handleError(e)
                )
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────

    fun logout() {
        viewModelScope.launch {
            try {
                val refreshToken = sessionManager.fetchRefreshToken() ?: ""
                repo.logout(refreshToken)
            } catch (_: Exception) { /* best-effort */ }
            sessionManager.clearSession()
            ApiClient.reset()
            _uiState.value = AuthUiState()
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            isSuccess      = false,
            errorMessage   = "",
            successMessage = ""
        )
    }
}
