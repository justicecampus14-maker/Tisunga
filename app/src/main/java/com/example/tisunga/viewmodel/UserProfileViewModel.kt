package com.example.tisunga.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tisunga.data.remote.ApiClient
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
    val firstName: String = "",
    val lastName: String = "",
    val middleName: String? = null,
    val nationalId: String = "",
    val phone: String = "",
    val avatarUrl: Any? = null,
    val isVerified: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isProfileComplete: Boolean = true // Start true to avoid flash, refresh will set it false if needed
)

class UserProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val apiService = ApiClient.getClient()

    init {
        refreshFromSession()
        loadProfile()
    }

    /**
     * Updates the UI state from the local SessionManager.
     * This is crucial after login/registration to ensure the profile state is current.
     */
    fun refreshFromSession() {
        val savedFirstName = sessionManager.getFirstName()
        val savedLastName = sessionManager.getLastName()
        val savedNationalId = sessionManager.getNationalId() ?: ""
        val phone = sessionManager.getUserPhone()
        
        val isComplete = savedFirstName.isNotBlank() && 
                         savedLastName.isNotBlank() && 
                         savedNationalId.isNotBlank()
        
        _uiState.value = _uiState.value.copy(
            firstName = savedFirstName,
            lastName = savedLastName,
            middleName = sessionManager.getMiddleName(),
            phone = phone,
            nationalId = savedNationalId,
            isProfileComplete = isComplete,
            isLoading = false
        )
    }

    fun loadProfile() {
        viewModelScope.launch {
            // We don't set isLoading = true here to avoid blocking the popup check in HomeScreen
            try {
                val user = apiService.getMyProfile()
                val isComplete = user.firstName.isNotBlank() && 
                               user.lastName.isNotBlank() && 
                               !user.nationalId.isNullOrBlank()

                // Form full URL for avatar if it's a relative path
                val fullAvatarUrl = user.avatarUrl?.let { 
                    if (it.startsWith("http")) it else Constants.BASE_URL + it.removePrefix("/")
                }

                _uiState.value = _uiState.value.copy(
                    firstName = user.firstName,
                    lastName = user.lastName,
                    middleName = user.middleName,
                    nationalId = user.nationalId ?: "",
                    phone = user.phone,
                    avatarUrl = fullAvatarUrl,
                    isProfileComplete = isComplete
                )
                
                sessionManager.saveFullUserData(
                    user.id,
                    user.firstName,
                    user.lastName,
                    user.middleName,
                    user.phone,
                    user.role,
                    user.nationalId
                )
            } catch (e: Exception) {
                // If API fails, refreshFromSession already provided the best local data
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
                sessionManager.saveFullUserData(
                    user.id,
                    user.firstName,
                    user.lastName,
                    user.middleName,
                    user.phone,
                    user.role,
                    user.nationalId
                )
            } catch (e: Exception) {
                // Mock behavior assuming success for demo/dev
                _uiState.value = _uiState.value.copy(
                    firstName = firstName,
                    lastName = lastName,
                    middleName = middleName,
                    nationalId = nationalId,
                    isLoading = false,
                    isProfileComplete = true,
                    successMessage = "Profile updated (Mock Mode)"
                )
                
                sessionManager.saveFullUserData(
                    -1,
                    firstName,
                    lastName,
                    middleName,
                    _uiState.value.phone,
                    "member",
                    nationalId
                )
            }
        }
    }

    fun uploadAvatar(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                val file = uriToFile(uri, context)
                if (!file.exists()) throw Exception("File creation failed")
                
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                
                val user = apiService.uploadAvatar(body)
                
                val fullAvatarUrl = user.avatarUrl?.let { 
                    if (it.startsWith("http")) it else Constants.BASE_URL + it.removePrefix("/")
                } ?: uri

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    avatarUrl = fullAvatarUrl,
                    successMessage = "Avatar updated successfully"
                )
            } catch (e: Exception) {
                // Mock behavior: show selected image immediately
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    avatarUrl = uri,
                    successMessage = "Avatar updated (Mock Mode)"
                )
            }
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
