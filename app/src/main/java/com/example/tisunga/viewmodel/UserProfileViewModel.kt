package com.example.tisunga.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.remote.ApiClient
import com.example.tisunga.data.remote.dto.UserResponse
import com.example.tisunga.utils.Constants
import com.example.tisunga.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

data class UserProfileUiState(
    val user: UserResponse? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class UserProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiService.getMyProfile()
                _uiState.value = _uiState.value.copy(user = response, isLoading = false)
                
                // Sync session manager
                sessionManager.saveUserData(
                    userId = response.id,
                    userName = "${response.firstName ?: ""} ${response.lastName ?: ""}".trim(),
                    userPhone = response.phone ?: "",
                    userRole = response.memberships?.firstOrNull()?.role ?: "member"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, middleName: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null, successMessage = null)
            try {
                val body = mutableMapOf<String, String>()
                body["firstName"] = firstName
                body["lastName"] = lastName
                body["middleName"] = middleName ?: ""
                
                val response = apiService.updateProfile(body)
                _uiState.value = _uiState.value.copy(user = response, isUpdating = false, successMessage = "Profile updated successfully")
                
                // Update session
                sessionManager.saveUserData(
                    userId = response.id,
                    userName = "${response.firstName ?: ""} ${response.lastName ?: ""}".trim(),
                    userPhone = response.phone ?: "",
                    userRole = sessionManager.getUserRole()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUpdating = false, errorMessage = e.message ?: "Failed to update profile")
            }
        }
    }

    fun uploadAvatar(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, errorMessage = null, successMessage = null)
            try {
                val file = uriToFile(uri, context)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                
                val response = apiService.uploadAvatar(body)
                
                // Merge partial response with current state to avoid losing names/phone
                val currentUser = _uiState.value.user
                val mergedUser = response.copy(
                    firstName = response.firstName ?: currentUser?.firstName,
                    lastName = response.lastName ?: currentUser?.lastName,
                    phone = response.phone ?: currentUser?.phone,
                    middleName = response.middleName ?: currentUser?.middleName,
                    memberships = if (response.memberships == null) currentUser?.memberships else response.memberships
                )
                
                _uiState.value = _uiState.value.copy(user = mergedUser, isUpdating = false, successMessage = "Avatar updated successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUpdating = false, errorMessage = e.message ?: "Failed to upload avatar")
            }
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
