package com.example.tisunga.data.remote.dto

data class LoginResponse(
    val token: String,
    val userId: Int,
    val userName: String,
    val userPhone: String,
    val userRole: String
)
