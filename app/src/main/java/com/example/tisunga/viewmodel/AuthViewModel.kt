package com.example.tisunga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.LoginRequest
import com.example.tisunga.data.remote.dto.RegisterRequest
import com.example.tisunga.utils.MockDataProvider
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = "",
    val token: String = "",
    val userId: Int = -1,
    val userName: String = "",
    val userPhone: String = "",
    val userRole: String = "member"
)

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    fun login(phone: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                val response = apiService.login(LoginRequest(phone, password))
                sessionManager.saveAuthToken(response.token)
                sessionManager.saveUserData(response.userId, response.userName, response.userPhone, response.userRole)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    token = response.token,
                    userId = response.userId,
                    userName = response.userName,
                    userPhone = response.userPhone,
                    userRole = response.userRole
                )
            } catch (e: Exception) {
                // DEVELOPMENT MODE: Use mock data when no network
                android.util.Log.d("AuthViewModel", "Network error, using mock data: ${e.message}")
                val mockUser = MockDataProvider.getMockUser()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    token = MockDataProvider.MOCK_TOKEN,
                    userId = mockUser.id,
                    userName = "${mockUser.firstName} ${mockUser.lastName}",
                    userPhone = mockUser.phone,
                    userRole = mockUser.role
                )
            }
        }
    }

    fun register(firstName: String, middleName: String?, lastName: String, phone: String) {
        // Simplified: registration password will be set in CreatePasswordScreen
        _uiState.value = _uiState.value.copy(
            userName = "$firstName $lastName",
            userPhone = phone
        )
    }

    fun verifyOtp(phone: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                apiService.verifyOtp(mapOf("phoneNumber" to phone, "otp" to code))
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true) // Bypass for dev
            }
        }
    }

    fun sendOtp(phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                apiService.sendOtp(mapOf("phoneNumber" to phone))
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Code resent")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Code resent (Mock)")
            }
        }
    }

    fun createPassword(phone: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
            try {
                // In a real app, we'd send the full registration data here or link it to the previously verified session
                val response = apiService.createPassword(mapOf("phoneNumber" to phone, "password" to password))
                sessionManager.saveAuthToken(response.token)
                sessionManager.saveUserData(response.userId, response.userName, response.userPhone, response.userRole)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                val mockUser = MockDataProvider.getMockUser()
                sessionManager.saveAuthToken(MockDataProvider.MOCK_TOKEN)
                sessionManager.saveUserData(mockUser.id, "${mockUser.firstName} ${mockUser.lastName}", mockUser.phone, mockUser.role)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
