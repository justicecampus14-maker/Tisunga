package com.example.tisunga.data.model

data class User(
    val id: Int,
    val firstName: String,
    val middleName: String? = null,
    val lastName: String,
    val phone: String,
    val nationalId: String? = null,
    val role: String = "member",
    val avatarUrl: String? = null,
    val isVerified: Boolean = false,
    val createdAt: String? = null
)
