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

class UserPointsViewModel : ViewModel() {
    private val _userPoints = MutableStateFlow(0)
    val userPoints: StateFlow<Int> = _userPoints

    val _userPointList = MutableStateFlow<List<PointsData>>(emptyList())
    val userPointList: StateFlow<List<PointsData>> = _userPointList

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    internal val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    init {
        fetchAllUserPoints()
    }

    fun savePointsToSupabase(points: Int, userName: String) {
        val pointsData = PointsData(points, userName)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loadingState.value = true
                client.postgrest["userpoints"].upsert(pointsData, onConflict = "userName")
                _userPoints.value = points
                _loadingState.value = false
            } catch (e: Exception) {
                Log.e("UserPointsViewModel", "Error saving points: ${e.message}", e)
                _errorState.value = e.message
                _loadingState.value = false
            }
        }
    }

    fun updateUserPoints(points: Int, userName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loadingState.value = true
                client.postgrest["userpoints"]
                    .update(mapOf("points" to points)) {
                        filter { eq("userName", userName) }
                    }
                _userPoints.value = points
                _loadingState.value = false
            } catch (e: Exception) {
                Log.e("UserPointsViewModel", "Error updating points: ${e.message}", e)
                _errorState.value = e.message
                _loadingState.value = false
            }
        }
    }

    fun fetchAllUserPoints() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loadingState.value = true
                val results = client.postgrest["userpoints"]
                    .select()
                    .decodeList<PointsData>()
                _userPointList.value = results
                Log.d("UserPointsViewModel", "Fetched all points: $results")
                _userPoints.value = results.sumOf { it.points }
                _loadingState.value = false
            } catch (e: Exception) {
                Log.e("UserPointsViewModel", "Error fetching all points: ${e.message}", e)
                _errorState.value = e.message
                _loadingState.value = false
            }
        }
    }

    fun fetchSingleUserPoints(userName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loadingState.value = true
                val userPoint = client.postgrest["userpoints"]
                    .select {
                        filter { eq("userName", userName) }
                    }
                    .decodeList<PointsData>()
                Log.d("UserPointsViewModel", "Fetched points for $userName: $userPoint")
                _userPoints.value = userPoint.firstOrNull()?.points ?: 0
                _loadingState.value = false
            } catch (e: Exception) {
                Log.e("UserPointsViewModel", "Error fetching user points: ${e.message}", e)
                _errorState.value = e.message
                _loadingState.value = false
            }
        }
    }
}