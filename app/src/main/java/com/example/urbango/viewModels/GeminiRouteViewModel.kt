package com.example.urbango.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbango.constants.Apikeys
import com.example.urbango.model.DelayReport
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GeminiRouteViewModel : ViewModel() {
    // State variables
    var routeResults by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var currentJob: Job? = null

    // Gemini model initialization
    private val model by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash-001",
            apiKey = Apikeys.geminiApiKey,
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
            )
        )
    }

    fun suggestRoute(
        locationOfDelay: List<DelayReport>,
        desiredDestination: String,
        startingLocation: String
    ) {
        currentJob?.cancel()

        currentJob = viewModelScope.launch {
            isLoading = true
            errorMessage = null
            routeResults = null

            try {
                val delayPoints = if (locationOfDelay.isNotEmpty()) {
                    locationOfDelay.joinToString(
                        separator = "\n",
                        prefix = "These are the reported delay points (latitude, longitude):\n",
                        postfix = "\nPlease avoid these areas if possible."
                    ) { report ->
                        "- (${report.latitude}, ${report.longitude})"
                    }
                } else {
                    "There are currently no reported delay points."
                }

                val prompt = content {
                    text(
                        """
                        I need you to act as a navigation assistant. Suggest the best alternative route 
                        from $startingLocation to $desiredDestination,explain the recommended route clearly, 
                        mentioning key roads and turns. considering the following information:
                        
                        $delayPoints
                        
                        Please provide:
                        1. A clear, step-by-step route description
                        2. Key roads and landmarks to follow
                        3. Estimated travel time (if possible)
                        4. Any alternative options if available
                        5. Important traffic considerations
                        
                        Keep the response concise but informative, focusing only on the directions.
                        """.trimIndent()
                    )
                }

                val response = model.generateContent(prompt)

                if (response.text.isNullOrEmpty()) {
                    errorMessage = "Received empty response from the API"
                } else {
                    routeResults = response.text
                }
            } catch (e: Exception) {
                errorMessage = when (e) {
                    is java.net.SocketTimeoutException -> "Request timed out. Please check your connection."
                    is java.net.UnknownHostException -> "No internet connection."
                    else -> "Failed to get route: ${e.localizedMessage ?: "Unknown error"}"
                }
                routeResults = null
            } finally {
                isLoading = false
                currentJob = null
            }
        }
    }

    fun clearRouteResults() {
        currentJob?.cancel()
        routeResults = null
        errorMessage = null
        isLoading = false
    }

    companion object {
        const val TEST_PROMPT = """
            Suggest the best alternative route to reach destination from start, 
            avoiding traffic delays. Explain the recommended route clearly, 
            mentioning key roads and turns.
        """
    }
}