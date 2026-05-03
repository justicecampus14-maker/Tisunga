package com.example.tisunga.data.repository

import com.example.tisunga.data.model.Event
import com.example.tisunga.data.remote.ApiService
import com.example.tisunga.data.remote.dto.ContributeRequest
import com.example.tisunga.data.remote.dto.CreateEventRequest

class EventRepository(private val apiService: ApiService) {
    suspend fun getGroupEvents(groupId: String) = apiService.getGroupEvents(groupId)
    
    suspend fun createEvent(groupId: String, event: Event) = apiService.createEvent(
        groupId = groupId,
        body = mapOf(
            "title" to event.title,
            "description" to event.description,
            "eventDate" to (event.endDate ?: ""),
            "fixedAmount" to (event.targetAmount ?: 0.0),
            "type" to "OTHER" // Default to OTHER if not specified
        )
    )

    suspend fun closeEvent(id: String) = apiService.closeEvent(id)
    suspend fun contributeToEvent(id: String, amount: Double) = apiService.contributeToEvent(id, ContributeRequest(amount = amount))
}
