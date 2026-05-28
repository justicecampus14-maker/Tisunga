package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.*
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
    val pendingUserId: String = "",
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
        
        // Sanitize technical/backend details to protect user from confusing technical info
        return when {
            msg.contains("prisma", ignoreCase = true) -> "A database error occurred. Please try again later."
            msg.contains("Internal Server Error", ignoreCase = true) -> "Something went wrong on our end."
            msg.contains("where minimum is", ignoreCase = true) -> {
                val value = msg.substringAfter("where minimum is").trim().takeWhile { it.isDigit() || it == '.' || it == ',' }
                if (value.isNotEmpty()) "The minimum amount required is MWK $value" 
                else "The amount entered is below the allowed minimum."
            }
            else -> msg
        }
    }

    fun register(firstName: String, middleName: String?, lastName: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = repo.register(RegisterRequest(firstName, middleName, lastName, phone))
                _uiState.value = _uiState.value.copy(
                    isLoading     = false,
                    isSuccess     = true,
                    pendingUserId = response.userId,
                    userPhone     = phone,
                    userName      = "$firstName $lastName"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleError(e))
            }
        }
    }

    fun verifyOtp(otp: String, purpose: String = "REGISTRATION") {
        val userId = _uiState.value.pendingUserId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                repo.verifyOtp(userId, otp, purpose)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleError(e))
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
                _uiState.value = _uiState.value.copy(errorMessage = handleError(e))
            }
        }
    }

    fun setPassword(password: String) {
        val userId = _uiState.value.pendingUserId
        if (userId.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = repo.setPassword(userId, password)
                saveSession(response)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    token     = response.token,
                    userId    = response.userId,
                    userName  = response.userName,
                    userPhone = response.userPhone
                )
                ApiClient.reset()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleError(e))
            }
        }
    }

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = repo.login(LoginRequest(phone, password))
                saveSession(response)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    token     = response.token,
                    userId    = response.userId,
                    userName  = response.userName,
                    userPhone = response.userPhone
                )
                ApiClient.reset()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleError(e))
            }
        }
    }

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
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleError(e))
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
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = handleError(e))
            }
        }
    }

    private fun saveSession(response: LoginResponse) {
        sessionManager.saveAuthToken(response.token)
        sessionManager.saveRefreshToken(response.refreshToken ?: "")
        sessionManager.saveUserData(
            response.userId,
            response.userName,
            response.userPhone,
            "MEMBER"
        )
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            ApiClient.reset()
            _uiState.value = AuthUiState()
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(isSuccess = false, errorMessage = "", successMessage = "")
    }
}
