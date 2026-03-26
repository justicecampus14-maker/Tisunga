package com.example.tisunga.data.repository

import com.example.tisunga.data.remote.ApiService
import com.example.tisunga.data.remote.dto.*

class AuthRepository(private val apiService: ApiService) {
    suspend fun login(request: LoginRequest) = apiService.login(request)
    suspend fun register(request: RegisterRequest) = apiService.register(request)
    suspend fun sendOtp(phone: String) = apiService.sendOtp(mapOf("phoneNumber" to phone))
    suspend fun verifyOtp(phone: String, otp: String) = apiService.verifyOtp(mapOf("phoneNumber" to phone, "otp" to otp))
    suspend fun createPassword(phone: String, password: String) = apiService.createPassword(mapOf("phoneNumber" to phone, "password" to password))
}
