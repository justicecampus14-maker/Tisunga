package com.example.tisunga.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val location: String,
    val totalSavings: Double,
    val status: String
)
