// UserPointsViewModel.kt
package com.example.urbango.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbango.model.PointsData
import com.example.urbango.repository.SupabaseClient.client
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi

class UserPointsViewModel : ViewModel() {
    private val _userPoints = MutableStateFlow(0)
    val userPoints: StateFlow<Int> = _userPoints

    private val _userPointList = MutableStateFlow<List<PointsData>>(emptyList())
    val userPointList: StateFlow<List<PointsData>> = _userPointList

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    internal val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    init {
        fetchAllUserPoints()
    }

    fun savePointsToSupabase(points: Int, userName: String, userEmail: String) {
        val pointsData = PointsData(points, userName, userEmail)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loadingState.value = true
                client.postgrest["userpoints"].upsert(pointsData, onConflict = "userEmail")
                _userPoints.value = points
                _loadingState.value = false
            } catch (e: Exception) {
                Log.e("UserPointsViewModel", "Error saving points: ${e.message}", e)
                _errorState.value = when (e) {
                    is io.github.jan.supabase.exceptions.BadRequestRestException -> "Database error: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _loadingState.value = false
            }
        }
    }

    fun updateUserPoints(points: Int, userEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loadingState.value = true
                client.postgrest["userpoints"]
                    .update(mapOf("points" to points)) {
                        filter { eq("userEmail", userEmail) }
                    }
                _userPoints.value = points
                _loadingState.value = false
            } catch (e: Exception) {
                Log.e("UserPointsViewModel", "Error updating points: ${e.message}", e)
                _errorState.value = when (e) {
                    is io.github.jan.supabase.exceptions.BadRequestRestException -> "Database error: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _loadingState.value = false
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun fetchAllUserPoints() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loadingState.value = true
                val response = client.postgrest["userpoints"].select()
                Log.d("UserPointsViewModel", "Raw JSON: $response")
                val results = response.decodeList<PointsData>().sortedByDescending { it.points }
                _userPointList.value = results
                Log.d("UserPointsViewModel", "Fetched all points: $results")
                _userPoints.value = results.sumOf { it.points }
                _loadingState.value = false
            } catch (e: Exception) {
                Log.e("UserPointsViewModel", "Error fetching all points: ${e.message}", e)
                _errorState.value = when (e) {
                    is io.github.jan.supabase.exceptions.BadRequestRestException -> "Database error: ${e.message}"
                    is kotlinx.serialization.MissingFieldException -> "Data format error: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _loadingState.value = false
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun fetchSingleUserPoints(userEmail: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loadingState.value = true
                val response = client.postgrest["userpoints"]
                    .select {
                        filter { eq("userEmail", userEmail) }
                    }
                Log.d("UserPointsViewModel", "Raw JSON: ${response}")
                val userPoint = response.decodeList<PointsData>()
                Log.d("UserPointsViewModel", "Fetched points for $userEmail: $userPoint")
                _userPoints.value = userPoint.firstOrNull()?.points ?: 0
                _loadingState.value = false
            } catch (e: Exception) {
                Log.e("UserPointsViewModel", "Error fetching user points: ${e.message}", e)
                _errorState.value = when (e) {
                    is io.github.jan.supabase.exceptions.BadRequestRestException -> "Database error: ${e.message}"
                    is kotlinx.serialization.MissingFieldException -> "Data format error: ${e.message}"
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _loadingState.value = false
            }
        }
    }
}