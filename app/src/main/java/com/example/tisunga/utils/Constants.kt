package com.example.tisunga.utils

object Constants {
    // For emulator: 10.0.2.2 points to localhost on your PC
    // For real device: use your PC's actual IP on same WiFi
    // e.g. "http://192.168.1.100:3000/api/"
    const val BASE_URL = "http://10.0.2.2:3000/api/"

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
