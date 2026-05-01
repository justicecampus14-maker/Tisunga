package com.example.tisunga.utils

object Constants {
    /** 
     * Wireless Network Connection:
     * Replace with your PC's IP from 'ipconfig' (Wireless LAN adapter Wi-Fi)
     * e.g., "http://192.168.1.5:3000/api/v1/"
     */
    const val BASE_URL = "http://192.168.137.1:3000/api/v1/"

    // Development flag
    const val IS_DEVELOPMENT_MODE = true

    const val TOKEN_KEY = "auth_token"
    const val USER_ID_KEY = "user_id"
    const val USER_ROLE_KEY = "user_role"
    const val USER_NAME_KEY = "user_name"
    const val USER_PHONE_KEY = "user_phone"
    const val CURRENCY = "MK"
    const val DEFAULT_INTEREST_RATE = 5.0
}
