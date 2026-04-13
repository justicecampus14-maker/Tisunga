package com.example.tisunga.data.model

data class MeetingAttendance(
    val id: String,
    val meetingId: String,
    val userId: String,
    val status: String,
    val note: String? = null,
    val markedAt: String? = null,
    val user: AttendanceUser? = null
)

data class AttendanceUser(
    val id: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val avatarUrl: String? = null
)

data class AttendanceSummary(
    val total: Int,
    val present: Int,
    val absent: Int,
    val excused: Int,
    val attendancePercent: Int
)
