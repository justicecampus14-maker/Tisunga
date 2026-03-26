package com.example.tisunga.data.model

data class Contribution(
    val id: Int,
    val groupId: Int,
    val userId: Int,
    val userName: String,
    val amount: Double,
    val type: String, // "regular", "special", "event"
    val timestamp: String,
    val status: String,
    val phoneNumber: String? = null
)
