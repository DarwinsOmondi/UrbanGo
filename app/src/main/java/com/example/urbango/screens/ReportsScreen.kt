package com.example.urbango.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.repository.SupabaseClient.client
import com.example.urbango.viewModels.DelayReportViewModel
import com.example.urbango.viewModels.PermissionViewModel
import com.example.urbango.viewModels.UploadState
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.io.File
import java.util.concurrent.Executors
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.core.net.toUri
import com.example.urbango.components.MessageSnackBar
import com.example.urbango.model.DelayReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationViewModel: PermissionViewModel = viewModel()
    val reportViewModel: DelayReportViewModel = viewModel()
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)
    var reportDetails by remember { mutableStateOf("") }
    val selectedGeoPoints = remember { mutableStateListOf<GeoPoint>() }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFileName by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var showCamera by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val uploadState by reportViewModel.uploadState.collectAsState()
    var selectedSeverityLevel by remember { mutableStateOf("") }
    var selctedSeverityIntValue by remember { mutableIntStateOf(0) }
    var selectedWeatherLevel by remember { mutableStateOf("") }
    val listOfSeverity = listOf("Low", "Medium", "High")
    val listOfWeather =
        listOf("Clear", "Light Rain", "Moderate Rain", "Heavy Rain", "Cloudy", "Foggy", "Snow")
    val dropdownExpanded = remember { mutableStateOf(false) }
    val weatherDropdownExpanded = remember { mutableStateOf(false) }
    val isLoading = reportViewModel.isLoading.collectAsState()
    val error = reportViewModel.error.collectAsState()

    val cardColors = listOf(
        Color(0xFF1565C0), // Deep Blue
        Color(0xFF1E88E5), // Lighter Blue
        Color(0xFF64B5F6), // Soft Blue
        Color(0xFF81C784), // Soft Green
        Color(0xFFFFD54F), // Warm Yellow
        Color(0xFFFFA726), // Soft Orange
        Color(0xFFEF5350), // Soft Red
        Color(0xFFAB47BC), // Pastel Purple
        Color(0xFF29B6F6), // Sky Blue
        Color(0xFFAED581)  // Pastel Green
    )
    val cardColor by remember { mutableStateOf(cardColors.random()) }
    var selectedCardTitle by remember { mutableStateOf("") }
    var delayImages by remember { mutableStateOf<ByteArray?>(null) }


