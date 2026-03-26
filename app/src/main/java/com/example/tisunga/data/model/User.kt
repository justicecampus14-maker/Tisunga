package com.example.tisunga.data.model

data class User(
    val id: Int,
    val firstName: String,
    val middleName: String? = null,
    val lastName: String,
    val phone: String,
    val role: String = "member"
)
