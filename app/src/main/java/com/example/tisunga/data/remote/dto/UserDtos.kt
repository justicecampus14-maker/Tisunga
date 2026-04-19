package com.example.tisunga.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id")          val id: String,
    @SerializedName("phone")       val phone: String? = null,
    @SerializedName("firstName")   val firstName: String? = null,
    @SerializedName("lastName")    val lastName: String? = null,
    @SerializedName("middleName")  val middleName: String? = null,
    @SerializedName("avatarUrl")   val avatarUrl: String? = null,
    @SerializedName("isVerified")  val isVerified: Boolean = false,
    @SerializedName("createdAt")   val createdAt: String? = null,
    @SerializedName("memberships") val memberships: List<UserMembershipDto>? = null
)

data class UserMembershipDto(
    @SerializedName("role")  val role: String,
    @SerializedName("group") val group: UserGroupDto
)

data class UserGroupDto(
    @SerializedName("id")        val id: String,
    @SerializedName("name")      val name: String,
    @SerializedName("groupCode") val groupCode: String
)
