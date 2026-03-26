package com.example.tisunga.data.remote.dto

data class RegisterRequest(
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val phone: String,
    val password: String
)
