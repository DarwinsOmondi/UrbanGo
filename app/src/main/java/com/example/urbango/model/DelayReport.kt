package com.example.urbango.model

data class DelayReport(
    val documentId: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val problemReport: String = "",
    val severity: String = "",
    val timestamp: Long = 0,
    val imageUri: String = "",
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val votedUsers: Map<String, String> = emptyMap()
)
