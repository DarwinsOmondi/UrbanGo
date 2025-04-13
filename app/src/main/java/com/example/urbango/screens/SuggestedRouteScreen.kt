package com.example.urbango.screens

import android.annotation.SuppressLint
import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.components.DelayReportViewModelFactory
import com.example.urbango.viewModels.DelayReportViewModel
import com.example.urbango.viewModels.GeminiRouteViewModel
import com.example.urbango.viewModels.PermissionViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestedRouteScreen(navHostController: NavHostController) {
    val viewModel: GeminiRouteViewModel = viewModel()
    val locationViewModel: PermissionViewModel = viewModel()
    // Access properties directly instead of using 'by'
    val routeResults = viewModel.routeResults
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val context = LocalContext.current
    val delayReportViewModel: DelayReportViewModel = viewModel(
        factory = DelayReportViewModelFactory(context)
    )
    val delayReports by delayReportViewModel.delayReports.collectAsState()
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)

    var startingLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var desiredDestination by remember { mutableStateOf<GeoPoint?>(null) }
    var speechText by remember { mutableStateOf("No route available.") }
    val textToSpeech = rememberTextToSpeech(context)

    // Update speechText whenever routeResults changes
    LaunchedEffect(routeResults) {
        speechText = routeResults ?: "No route available."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Suggested Routes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navHostController.navigate("home")
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.background
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
            ) {
                OSMDroidMapView(
                    locationPermissionGranted = locationPermissionGranted,
                    locationViewModel = locationViewModel,
                    modifier = Modifier.fillMaxSize(),
                    onLocationSelected = { geoPoint, isStart ->
                        if (isStart) {
                            startingLocation = geoPoint
                        } else {
                            desiredDestination = geoPoint
                        }
                    }
                )
            }

            startingLocation?.let { start ->
                Text(
                    text = "Start: (${
                        String.format(
                            "%.6f",
                            start.latitude
                        )
                    }, ${String.format("%.6f", start.longitude)})",
                    modifier = Modifier.padding(8.dp)
                )
            }
            desiredDestination?.let { destination ->
                Text(
                    text = "Destination: (${
                        String.format(
                            "%.6f",
                            destination.latitude
                        )
                    }, ${String.format("%.6f", destination.longitude)})",
                    modifier = Modifier.padding(8.dp)
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF1976D2)
                )
            }
            if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Text(
                text = speechText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                Modifier
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Button(
                    onClick = {
                        if (startingLocation == null || desiredDestination == null) {
                            Toast.makeText(
                                context,
                                "Please select both start and destination points",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.suggestRoute(
                                locationOfDelay = delayReports,
                                desiredDestination = "${desiredDestination!!.latitude},${desiredDestination!!.longitude}",
                                startingLocation = "${startingLocation!!.latitude},${startingLocation!!.longitude}"
                            )
                        }
                    },
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = "Route"
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Get Route",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                }
                Button(
                    onClick = { onSpeak(speechText, textToSpeech, context) },
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    enabled = speechText.isNotEmpty() && !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Speak",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Speak Route",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                }
            }
            TextButton(
                onClick = { viewModel.clearRouteResults() },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Clear Results", color = Color(0xFF1976D2))
            }
        }
    }
}

@Composable
fun OSMDroidMapView(
    locationPermissionGranted: Boolean,
    locationViewModel: PermissionViewModel,
    modifier: Modifier = Modifier,
    onLocationSelected: (GeoPoint, Boolean) -> Unit
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var startMarker by remember { mutableStateOf<org.osmdroid.views.overlay.Marker?>(null) }
    var destinationMarker by remember { mutableStateOf<org.osmdroid.views.overlay.Marker?>(null) }
    var selectionCount by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(locationPermissionGranted, mapView) {
        mapView?.overlays?.clear()

        val locationOverlay = MyLocationNewOverlay(mapView).apply {
            enableMyLocation()
            enableFollowLocation()
        }
        mapView?.overlays?.add(locationOverlay)

        if (locationPermissionGranted) {
            locationOverlay.runOnFirstFix {
                val currentLocation = locationOverlay.myLocation
                if (currentLocation != null) {
                    userLocation = GeoPoint(currentLocation.latitude, currentLocation.longitude)
                    mapView?.controller?.setCenter(userLocation)
                }
            }
        } else {
            locationViewModel.requestLocationPermission(context)
        }
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { geoPoint ->
                    val isStart = selectionCount == 0
                    val newMarker = org.osmdroid.views.overlay.Marker(mapView).apply {
                        position = geoPoint
                        setAnchor(
                            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                            org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                        )
                        title = if (isStart) "Starting Location" else "Destination"
                    }

                    if (isStart) {
                        startMarker?.let { mapView?.overlays?.remove(it) }
                        startMarker = newMarker
                    } else {
                        destinationMarker?.let { mapView?.overlays?.remove(it) }
                        destinationMarker = newMarker
                    }

                    mapView?.overlays?.add(newMarker)
                    onLocationSelected(geoPoint, isStart)
                    selectionCount = (selectionCount + 1) % 2
                    mapView?.invalidate()
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean = false
        })
        mapView?.overlays?.add(mapEventsOverlay)
        mapView?.invalidate()
    }
    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            mapView?.controller?.setCenter(userLocation)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .clip(RoundedCornerShape(bottomEnd = 12.dp, bottomStart = 12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance()
                    .load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setBuiltInZoomControls(true)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)

                    if (userLocation == null) {
                        controller.setCenter(
                            GeoPoint(
                                0.0,
                                0.0
                            )
                        )
                    }
                }.also { mapView = it }
            },
            modifier = modifier
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
}

@Composable
fun rememberTextToSpeech(context: Context): TextToSpeech? {
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
            } else {
                Toast.makeText(context, "TextToSpeech initialization failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        textToSpeech = tts
    }

    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.shutdown()
        }
    }

    return textToSpeech
}

private fun onSpeak(text: String, textToSpeech: TextToSpeech?, context: Context) {
    if (textToSpeech == null) {
        Toast.makeText(context, "Text-to-Speech not initialized yet", Toast.LENGTH_SHORT).show()
        return
    }

    if (textToSpeech.isSpeaking) {
        textToSpeech.stop()
        Toast.makeText(context, "Speech stopped", Toast.LENGTH_SHORT).show()
    } else {
        if (text.isNotEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Toast.makeText(context, "No text to speak", Toast.LENGTH_SHORT).show()
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun SuggestedRouteScreenPreview() {
//    SuggestedRouteScreen(navHostController = NavHostController)
//}