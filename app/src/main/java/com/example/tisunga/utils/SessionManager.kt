package com.example.tisunga.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tisunga_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_PHONE = "user_phone"
        const val USER_ROLE = "user_role"
        const val GROUP_ROLES = "group_roles"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(USER_TOKEN, token).apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUserData(userId: Int, userName: String, userPhone: String, userRole: String) {
        prefs.edit().apply {
            putInt(USER_ID, userId)
            putString(USER_NAME, userName)
            putString(USER_PHONE, userPhone)
            putString(USER_ROLE, userRole)
        }.apply()
    }

    fun getUserName(): String = prefs.getString(USER_NAME, "") ?: ""
    fun getUserPhone(): String = prefs.getString(USER_PHONE, "") ?: ""
    fun getUserRole(): String = prefs.getString(USER_ROLE, "member") ?: "member"

    fun saveGroupRoles(roles: Map<Int, String>) {
        val json = gson.toJson(roles)
        prefs.edit().putString(GROUP_ROLES, json).apply()
    }

    fun getGroupRole(groupId: Int): String? {
        val json = prefs.getString(GROUP_ROLES, null) ?: return null
        val type = object : TypeToken<Map<Int, String>>() {}.type
        val roles: Map<Int, String> = gson.fromJson(json, type)
        return roles[groupId]
    }

    fun getGroupRolesMap(): Map<Int, String> {
        val json = prefs.getString(GROUP_ROLES, null) ?: return emptyMap()
        val type = object : TypeToken<Map<Int, String>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
