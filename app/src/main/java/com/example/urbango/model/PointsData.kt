package com.example.urbango.model

import kotlinx.serialization.Serializable

@Serializable
data class PointsData(
    val points: Int,
    val userName: String
)
