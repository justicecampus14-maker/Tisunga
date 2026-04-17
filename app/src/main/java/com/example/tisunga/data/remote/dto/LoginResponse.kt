package com.example.tisunga.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.tisunga.data.model.Group

/**
 * Backend login returns:
 *   { accessToken, refreshToken, user: { id, firstName, phone } }
 * set-password returns the same shape.
 */
data class LoginResponse(
    @SerializedName("accessToken")  val accessToken: String = "",
    @SerializedName("refreshToken") val refreshToken: String = "",
    @SerializedName("user")         val user: UserPayload? = null
) {
    // Convenience accessors so existing callers keep working
    val token: String get() = accessToken
    val userId: String get() = user?.id ?: ""
    val userName: String get() = user?.firstName ?: ""
    val userPhone: String get() = user?.phone ?: ""
}

data class UserPayload(
    @SerializedName("id")        val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("phone")     val phone: String
)
