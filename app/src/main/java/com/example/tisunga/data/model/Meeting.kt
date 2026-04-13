package com.example.tisunga.data.model

data class Meeting(
    val id: String,
    val groupId: String,
    val createdBy: String,
    val title: String,
    val agenda: String? = null,
    val location: String? = null,
    val scheduledAt: String,          // ISO date string e.g. "2025-05-10T10:00:00.000Z"
    val status: String,               // "SCHEDULED" | "ONGOING" | "COMPLETED" | "CANCELLED"
    val notes: String? = null,
    val notifiedAt: String? = null,
    val createdAt: String? = null,
    val creatorName: String? = null,
    val presentCount: Int = 0,
    val totalCount: Int = 0,
    val attendancePercent: Int = 0
)
