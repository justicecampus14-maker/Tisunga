package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Event
import com.example.tisunga.data.remote.ApiService
import com.example.tisunga.data.remote.dto.ContributeRequest
import com.example.tisunga.data.remote.dto.CreateEventRequest

class EventRepository(private val apiService: ApiService) {
    suspend fun getGroupEvents(groupId: String) = apiService.getGroupEvents(groupId)
    
    suspend fun createEvent(groupId: String, event: Event) = apiService.createEventTyped(
        groupId = groupId,
        body = CreateEventRequest(
            type = "GENERAL",
            title = event.title,
            date = event.endDate ?: "",
            amountType = if (event.targetAmount != null) "TARGET" else "VOLUNTARY",
            amount = event.targetAmount ?: 0.0,
            description = event.description
        )
    )

    suspend fun closeEvent(id: String) = apiService.closeEvent(id)
    suspend fun contributeToEvent(id: String, amount: Double) = apiService.contributeToEvent(id, ContributeRequest(amount = amount))
}
