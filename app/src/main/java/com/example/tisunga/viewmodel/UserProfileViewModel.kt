package com.example.tisunga.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.remote.ApiClient
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
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String? = null,
    val nationalId: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isProfileComplete: Boolean = true
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
                val user = apiService.getMyProfile()
                val isComplete = user.firstName.isNotBlank() && 
                               user.lastName.isNotBlank() && 
                               !user.nationalId.isNullOrBlank()

                _uiState.value = _uiState.value.copy(
                    firstName = user.firstName,
                    lastName = user.lastName,
                    middleName = user.middleName,
                    nationalId = user.nationalId ?: "",
                    phone = user.phone,
                    // Assuming avatarUrl is added to User model or handled specifically
                    // avatarUrl = user.avatarUrl, 
                    isLoading = false,
                    isProfileComplete = isComplete
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load profile"
                )
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, middleName: String?, nationalId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                val body = mutableMapOf<String, String>()
                body["firstName"] = firstName
                body["lastName"] = lastName
                middleName?.let { if (it.isNotBlank()) body["middleName"] = it }
                body["nationalId"] = nationalId
                
                val user = apiService.updateProfile(body)
                _uiState.value = _uiState.value.copy(
                    firstName = user.firstName,
                    lastName = user.lastName,
                    middleName = user.middleName,
                    nationalId = user.nationalId ?: "",
                    isLoading = false,
                    isProfileComplete = true,
                    successMessage = "Profile updated successfully"
                )
                // Update session manager if needed
                sessionManager.saveUserData(user.id, "${user.firstName} ${user.lastName}", user.phone, user.role)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to update profile"
                )
            }
        }
    }

    fun uploadAvatar(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                val file = uriToFile(uri, context)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                
                val user = apiService.uploadAvatar(body)
                _uiState.value = _uiState.value.copy(
                    // avatarUrl = user.avatarUrl,
                    isLoading = false,
                    successMessage = "Avatar updated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to upload avatar"
                )
            }
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val file = File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
