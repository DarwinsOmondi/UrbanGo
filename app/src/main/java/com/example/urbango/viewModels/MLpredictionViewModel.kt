package com.example.urbango.viewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbango.model.PredictionResult
import com.example.urbango.model.TrafficData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PredictionViewModelML : ViewModel() {
    private val trafficDelayNN = TrafficDelayNN()

    private val _predictionResults = MutableStateFlow<List<PredictionResult>>(emptyList())
    val predictionResults: StateFlow<List<PredictionResult>> = _predictionResults.asStateFlow()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Inner Neural Network class (unchanged)
    private inner class TrafficDelayNN {
        private val inputSize = 6  // Updated to match new TrafficData fields
        private val hiddenSize = 16
        private val outputSize = 4 // For severity, delay type, day, and time

        // Weights and biases
        private val w1 = Array(inputSize) { DoubleArray(hiddenSize) { Math.random() - 0.5 } }
        private val b1 = DoubleArray(hiddenSize) { 0.0 }
        private val w2 = Array(hiddenSize) { DoubleArray(outputSize) { Math.random() - 0.5 } }
        private val b2 = DoubleArray(outputSize) { 0.0 }

        // Activation functions
        private fun sigmoid(x: Double): Double = 1.0 / (1.0 + Math.exp(-x))
        private fun relu(x: Double): Double = if (x > 0) x else 0.0

        private fun getWeatherValue(weather: String): Double {
            return when (weather.lowercase()) {
                "clear" -> 0.1
                "light rain" -> 0.3
                "moderate rain" -> 0.5
                "heavy rain" -> 0.8
                "cloudy" -> 0.2
                "foggy" -> 0.4
                "snow" -> 0.9
                else -> 0.5
            }
        }

        private fun normalizeInput(data: TrafficData): DoubleArray {
            val currentTime = Date()
            val timeInMinutes =
                SimpleDateFormat("HH", Locale.getDefault()).format(currentTime).toInt() * 60 +
                        SimpleDateFormat("mm", Locale.getDefault()).format(currentTime).toInt()

            return doubleArrayOf(
                (data.latitude + 90) / 180, // Normalize latitude (-90 to 90) to 0-1
                (data.longitude + 180) / 360, // Normalize longitude (-180 to 180) to 0-1
                data.severityLevel.toDouble() / 5, // Normalize severity (0-5) to 0-1
                getWeatherValue(data.weather),
                timeInMinutes.toDouble() / (24 * 60), // Normalize time (0-1440) to 0-1
                when (data.delayTitle.lowercase()) { // Encode delay type
                    "minor delay" -> 0.2
                    "delay" -> 0.5
                    "congestion" -> 0.8
                    else -> 0.5
                }
            )
        }

        private fun getSeverityFromOutput(value: Double): Pair<String, Float> {
            val severity = when {
                value < 0.33 -> "Low severity"
                value < 0.66 -> "Medium severity"
                else -> "High severity"
            }
            val confidence = when (severity) {
                "Low severity" -> 1 - value
                "Medium severity" -> 1 - 2 * Math.abs(value - 0.5)
                else -> value
            }.toFloat()
            return Pair(severity, confidence.coerceIn(0f, 1f))
        }

        private fun getDelayTypeFromOutput(value: Double): Pair<String, Float> {
            val delayType = when {
                value < 0.33 -> "Minor delay"
                value < 0.66 -> "Delay"
                else -> "Congestion"
            }
            val confidence = when (delayType) {
                "Minor delay" -> 1 - value
                "Delay" -> 1 - 2 * Math.abs(value - 0.5)
                else -> value
            }.toFloat()
            return Pair(delayType, confidence.coerceIn(0f, 1f))
        }

        private fun getDayFromOutput(value: Double): Pair<String, Float> {
            val days =
                listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val index = (value * 6).roundToInt().coerceIn(0, 6)
            val confidence = (1 - 2 * Math.abs(value - index / 6.0)).toFloat().coerceIn(0f, 1f)
            return Pair(days[index], confidence)
        }

        private fun getTimeFromOutput(value: Double): Pair<String, Float> {
            val minutes = (value * 24 * 60).roundToInt()
            val hour = minutes / 60
            val minute = minutes % 60
            val timeStr = String.format("%02d:%02d", hour, minute)
            return Pair(timeStr, 1.0f)
        }

        private fun getLocationDescription(lat: Double, lon: Double): String {
            return when {
                lat in 41.0..42.0 && lon in -88.0..-87.0 -> "Chicago area"
                lat in 40.0..41.0 && lon in -75.0..-74.0 -> "New York area"
                lat in 51.0..52.0 && lon in -1.0..0.0 -> "London area"
                else -> "coordinates (${"%.4f".format(lat)}, ${"%.4f".format(lon)})"
            }
        }

        private fun getWeatherDescription(weather: String): String {
            return when (weather.lowercase()) {
                "clear" -> "Clear sky"
                "light rain" -> "Light rain"
                "moderate rain" -> "Moderate rain"
                "heavy rain" -> "Heavy rain"
                "cloudy" -> "Cloudy"
                "foggy" -> "Foggy"
                "snow" -> "Snow"
                else -> "Unknown weather"
            }
        }

        suspend fun predict(data: TrafficData): PredictionResult =
            withContext(Dispatchers.Default) {
                val input = normalizeInput(data)

                val hidden = DoubleArray(hiddenSize)
                for (i in 0 until hiddenSize) {
                    var sum = b1[i]
                    for (j in 0 until inputSize) {
                        sum += input[j] * w1[j][i]
                    }
                    hidden[i] = relu(sum)
                }

                val output = DoubleArray(outputSize)
                for (i in 0 until outputSize) {
                    var sum = b2[i]
                    for (j in 0 until hiddenSize) {
                        sum += hidden[j] * w2[j][i]
                    }
                    output[i] = sigmoid(sum)
                }

                val (severity, severityConf) = getSeverityFromOutput(output[0])
                val (delayType, delayTypeConf) = getDelayTypeFromOutput(output[1])
                val (day, dayConf) = getDayFromOutput(output[2])
                val (time, timeConf) = getTimeFromOutput(output[3])

                return@withContext PredictionResult(
                    predictedSeverity = severity,
                    predictedDelayType = delayType,
                    predictedDay = day,
                    predictedTime = time,
                    severityConfidence = severityConf,
                    delayTypeConfidence = delayTypeConf,
                    dayConfidence = dayConf,
                    timeConfidence = timeConf,
                    location = getLocationDescription(data.latitude, data.longitude),
                    weather = getWeatherDescription(data.weather)
                )
            }
    }

    fun predictTrafficDelay(trafficData: TrafficData) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val prediction = trafficDelayNN.predict(trafficData)
                // For single prediction, append to existing list or replace it
                _predictionResults.value = listOf(prediction)
            } catch (e: Exception) {
                _errorMessage.postValue("Prediction failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun predictTrafficDelays(trafficDataList: List<TrafficData>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val predictions = mutableListOf<PredictionResult>()
                trafficDataList.forEach { data ->
                    val prediction = trafficDelayNN.predict(data)
                    predictions.add(prediction)
                }
                _predictionResults.value = predictions
            } catch (e: Exception) {
                _errorMessage.postValue("Prediction failed: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun extractDayAndTime(timestamp: String): Pair<String, String> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSX")
        val dateTime = LocalDateTime.parse(timestamp, formatter)

        val dayOfWeek =
            dateTime.dayOfWeek.toString().lowercase().replaceFirstChar { it.uppercase() }
        val time = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"))

        return Pair(dayOfWeek, time)
    }
}