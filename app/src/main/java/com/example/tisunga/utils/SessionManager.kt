package com.example.tisunga.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tisunga_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val USER_TOKEN         = "user_token"
        const val REFRESH_TOKEN      = "refresh_token"
        const val USER_ID            = "user_id"
        const val USER_NAME          = "user_name"
        const val USER_PHONE         = "user_phone"
        const val USER_ROLE          = "user_role"
        const val GROUP_ROLES        = "group_roles"
    }

    fun saveAuthToken(token: String) =
        prefs.edit().putString(USER_TOKEN, token).apply()

    fun fetchAuthToken(): String? =
        prefs.getString(USER_TOKEN, null)

    fun saveRefreshToken(token: String) =
        prefs.edit().putString(Companion.REFRESH_TOKEN, token).apply()

    fun fetchRefreshToken(): String? =
        prefs.getString(Companion.REFRESH_TOKEN, null)

    fun saveUserData(userId: String, userName: String, userPhone: String, userRole: String) {
        prefs.edit().apply {
            putString(USER_ID, userId)
            putString(USER_NAME, userName)
            putString(USER_PHONE, userPhone)
            putString(USER_ROLE, userRole)
        }.apply()
    }

    fun getUserId(): String    = prefs.getString(USER_ID, "") ?: ""
    fun getUserName(): String  = prefs.getString(USER_NAME, "") ?: ""
    fun getUserPhone(): String = prefs.getString(USER_PHONE, "") ?: ""
    fun getUserRole(): String  = prefs.getString(USER_ROLE, "MEMBER") ?: "MEMBER"

    fun saveGroupRoles(roles: Map<String, String>) {
        prefs.edit().putString(GROUP_ROLES, gson.toJson(roles)).apply()
    }

    fun getGroupRole(groupId: String): String? {
        val json = prefs.getString(GROUP_ROLES, null) ?: return null
        val type = object : TypeToken<Map<String, String>>() {}.type
        val roles: Map<String, String> = gson.fromJson(json, type)
        return roles[groupId]
    }

    fun isLoggedIn(): Boolean = !fetchAuthToken().isNullOrBlank()

    fun clearSession() = prefs.edit().clear().apply()
}