//    LaunchedEffect(Unit) {
//        val bucketName = "trafficimages"
//        val bucket = client.storage[bucketName]
//        val delayImage = bucket.downloadAuthenticated()
//        delayImages = delayImage
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Report",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                "Help other commuters by reporting delays, overcrowding, or incidents in real time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(10.dp))

            CardView(
                cardItems = listOfCardItems,
                modifier = Modifier.height(100.dp),
                cardColor = cardColor,
                onCardClick = { title ->
                    selectedCardTitle = title
                    dropdownExpanded.value = true
                }
            )
            DropdownMenu(
                expanded = dropdownExpanded.value,
                onDismissRequest = { dropdownExpanded.value = false }
            ) {
                listOfSeverity.forEach { severity ->
                    DropdownMenuItem(
                        text = { Text(severity) },
                        onClick = {
                            selectedSeverityLevel = severity
                            weatherDropdownExpanded.value = true
                            dropdownExpanded.value = false
                        }
                    )
                }
            }
            DropdownMenu(
                expanded = weatherDropdownExpanded.value,
                onDismissRequest = { weatherDropdownExpanded.value = false }
            ) {
                listOfWeather.forEach { weather ->
                    DropdownMenuItem(
                        text = { Text(weather) },
                        onClick = {
                            selectedWeatherLevel = weather
                            weatherDropdownExpanded.value = false
                        }
                    )
                }
            }
            OutlinedTextField(
                value = selectedCardTitle,
                onValueChange = { selectedCardTitle = it },
                label = { Text("Report Issue") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showCamera = !showCamera }) {
                        Icon(
                            Icons.Default.Camera,
                            contentDescription = "Add Image",
                            tint = Color(0xFF1976D2)
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(),

                )
            Spacer(modifier = Modifier.height(16.dp))

            if (showCamera) {
                CameraCard(
                    onImageCaptured = { uri, filename ->
                        imageUri = uri
                        imageFileName = filename
                        showCamera = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Image captured successfully!")
                        }
                    },
                    onClose = { showCamera = false }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        Configuration.getInstance().userAgentValue = ctx.packageName
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            controller.setZoom(22.0)
                            setMultiTouchControls(true)

                            val locationOverlay =
                                MyLocationNewOverlay(this).apply { enableMyLocation() }
                            overlays.add(locationOverlay)
                            if (locationPermissionGranted) locationOverlay.enableFollowLocation()

                            val mapEventsOverlay = object : org.osmdroid.views.overlay.Overlay() {
                                override fun onSingleTapConfirmed(
                                    e: MotionEvent?,
                                    mapView: MapView?
                                ): Boolean {
                                    e?.let { event ->
                                        val geoPoint = mapView?.projection?.fromPixels(
                                            event.x.toInt(),
                                            event.y.toInt()
                                        )
                                        geoPoint?.let {
                                            selectedGeoPoints.add(it as GeoPoint)
                                            mapView.invalidate()
                                        }
                                    }
                                    return true
                                }
                            }
                            overlays.add(mapEventsOverlay)
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.removeAll { it is Marker }
                        selectedGeoPoints.forEach { geoPoint ->
                            val marker = Marker(mapView).apply {
                                position = geoPoint
                                title = selectedCardTitle
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mapView.overlays.add(marker)
                        }
                        selectedGeoPoints.lastOrNull()?.let { mapView.controller.animateTo(it) }
                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (selectedGeoPoints.isNotEmpty()) {
                            val lastPoint = selectedGeoPoints.last()
                            reportViewModel.saveDelayReport(
                                lastPoint.latitude,
                                lastPoint.longitude,
                                selectedCardTitle,
                                selectedSeverityLevel,
                                imageFileName.toString()
                            )
                            selctedSeverityIntValue = when (selectedSeverityLevel) {
                                "Low" -> {
                                    1
                                }

                                "Medium" -> {
                                    3
                                }

                                "High" -> {
                                    5
                                }

                                else -> {
                                    2
                                }
                            }

                            reportViewModel.saveTrafficDelaysInSupabase(
                                lastPoint.latitude,
                                lastPoint.longitude,
                                selectedCardTitle,
                                selctedSeverityIntValue,
                                selectedWeatherLevel,
                            )
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please select a location on the map.")
                            }
                        }
                    }
                    reportDetails = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                if (isLoading.value) {
                    LinearProgressIndicator()
                } else {
                    Text("Submit Report")
                }
            }
        }
    }
}


