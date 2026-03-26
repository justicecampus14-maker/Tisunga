package com.example.tisunga.data.model

data class Event(
    val id: Int,
    val groupId: Int,
    val type: String, // "Wedding", "Birthday", "Funeral", "Other"
    val title: String,
    val date: String,
    val amountType: String, // "Fixed", "Flexible"
    val amount: Double? = null,
    val description: String,
    val status: String, // "active", "closed"
    val raisedAmount: Double = 0.0
)
