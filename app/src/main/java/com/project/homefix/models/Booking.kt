package com.project.homefix.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Booking(
    val id: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val details: String = "",
    val serviceName: String = "",
    val serviceId: String = "",
    val providerId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val status: String = "pending",
    val createdAt: Long = 0L
)

object BookingStore {
    var selectedService: Service? by mutableStateOf(null)
    var providerSummaryPage: Int by mutableStateOf(0)
}
