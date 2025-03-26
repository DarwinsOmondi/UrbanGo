package com.example.urbango.screens

import android.net.Uri
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.viewModels.DelayReportViewModel
import com.example.urbango.viewModels.PermissionViewModel
import com.example.urbango.viewModels.UploadState
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.io.File
import java.util.concurrent.Executors
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(navController: NavHostController) {
    val context = LocalContext.current
    val locationViewModel: PermissionViewModel = viewModel()
    val reportViewModel: DelayReportViewModel = viewModel()
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)
    var reportDetails by remember { mutableStateOf("") }
    val selectedGeoPoints = remember { mutableStateListOf<GeoPoint>() }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var showCamera by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val uploadState by reportViewModel.uploadState.collectAsState()
    var selectedSeverityLevel by remember { mutableStateOf("") }
    var outlineTextsFieldValue by remember { mutableStateOf("") }
    val listOfSeverity = listOf("Low", "Medium", "High")
    val dropdownExpanded = remember { mutableStateOf(false) }


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

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Report",
                    style = MaterialTheme.typography.headlineSmall
                )
            })
        },
        bottomBar = { BottomNavigationBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
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
                            dropdownExpanded.value = false
                        }
                    )
                }
            }


            OutlinedTextField(
                value = selectedCardTitle,
                onValueChange = { selectedCardTitle = it },
                label = { Text("Report Issue") },
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
                colors = TextFieldDefaults.outlinedTextFieldColors()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (showCamera) {
                CameraCard(
                    onImageCaptured = { uri ->
                        imageUri = uri
                        showCamera = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Image captured successfully!")
                        }
                    },
                    onClose = { showCamera = false }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (selectedGeoPoints.isNotEmpty()) {
                        val lastPoint = selectedGeoPoints.last()
                        reportViewModel.saveDelayReport(
                            lastPoint.latitude,
                            lastPoint.longitude,
                            selectedCardTitle,
                            selectedSeverityLevel
                        )
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please select a location on the map.")
                        }
                    }
                    reportDetails = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Report")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Report")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        Configuration.getInstance().userAgentValue = ctx.packageName
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            controller.setZoom(15.0)
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
        }
    }
    LaunchedEffect(uploadState) {
        when (uploadState) {
            is UploadState.Success -> snackbarHostState.showSnackbar("Report submitted successfully!")
            is UploadState.Error -> snackbarHostState.showSnackbar((uploadState as UploadState.Error).message)
            else -> {}
        }
    }
}


@Composable
fun CameraCard(onImageCaptured: (Uri) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var hasCameraPermission by remember { mutableStateOf(false) }

    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    // If permission is granted, initialize CameraX
    if (hasCameraPermission) {
        LaunchedEffect(Unit) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Close Button
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
                // Camera Preview
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            }

            // Capture Button
            Button(
                onClick = {
                    val photoFile = File(context.cacheDir, "captured_image.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture.takePicture(
                        outputOptions,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onImageCaptured(Uri.fromFile(photoFile))
                            }

                            override fun onError(exception: ImageCaptureException) {
                                exception.printStackTrace()
                            }
                        })
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Capture")
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
            .padding(16.dp)
    ) {
        items(cardItems) { cardItem ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .height(200.dp)
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
        data object Card2 : CardItems("⚠\uFE0F Accident")
        data object Card3 : CardItems("\uD83D\uDE8D Overcrowding")
        data object Card4 : CardItems("❌ Cancellation")
        data object Card5 : CardItems("\uD83D\uDEA6 Traffic Jam")
        data object Card6 : CardItems("☀\uFE0F Weather")
        data object Card7 : CardItems("\uD83D\uDED1 Other ")
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
)