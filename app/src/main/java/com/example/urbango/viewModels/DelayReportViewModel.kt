package com.example.urbango.viewModels

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class DelayReportViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val _delayReports = MutableStateFlow<List<DelayReport>>(emptyList())
    val delayReports: StateFlow<List<DelayReport>> = _delayReports
    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                fetchDelayReports()
                delay(1000) // Refresh every second
            }
        }
    }

    fun saveDelayReport(latitude: Double, longitude: Double, problemReport: String) {
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
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("delays").add(reportData)
            .addOnSuccessListener {
                _uploadState.value = UploadState.Success
            }
            .addOnFailureListener { e ->
                _uploadState.value = UploadState.Error("Failed to save report: ${e.message}")
            }
    }

    fun fetchDelayReports() {
        db.collection("delays")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    _uploadState.value = UploadState.Error("Failed to listen for updates: ${e.message}")
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
                _uploadState.value = UploadState.Success
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

                when (votedUsers[userId]) {
                    "upvote" -> {
                        db.collection("delays").document(reportId).update(
                            "upvotes", FieldValue.increment(-1),
                            "votedUsers.$userId", FieldValue.delete()
                        )
                    }
                    "downvote" -> {
                        db.collection("delays").document(reportId).update(
                            "downvotes", FieldValue.increment(-1),
                            "upvotes", FieldValue.increment(1),
                            "votedUsers.$userId", "upvote"
                        )
                    }
                    else -> {
                        db.collection("delays").document(reportId).update(
                            "upvotes", FieldValue.increment(1),
                            "votedUsers.$userId", "upvote"
                        )
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

    fun fetchAreaName(context: Context, latitude: Double, longitude: Double, onResult: (String?) -> Unit) {
        val geocoder = Geocoder(context, Locale.getDefault())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val area = addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subLocality ?: "Unknown Area"
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
}

sealed class UploadState {
    data object Idle : UploadState()
    data object Loading : UploadState()
    data object Success : UploadState()
    data class Error(val message: String) : UploadState()
}

data class DelayReport(
    val documentId: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val problemReport: String = "",
    val severity: String = "", // Added severity field
    val timestamp: Long = 0,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val votedUsers: Map<String, String> = emptyMap()
)
