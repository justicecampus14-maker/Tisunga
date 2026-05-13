package com.example.tisunga.utils

object Constants {
    /** 
     * Production Backend URL
     * Note: Retrofit requires the base URL to end with a trailing slash.
     * We include /api/v1/ as the endpoints in ApiService are defined relative to it.
     */
    const val BASE_URL = "https://tisunga-backend.onrender.com/api/v1/"

    // Set to false for production
    const val IS_DEVELOPMENT_MODE = false

    const val TOKEN_KEY = "auth_token"
    const val USER_ID_KEY = "user_id"
    const val USER_ROLE_KEY = "user_role"
    const val USER_NAME_KEY = "user_name"
    const val USER_PHONE_KEY = "user_phone"
    const val CURRENCY = "MK"
    const val DEFAULT_INTEREST_RATE = 15.0
}
