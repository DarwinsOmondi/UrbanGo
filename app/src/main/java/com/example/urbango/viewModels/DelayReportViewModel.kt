package com.example.urbango.viewModels

import android.content.Context
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urbango.model.DelayReport
import com.example.urbango.model.TrafficData
import com.example.urbango.repository.SupabaseClient.client
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import androidx.core.content.edit

class DelayReportViewModel(private val context: Context) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // State Flows
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val _delayReports = MutableStateFlow<List<DelayReport>>(emptyList())
    val delayReports: StateFlow<List<DelayReport>> = _delayReports

    private val _trafficDelays = MutableStateFlow<List<TrafficData>>(emptyList())
    val trafficDelays: StateFlow<List<TrafficData>> = _trafficDelays

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String>("")
    val error: StateFlow<String> = _error

    private val _imageUri = MutableStateFlow<ByteArray?>(null)
    val imageUri: StateFlow<ByteArray?> = _imageUri


    private val MAX_CACHE_SIZE_BYTES = 50 * 1024 * 1024 // 50MB
    private val MAX_CACHE_ITEMS = 100
    private val imageCacheDir by lazy {
        File(context.cacheDir, "delay_images").apply {
            if (!exists()) mkdirs()
        }
    }
    private val cachedImageUris = mutableSetOf<String>()
    private var isNetworkConnected = false

    init {
        startAutoRefresh()
        fetchDelayReports()
        fetchTrafficDelaysFromSupabase()
        startNetworkMonitoring()
    }

    private fun startNetworkMonitoring() {
        viewModelScope.launch {
            while (true) {
                val newState = isOnline()
                if (!isNetworkConnected && newState) {
                    refreshCachedImages()
                }
                isNetworkConnected = newState
                delay(5000)
            }
        }
    }

    private suspend fun refreshCachedImages() {
        withContext(Dispatchers.IO) {
            cachedImageUris.toList().forEach { imageUri ->
                try {
                    val imageBytes = client.storage["trafficimages"].downloadAuthenticated(imageUri)
                    cacheImage(imageUri, imageBytes)
                } catch (e: Exception) {
                    Log.e("CacheRefresh", "Failed to refresh image: $imageUri", e)
                }
            }
        }
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
        imageUri: String
    ) {
        val user = auth.currentUser ?: run {
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
                updateUserPoints(user.uid, 10)
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

                _delayReports.value = snapshots?.documents?.mapNotNull { document ->
                    document.toObject(DelayReport::class.java)?.copy(documentId = document.id)
                } ?: emptyList()
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
                        updateVotes(reportId, userId, upvoteChange = -1L, pointsChange = -5)
                    }

                    "downvote" -> {
                        updateVotes(
                            reportId, userId, upvoteChange = 1L, downvoteChange = -1L,
                            newVote = "upvote", pointsChange = 10
                        )
                    }

                    else -> {
                        updateVotes(
                            reportId,
                            userId,
                            upvoteChange = 1L,
                            newVote = "upvote",
                            pointsChange = 5
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
                        updateVotes(reportId, userId, downvoteChange = -1L)
                    }

                    "upvote" -> {
                        updateVotes(
                            reportId, userId, upvoteChange = -1L, downvoteChange = 1L,
                            newVote = "downvote", pointsChange = -10
                        )
                    }

                    else -> {
                        updateVotes(reportId, userId, downvoteChange = 1L, newVote = "downvote")
                    }
                }
            }
    }

    private fun updateVotes(
        reportId: String,
        userId: String,
        upvoteChange: Long = 0L,
        downvoteChange: Long = 0L,
        newVote: String? = null,
        pointsChange: Int = 0
    ) {
        val updates = mutableMapOf<String, Any>()
        if (upvoteChange != 0L) updates["upvotes"] = FieldValue.increment(upvoteChange)
        if (downvoteChange != 0L) updates["downvotes"] = FieldValue.increment(downvoteChange)

        newVote?.let {
            updates["votedUsers.$userId"] = it
        } ?: run {
            updates["votedUsers.$userId"] = FieldValue.delete()
        }

        db.collection("delays").document(reportId).update(updates)

        if (pointsChange != 0) {
            db.collection("delays").document(reportId).get()
                .addOnSuccessListener { document ->
                    val reportOwnerId = document.getString("userId")
                    reportOwnerId?.let { updateUserPoints(it, pointsChange) }
                }
        }
    }

    private fun updateUserPoints(userId: String, pointsChange: Int) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    db.collection("users").document(userId).set(hashMapOf("points" to pointsChange))
                } else {
                    db.collection("users").document(userId)
                        .update("points", FieldValue.increment(pointsChange.toLong()))
                }
            }
    }

    fun fetchAreaName(
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val addresses = withContext(Dispatchers.IO) {
                    Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)
                }
                val area =
                    addresses?.firstOrNull()?.locality ?: addresses?.firstOrNull()?.subLocality
                    ?: "Unknown Area"
                onResult(area)
            } catch (e: Exception) {
                onResult("Unknown Area")
            }
        }
    }

    fun calculateAccuracyPercentage(upvotes: Int, downvotes: Int): Int {
        val totalVotes = upvotes + downvotes
        return if (totalVotes > 0) (upvotes.toFloat() / totalVotes * 100).toInt() else 0
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
                _isLoading.value = true
                client.postgrest["trafficdelay"].insert(
                    TrafficData(
                        latitude = latitude,
                        longitude = longitude,
                        delayTitle = delayTitle,
                        severityLevel = severity,
                        weather = weather,
                    )
                )
            } catch (e: Exception) {
                _error.value = "Error saving traffic data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTrafficDelaysFromSupabase() {
        viewModelScope.launch {
            try {
                val reports = client.postgrest["trafficdelay"].select().decodeList<TrafficData>()
                _trafficDelays.value = reports
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Failed to fetch reports: ${e.message}")
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

    fun fetchTrafficImage(imageUri: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val cachedImage = getCachedImage(imageUri)
                if (cachedImage != null) {
                    _imageUri.value = cachedImage
                    if (isOnline()) {
                        launch { updateImageInBackground(imageUri) }
                    }
                    _isLoading.value = false
                    return@launch
                }

                if (isOnline()) {
                    val imageBytes = client.storage["trafficimages"].downloadAuthenticated(imageUri)
                    if (shouldCacheImage(imageUri, imageBytes.size)) {
                        cacheImage(imageUri, imageBytes)
                    }
                    _imageUri.value = imageBytes
                } else {
                    _error.value = "No internet connection"
                }
            } catch (e: Exception) {
                _error.value = "Image load failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun updateImageInBackground(imageUri: String) {
        try {
            val imageBytes = client.storage["trafficimages"].downloadAuthenticated(imageUri)
            if (shouldCacheImage(imageUri, imageBytes.size)) {
                cacheImage(imageUri, imageBytes)
            }
        } catch (e: Exception) {
            Log.e("BgRefresh", "Background refresh failed", e)
        }
    }

    private fun shouldCacheImage(imageUri: String, imageSize: Int): Boolean {
        return imageSize < 1 * 1024 * 1024 && getCurrentCacheSize() + imageSize < MAX_CACHE_SIZE_BYTES
    }

    private fun getCurrentCacheSize(): Long {
        return imageCacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    private suspend fun enforceCacheLimits() {
        withContext(Dispatchers.IO) {
            val files = imageCacheDir.listFiles()?.sortedByDescending { it.lastModified() }
            files?.let {
                if (it.size > MAX_CACHE_ITEMS) {
                    it.takeLast(it.size - MAX_CACHE_ITEMS).forEach { file ->
                        file.delete()
                        cachedImageUris.remove(file.name)
                    }
                }

                var totalSize = it.sumOf { file -> file.length() }
                var index = it.lastIndex
                while (totalSize > MAX_CACHE_SIZE_BYTES && index >= 0) {
                    val file = it[index]
                    totalSize -= file.length()
                    file.delete()
                    cachedImageUris.remove(file.name)
                    index--
                }
            }
        }
    }

    private suspend fun cacheImage(imageUri: String, imageBytes: ByteArray) {
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = imageUri.hashCode().toString()
                File(imageCacheDir, cacheKey).writeBytes(imageBytes)
                cachedImageUris.add(imageUri)
                enforceCacheLimits()
            } catch (e: Exception) {
                Log.e("ImageCache", "Error caching image", e)
            }
        }
    }

    private suspend fun getCachedImage(imageUri: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val cacheKey = imageUri.hashCode().toString()
                val file = File(imageCacheDir, cacheKey)
                if (file.exists()) file.readBytes() else null
            } catch (e: Exception) {
                Log.e("ImageCache", "Error reading cached image", e)
                null
            }
        }
    }

    fun clearImageCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                imageCacheDir.listFiles()?.forEach { it.delete() }
                cachedImageUris.clear()
            }
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    sealed class UploadState {
        object Idle : UploadState()
        object Loading : UploadState()
        data class Success(val message: String) : UploadState()
        data class Error(val message: String) : UploadState()
    }

    fun saveUserScreenState(
        screenModeEnabled: Boolean,
        notificationEnabled: Boolean,
        userEmail: String
    ) {
        val sharedPreferences =
            context.getSharedPreferences("prefs_$userEmail", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean("screen_mode", screenModeEnabled)
            putBoolean("notification_enabled", notificationEnabled)
        }
    }

    fun returnUserScreenState(userEmail: String): Pair<Boolean, Boolean> {
        val sharedPreferences =
            context.getSharedPreferences("prefs_$userEmail", Context.MODE_PRIVATE)
        val screenModeEnabled = sharedPreferences.getBoolean("screen_mode", false)
        val notificationEnabled = sharedPreferences.getBoolean("notification_enabled", false)
        return Pair(screenModeEnabled, notificationEnabled)
    }

}