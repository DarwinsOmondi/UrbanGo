package com.example.urbango.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.OnlinePrediction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.urbango.R
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.components.DelayReportViewModelFactory
import com.example.urbango.model.DelayReport
import com.example.urbango.model.TrafficData
import com.example.urbango.viewModels.DelayReportViewModel
import com.example.urbango.viewModels.PermissionViewModel
import com.example.urbango.viewModels.PredictionViewModelML
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    onNavigateToSuggestedRoute: () -> Unit,
    locationViewModel: PermissionViewModel = viewModel(),
    mlViewModelML: PredictionViewModelML = viewModel(),
) {
    val context = LocalContext.current
    val delayReportViewModel: DelayReportViewModel = viewModel(
        factory = DelayReportViewModelFactory(context)
    )
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)
    val imageState by delayReportViewModel.imageUri.collectAsState()
    val isLoading by delayReportViewModel.isLoading.collectAsState()
    val error by delayReportViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        delayReportViewModel.fetchDelayReports()
        delayReportViewModel.fetchTrafficDelaysFromSupabase()
    }

    val delayReports by delayReportViewModel.delayReports.collectAsState()

    var selectedReport by remember { mutableStateOf<DelayReport?>(null) }
    var areaName by remember { mutableStateOf<String?>(null) }
    var routeSuggestionDialogVisible by remember { mutableStateOf(false) }

    Configuration.getInstance().userAgentValue = context.packageName

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Home",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("predicteddelay")
                        },
                        Modifier
                            .size(50.dp)
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.OnlinePrediction,
                            contentDescription = "Predict Delay",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNavigateToSuggestedRoute()
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Directions, contentDescription = "Suggest Route")
            }
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            OSMDroidMapView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                context = context,
                locationPermissionGranted = locationPermissionGranted,
                locationViewModel = locationViewModel,
                delayReports = delayReports,
                onMarkerClick = { report ->
                    selectedReport = report
                    delayReportViewModel.fetchAreaName(
                        report.latitude,
                        report.longitude
                    ) { name ->
                        areaName = name
                    }
                    delayReportViewModel.fetchTrafficImage(report.imageUri)

                    mlViewModelML.predictTrafficDelay(
                        TrafficData(
                            latitude = report.latitude,
                            longitude = report.longitude,
                            delayTitle = report.problemReport,
                            severityLevel = when (report.severity) {
                                "Low" -> 1
                                "Medium" -> 3
                                "High" -> 5
                                else -> 3
                            },
                            weather = "Unknown"
                        )
                    )
                }
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp),
                    strokeWidth = 6.dp
                )
            }

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }
        }
    }

    if (routeSuggestionDialogVisible) {
        RouteSuggestionDialog(
            onDismiss = { routeSuggestionDialogVisible = false },
            onNavigateToSuggestedRoute = { onNavigateToSuggestedRoute() }
        )
    }

    selectedReport?.let { report ->
        ReportDetailsDialog(
            report = report,
            areaName = areaName,
            onDelete = {
                delayReportViewModel.deleteDelayReport(report.documentId)
                selectedReport = null
            },
            onDismiss = { selectedReport = null },
            imageState = imageState,
            isLoading = isLoading,
            error = error
        )
    }
}

@Composable
fun ReportDetailsDialog(
    report: DelayReport,
    areaName: String?,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    imageState: ByteArray?,
    isLoading: Boolean,
    error: String
) {
    val timestamp = report.timestamp
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = sdf.format(Date(timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delay Report") },
        text = {
            Column {
                Text("Reported at: $formattedTime")

                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    imageState != null -> {
                        Image(
                            bitmap = imageState.toImageBitmap(),
                            contentDescription = "Delay image",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.Crop
                        )
                    }

                    error.isNotEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_wifi_off_24),
                                contentDescription = "Error",
                                modifier = Modifier.size(50.dp)
                            )
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    else -> {
                        Icon(
                            painter = painterResource(R.drawable.baseline_image_not_supported_24),
                            contentDescription = "No image",
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Problem: ${report.problemReport}")
                Text("Severity: ${report.severity}")
                Text("Accuracy: ${calculateAccuracyPercentage(report.upvotes, report.downvotes)}%")
                Text("Area: ${areaName ?: "Unknown Area"}")
                Text("Reported by: ${report.userId}")
            }
        },
        confirmButton = {
            Button(onClick = onDelete) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}

fun ByteArray.toImageBitmap(): ImageBitmap {
    return BitmapFactory.decodeByteArray(this, 0, size).asImageBitmap()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSuggestionDialog(
    onDismiss: () -> Unit,
    onNavigateToSuggestedRoute: () -> Unit,
) {
    var startingLocation by remember { mutableStateOf("") }
    var desiredDestination by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Suggest Route") },
        text = {
            Column {
                OutlinedTextField(
                    value = startingLocation,
                    onValueChange = { startingLocation = it },
                    label = { Text("Starting Location") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = androidx.compose.ui.graphics.Color.Black,
                        unfocusedTextColor = androidx.compose.ui.graphics.Color.Black,
                        focusedBorderColor = androidx.compose.ui.graphics.Color.DarkGray,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Black
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp),
                )

                OutlinedTextField(
                    value = desiredDestination,
                    onValueChange = { desiredDestination = it },
                    label = { Text("Destination Location") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = androidx.compose.ui.graphics.Color.Black,
                        unfocusedTextColor = androidx.compose.ui.graphics.Color.Black,
                        focusedBorderColor = androidx.compose.ui.graphics.Color.DarkGray,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Black
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onNavigateToSuggestedRoute()
            }) {
                Text("Suggest")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OSMDroidMapView(
    modifier: Modifier = Modifier,
    context: Context,
    locationPermissionGranted: Boolean,
    locationViewModel: PermissionViewModel,
    delayReports: List<DelayReport>,
    onMarkerClick: (DelayReport) -> Unit
) {
    val mapView = remember { MapView(context) }

    LaunchedEffect(delayReports) {
        mapView.overlays.clear()

        val locationOverlay = MyLocationNewOverlay(mapView).apply {
            enableMyLocation()
        }
        mapView.overlays.add(locationOverlay)

        if (locationPermissionGranted) {
            locationOverlay.enableFollowLocation()
        } else {
            locationViewModel.requestLocationPermission(context)
        }

        delayReports.forEach { report ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(report.latitude, report.longitude)
                title = report.problemReport
                snippet = "Tap for details"
                setOnMarkerClickListener { _, _ ->
                    onMarkerClick(report)
                    true
                }
            }
            when (report.severity) {
                "Low" -> marker.icon = createMarkerWithColor(context, Color.GREEN)
                "Medium" -> marker.icon = createMarkerWithColor(context, Color.YELLOW)
                "High" -> marker.icon = createMarkerWithColor(context, Color.RED)
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    AndroidView(
        factory = {
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(17.0)
                setMultiTouchControls(true)
            }
        },
        modifier = modifier
    )
}

fun createMarkerWithColor(context: Context, color: Int): Drawable {
    val drawable = ContextCompat.getDrawable(context, R.drawable.baseline_location_pin_24)!!
    drawable.setTint(color)
    return drawable
}

fun calculateAccuracyPercentage(upvote: Int, downvotes: Int): Int {
    val totalVotes = upvote + downvotes
    return if (totalVotes > 0) (upvote * 100) / totalVotes else 0
}