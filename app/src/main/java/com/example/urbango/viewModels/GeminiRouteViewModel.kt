package com.example.urbango.viewModels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbango.constants.Apikeys
import com.example.urbango.model.DelayReport
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import kotlinx.coroutines.launch

class GeminiRouteViewModel : ViewModel() {
    var routeResults = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)



    fun suggestRoute(
        locationOfDelay: List<DelayReport>,
        desiredDestination: String,
        startingLocation: String
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val query = """
    Suggest the best alternative route to reach $desiredDestination from $startingLocation, 
    avoiding traffic delays at these points: $locationOfDelay. 
    Explain the recommended route clearly, mentioning key roads and turns, 
    but keep the response focused only on the directions,roads and turns.
""".trimIndent()
                val response = model.generateContent(query)
                println(response.text)

                routeResults.value = response.text

            } catch (e: Exception) {
                errorMessage.value = "An error occurred: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
private val model = GenerativeModel(
    modelName = "gemini-1.5-flash-001",
    apiKey = Apikeys.geminiApiKey,
    safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
    )
)