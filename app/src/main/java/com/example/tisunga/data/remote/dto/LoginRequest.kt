package com.example.tisunga.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String
)

data class RegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("tempUserId") val tempUserId: String
) {
    val userId: String get() = tempUserId
}

data class VerifyOtpResponse(
    @SerializedName("message") val message: String,
    @SerializedName("isVerified") val isVerified: Boolean
)

data class MessageResponse(
    @SerializedName("message") val message: String
)

data class ForgotPasswordResponse(
    @SerializedName("message") val message: String,
    @SerializedName("resetToken") val resetToken: String? = null,
    @SerializedName("userId") val userId: String? = null
)
