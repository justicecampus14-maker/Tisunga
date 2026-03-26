package com.example.tisunga.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val phone: String,
    val role: String
)
