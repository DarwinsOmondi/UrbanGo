package com.example.urbango.viewModels

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbango.model.DelayReport
import com.example.urbango.model.TrafficData
import com.example.urbango.model.UiState
import com.example.urbango.repository.SupabaseClient.client
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Calendar
import java.util.Locale

class DelayReportViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _delayReports = MutableStateFlow<List<DelayReport>>(emptyList())
    val delayReports: StateFlow<List<DelayReport>> = _delayReports

    private val _trafficDelays = MutableStateFlow<List<TrafficData>>(emptyList())
    val trafficDelays: StateFlow<List<TrafficData>> = _trafficDelays

    init {
        startAutoRefresh()
        fetchDelayReports()
        fetchTrafficDelaysFromSupabase()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                fetchDelayReports()
                delay(1000)
            }
        }
    }

    fun saveDelayReport(
        latitude: Double,
        longitude: Double,
        problemReport: String,
        severity: String,
        imageUri:String
    ) {
        val user = auth.currentUser
        if (user == null) {
            _uploadState.value = UploadState.Error("User not authenticated")
            return
        }

        _uploadState.value = UploadState.Loading

        val reportData = hashMapOf(
            "userId" to user.uid,
            "latitude" to latitude,
            "longitude" to longitude,
            "problemReport" to problemReport,
            "severity" to severity,
            "imageUri" to imageUri,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("delays").add(reportData)
            .addOnSuccessListener {
                val userRef = db.collection("users").document(user.uid)
                userRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        userRef.set(hashMapOf("points" to 10))
                    } else {
                        userRef.update("points", FieldValue.increment(10))
                    }
                }
                _uploadState.value = UploadState.Success("Report saved successfully")
            }
            .addOnFailureListener { e ->
                _uploadState.value = UploadState.Error("Failed to save report: ${e.message}")
            }
    }

    fun fetchDelayReports() {
        db.collection("delays")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    _uploadState.value =
                        UploadState.Error("Failed to listen for updates: ${e.message}")
                    return@addSnapshotListener
                }

                val reports = snapshots?.documents?.mapNotNull { document ->
                    document.toObject(DelayReport::class.java)?.copy(documentId = document.id)
                } ?: emptyList()

                _delayReports.value = reports
            }
    }

    fun deleteDelayReport(reportId: String) {
        _uploadState.value = UploadState.Loading

        db.collection("delays").document(reportId).delete()
            .addOnSuccessListener {
                _uploadState.value = UploadState.Success("Report deleted successfully")
                _delayReports.value = _delayReports.value.filterNot { it.documentId == reportId }
            }
            .addOnFailureListener { e ->
                _uploadState.value = UploadState.Error("Failed to delete report: ${e.message}")
            }
    }

    fun upvoteReport(reportId: String) {
        val user = auth.currentUser ?: return
        val userId = user.uid

        db.collection("delays").document(reportId).get()
            .addOnSuccessListener { document ->
                val votedUsers = document.get("votedUsers") as? Map<String, String> ?: emptyMap()
                val reportOwnerId = document.getString("userId") ?: return@addOnSuccessListener

                when (votedUsers[userId]) {
                    "upvote" -> {
                        db.collection("delays").document(reportId).update(
                            "upvotes", FieldValue.increment(-1),
                            "votedUsers.$userId", FieldValue.delete()
                        )
                        db.collection("users").document(reportOwnerId)
                            .update("points", FieldValue.increment(-5))
                    }

                    "downvote" -> {
                        db.collection("delays").document(reportId).update(
                            "downvotes", FieldValue.increment(-1),
                            "upvotes", FieldValue.increment(1),
                            "votedUsers.$userId", "upvote"
                        )
                        db.collection("users").document(reportOwnerId).get()
                            .addOnSuccessListener { userDoc ->
                                if (!userDoc.exists()) {
                                    db.collection("users").document(reportOwnerId)
                                        .set(hashMapOf("points" to 10))
                                } else {
                                    db.collection("users").document(reportOwnerId)
                                        .update("points", FieldValue.increment(10))
                                }
                            }
                    }

                    else -> {
                        db.collection("delays").document(reportId).update(
                            "upvotes", FieldValue.increment(1),
                            "votedUsers.$userId", "upvote"
                        )
                        db.collection("users").document(reportOwnerId).get()
                            .addOnSuccessListener { userDoc ->
                                if (!userDoc.exists()) {
                                    db.collection("users").document(reportOwnerId)
                                        .set(hashMapOf("points" to 5))
                                } else {
                                    db.collection("users").document(reportOwnerId)
                                        .update("points", FieldValue.increment(5))
                                }
                            }
                    }
                }
            }
    }

    fun downvoteReport(reportId: String) {
        val user = auth.currentUser ?: return
        val userId = user.uid

        db.collection("delays").document(reportId).get()
            .addOnSuccessListener { document ->
                val votedUsers = document.get("votedUsers") as? Map<String, String> ?: emptyMap()

                when (votedUsers[userId]) {
                    "downvote" -> {
                        db.collection("delays").document(reportId).update(
                            "downvotes", FieldValue.increment(-1),
                            "votedUsers.$userId", FieldValue.delete()
                        )
                    }

                    "upvote" -> {
                        db.collection("delays").document(reportId).update(
                            "upvotes", FieldValue.increment(-1),
                            "downvotes", FieldValue.increment(1),
                            "votedUsers.$userId", "downvote"
                        )
                    }

                    else -> {
                        db.collection("delays").document(reportId).update(
                            "downvotes", FieldValue.increment(1),
                            "votedUsers.$userId", "downvote"
                        )
                    }
                }
            }
    }

    fun fetchAreaName(
        context: Context,
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        val geocoder = Geocoder(context, Locale.getDefault())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val area =
                    addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subLocality
                    ?: "Unknown Area"
                withContext(Dispatchers.Main) {
                    onResult(area)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("Unknown Area")
                }
            }
        }
    }

    fun calculateAccuracyPercentage(upvotes: Int, downvotes: Int): Int {
        val totalVotes = upvotes + downvotes
        return if (totalVotes > 0) {
            (upvotes.toFloat() / totalVotes * 100).toInt()
        } else {
            0
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTrafficDelaysInSupabase(
        latitude: Double,
        longitude: Double,
        delayTitle: String,
        severity: Int,
        weather: String,
    ) {
        viewModelScope.launch {
            try {
                val severityLevel = severity

                client.postgrest["trafficdelay"].insert(
                    TrafficData(
                        latitude = latitude,
                        longitude = longitude,
                        delayTitle = delayTitle,
                        severityLevel = severityLevel,
                        weather = weather,
                    )
                )
                _uiState.value = UiState.Success("Delay Saved successfully")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to save report: ${e.message}")
            }
        }
    }

    fun fetchTrafficDelaysFromSupabase() {
        viewModelScope.launch {
            try {
                val reports = client.postgrest["trafficdelay"].select().decodeList<TrafficData>()
                _trafficDelays.value = reports
                _uiState.value = UiState.Success("Traffic delay fetched successfully")
                _uploadState.value = UploadState.Success("Traffic delay fetched successfully")
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Failed to fetch reports: ${e.message}")
                _uiState.value = UiState.Error("Failed to fetch report")
            }
        }
    }

    fun deleteTrafficDelayFromSupabase(reportId: String) {
        viewModelScope.launch {
            try {
                client.postgrest["trafficdelay"].delete {
                    filter {
                        eq("id", reportId)
                    }
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Failed to delete report: ${e.message}")
            }
        }
    }
}

sealed class UploadState {
    data object Idle : UploadState()
    data object Loading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}
