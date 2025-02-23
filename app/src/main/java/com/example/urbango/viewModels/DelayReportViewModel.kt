package com.example.urbango.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DelayReportViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

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
}

sealed class UploadState {
    data object Idle : UploadState()
    data object Loading : UploadState()
    data object Success : UploadState()
    data class Error(val message: String) : UploadState()
}
