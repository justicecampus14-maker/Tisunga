package com.example.tisunga.data.remote.dto

/**
 * Password is NOT sent at registration.
 * It is set later via POST /auth/set-password with { userId, password }.
 */
data class RegisterRequest(
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val phone: String
)
