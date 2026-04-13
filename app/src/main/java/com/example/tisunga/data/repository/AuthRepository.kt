package com.example.tisunga.data.repository

import com.example.tisunga.data.remote.ApiService
import com.example.tisunga.data.remote.dto.*

class AuthRepository(private val apiService: ApiService) {

    /** Step 1 — POST /auth/register → returns userId */
    suspend fun register(request: RegisterRequest) =
        apiService.register(request)

    /** Step 2 — POST /auth/verify-otp: { userId, otp, purpose } */
    suspend fun verifyOtp(userId: String, otp: String, purpose: String) =
        apiService.verifyOtp(
            mapOf("userId" to userId, "otp" to otp, "purpose" to purpose)
        )

    /** Resend OTP: { userId, purpose } */
    suspend fun resendOtp(userId: String, purpose: String) =
        apiService.resendOtp(
            mapOf("userId" to userId, "purpose" to purpose)
        )

    /** Step 3 — POST /auth/set-password: { userId, password } → tokens */
    suspend fun setPassword(userId: String, password: String) =
        apiService.setPassword(
            mapOf("userId" to userId, "password" to password)
        )

    /** Normal sign-in */
    suspend fun login(request: LoginRequest) =
        apiService.login(request)

    /** Forgot password — GET OTP */
    suspend fun forgotPassword(phone: String) =
        apiService.forgotPassword(mapOf("phone" to phone))

    /** Reset password with new value */
    suspend fun resetPassword(userId: String, newPassword: String) =
        apiService.resetPassword(
            mapOf("userId" to userId, "newPassword" to newPassword)
        )

    /** Logout */
    suspend fun logout(refreshToken: String) =
        apiService.logout(mapOf("refreshToken" to refreshToken))
}
