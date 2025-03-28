package com.example.urbango.model

import kotlinx.serialization.Serializable

@Serializable
data class TrafficData(
    val latitude: Double,
    val longitude: Double,
    val delayTitle: String,
    val severityLevel: Int,
    val weather: String,
)