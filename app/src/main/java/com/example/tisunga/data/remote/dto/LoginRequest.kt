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
    @SerializedName("message") val message: String,
    @SerializedName("tempUserId") val tempUserId: String
) {
    /** 
     * Convenience getter to match common usage in ViewModels 
     */
    val userId: String get() = tempUserId
}

/**
 * Response from POST /auth/verify-otp
 */
data class VerifyOtpResponse(
    @SerializedName("message") val message: String,
    @SerializedName("isVerified") val isVerified: Boolean
)

/**
 * Generic response for messages from the backend
 */
data class MessageResponse(
    @SerializedName("message") val message: String
)

/**
 * Response from POST /auth/forgot-password
 */
data class ForgotPasswordResponse(
    @SerializedName("message") val message: String,
    @SerializedName("resetToken") val resetToken: String? = null,
    @SerializedName("userId") val userId: String? = null
)
