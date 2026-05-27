package com.example.tisunga.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /auth/login
 */
data class LoginRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String
)

/**
 * Response from POST /auth/register
 */
data class RegisterResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("userId") val userIdField: String? = null,
    @SerializedName("tempUserId") val tempUserId: String? = null,
    @SerializedName("id") val id: String? = null
) {
    /** 
     * Convenience getter to match common usage in ViewModels.
     * Tries multiple possible field names for the user ID.
     */
    val userId: String get() = userIdField ?: tempUserId ?: id ?: ""
}

/**
 * Response from POST /auth/verify-otp
 */
data class VerifyOtpResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("isVerified") val isVerified: Boolean = false
)

/**
 * Generic response for messages from the backend
 */
data class MessageResponse(
    @SerializedName("message") val message: String? = null
)

/**
 * Response from POST /auth/forgot-password
 */
data class ForgotPasswordResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("resetToken") val resetToken: String? = null,
    @SerializedName("userId") val userId: String? = null
)
