package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Event
import com.example.tisunga.data.remote.ApiService

class EventRepository(private val apiService: ApiService) {
    suspend fun getGroupEvents(groupId: Int) = apiService.getGroupEvents(groupId)
    suspend fun createEvent(event: Event) = apiService.createEvent(event)
    suspend fun closeEvent(id: Int) = apiService.closeEvent(id)
    suspend fun contributeToEvent(id: Int, amount: Double) = apiService.contributeToEvent(id, mapOf("amount" to amount))
}
