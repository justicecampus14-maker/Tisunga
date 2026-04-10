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
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val MIDDLE_NAME = "middle_name"
        const val NATIONAL_ID = "national_id"
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

    fun saveFullUserData(
        userId: Int,
        firstName: String,
        lastName: String,
        middleName: String?,
        phone: String,
        role: String,
        nationalId: String? = null
    ) {
        val editor = prefs.edit()
        editor.putInt(USER_ID, userId)
        editor.putString(FIRST_NAME, firstName)
        editor.putString(LAST_NAME, lastName)
        editor.putString(MIDDLE_NAME, middleName)
        editor.putString(USER_NAME, "$firstName $lastName")
        editor.putString(USER_PHONE, phone)
        editor.putString(USER_ROLE, role)
        
        if (nationalId != null && nationalId.isNotBlank()) {
            editor.putString(NATIONAL_ID, nationalId)
        } else {
            editor.remove(NATIONAL_ID)
        }
        editor.apply()
    }

    fun getUserName(): String = prefs.getString(USER_NAME, "") ?: ""
    fun getUserPhone(): String = prefs.getString(USER_PHONE, "") ?: ""
    fun getUserRole(): String = prefs.getString(USER_ROLE, "member") ?: "member"
    fun getFirstName(): String = prefs.getString(FIRST_NAME, "") ?: ""
    fun getLastName(): String = prefs.getString(LAST_NAME, "") ?: ""
    fun getMiddleName(): String? = prefs.getString(MIDDLE_NAME, null)
    fun getNationalId(): String? = prefs.getString(NATIONAL_ID, null)

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