@Composable
fun CameraCard(onImageCaptured: (Uri, String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProvider = remember { ProcessCameraProvider.getInstance(context) }
    val executor = ContextCompat.getMainExecutor(context)
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setTargetResolution(android.util.Size(1280, 720))
            .build()
    }

    var hasCameraPermission by remember { mutableStateOf(false) }
    var captureInProgress by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    // Initialize CameraX
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            val cameraProviderInstance = cameraProvider.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProviderInstance.unbindAll()
            cameraProviderInstance.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Camera",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!hasCameraPermission) {
                Text(
                    "Camera permission is required",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            } else {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            }

            if (captureInProgress) {
                Text(
                    text = statusText,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 72.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = {
                    if (captureInProgress) return@Button
                    captureInProgress = true
                    statusText = "Capturing..."

                    val photoFile = File.createTempFile(
                        "captured_image_${System.currentTimeMillis()}",
                        ".jpg",
                        context.cacheDir
                    )

                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture.takePicture(
                        outputOptions,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        statusText = "Compressing..."

                                        // Compress the image
                                        val bitmap =
                                            BitmapFactory.decodeFile(photoFile.absolutePath)
                                        val compressedFile =
                                            File(context.cacheDir, "compressed_${photoFile.name}")
                                        FileOutputStream(compressedFile).use { out ->
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
                                        }
                                        val fileBytes = compressedFile.readBytes()

                                        val bucketName = "trafficimages"
                                        val fileName = compressedFile.name

                                        statusText = "Uploading..."

                                        // Upload to Supabase
                                        client.storage
                                            .from(bucketName)
                                            .upload(
                                                path = fileName,
                                                data = fileBytes,
                                            ) {
                                                upsert = true
                                            }

                                        val publicUrl = client.storage
                                            .from(bucketName)
                                            .publicUrl(fileName)

                                        withContext(Dispatchers.Main) {
                                            captureInProgress = false
                                            statusText = ""
                                            onImageCaptured(publicUrl.toUri(), fileName)
                                        }

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        withContext(Dispatchers.Main) {
                                            captureInProgress = false
                                            statusText = ""
                                            onImageCaptured(Uri.fromFile(photoFile), photoFile.name)
                                        }
                                    }
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                exception.printStackTrace()
                                captureInProgress = false
                                statusText = ""
                            }
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = CircleShape,
                enabled = !captureInProgress,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (captureInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Capture")
                }
            }
        }
    }
}


@Composable
fun CardView(
    cardItems: List<CardItem.CardItems>,
    modifier: Modifier = Modifier,
    cardColor: Color,
    onCardClick: (String) -> Unit
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
    ) {
        items(cardItems) { cardItem ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .height(50.dp)
                    .width(200.dp),
                onClick = {
                    onCardClick(cardItem.title)
                },
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(cardColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = cardItem.title)
                }
            }
        }
    }
}


sealed class CardItem(val title: String) {
    sealed class CardItems(content: String) : CardItem(title = content) {
        data object Card1 : CardItems("⏳ Delay")
        data object Card2 : CardItems("⚠️ Accident")
        data object Card3 : CardItems("\uD83D\uDE8D Overcrowding")
        data object Card4 : CardItems("❌ Cancellation")
        data object Card5 : CardItems("\uD83D\uDEA6 Traffic Jam")
        data object Card6 : CardItems("\uD83D\uDED1 Other")
        data object Card7 : CardItems("🚧 Road Construction")
        data object Card8 : CardItems("🔧 Breakdown")
        data object Card9 : CardItems("🌧️ Heavy Rain")
        data object Card10 : CardItems("❄️ Snow")
        data object Card11 : CardItems("💨 Strong Winds")
        data object Card12 : CardItems("🏗️ Construction Zone")
        data object Card13 : CardItems("🚓 Police Activity")
        data object Card14 : CardItems("🚒 Fire Trucks")
        data object Card15 : CardItems("🎉 Event/Parade")
        data object Card16 : CardItems("⚡ Power Outage")
        data object Card17 : CardItems("🌍 Natural Disaster")
        data object Card18 : CardItems("🦠 Epidemic")
    }
}


val listOfCardItems = listOf(
    CardItem.CardItems.Card1,
    CardItem.CardItems.Card2,
    CardItem.CardItems.Card3,
    CardItem.CardItems.Card4,
    CardItem.CardItems.Card5,
    CardItem.CardItems.Card6,
    CardItem.CardItems.Card7,
    CardItem.CardItems.Card8,
    CardItem.CardItems.Card9,
    CardItem.CardItems.Card10,
    CardItem.CardItems.Card11,
    CardItem.CardItems.Card12,
    CardItem.CardItems.Card13,
    CardItem.CardItems.Card14,
    CardItem.CardItems.Card15,
    CardItem.CardItems.Card16,
    CardItem.CardItems.Card17,
    CardItem.CardItems.Card18,
)