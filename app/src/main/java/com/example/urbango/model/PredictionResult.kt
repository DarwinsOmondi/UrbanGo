package com.example.urbango.model

data class PredictionResult(
    val predictedSeverity: String,
    val predictedDelayType: String,
    val predictedDay: String,
    val predictedTime: String,
    val severityConfidence: Float,
    val delayTypeConfidence: Float,
    val dayConfidence: Float,
    val timeConfidence: Float,
    val location: String,
    val weather: String
)
