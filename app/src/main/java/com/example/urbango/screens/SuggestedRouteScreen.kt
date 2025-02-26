package com.example.urbango.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.urbango.viewModels.DelayReportViewModel
import com.example.urbango.viewModels.GeminiRouteViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestedRouteScreen() {
    val viewModel: GeminiRouteViewModel = viewModel()
    val routeResults by viewModel.routeResults
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val context = LocalContext.current
    val delayReportViewModel: DelayReportViewModel = viewModel()
    val delayReports by delayReportViewModel.delayReports.collectAsState()

    var startingLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var desiredDestination by remember { mutableStateOf<GeoPoint?>(null) }
    var speechText by remember { mutableStateOf("No route available.") }
    val textToSpeech = rememberTextToSpeech(context)

    // Update speechText safely when routeResults changes
    LaunchedEffect(routeResults) {
        speechText = routeResults ?: "No route available."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suggested Routes", style = MaterialTheme.typography.titleMedium) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Map View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                OSMDroidMapView(
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

            // Display Selected Locations
            startingLocation?.let { start ->
                Text(
                    text = "Start: (${start.latitude}, ${start.longitude})",
                    modifier = Modifier.padding(8.dp)
                )
            }
            desiredDestination?.let { destination ->
                Text(
                    text = "Destination: (${destination.latitude}, ${destination.longitude})",
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Loading Indicator
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp), color = Color(0xFF1976D2))
            }

            // Error Message
            errorMessage?.let {
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Display Route Results
            Text(
                text = speechText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button to trigger route suggestion
            Button(
                onClick = {
                    startingLocation?.let { start ->
                        desiredDestination?.let { destination ->
                            viewModel.suggestRoute(
                                locationOfDelay = delayReports,
                                desiredDestination = "${destination.latitude},${destination.longitude}",
                                startingLocation = "${start.latitude},${start.longitude}"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text(
                    text = "Get Suggested Route",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                )
            }

            // Button to speak the AI response
            Button(
                onClick = { onSpeak(speechText, textToSpeech, context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
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
    }
}

@Composable
fun OSMDroidMapView(
    modifier: Modifier = Modifier,
    onLocationSelected: (GeoPoint, Boolean) -> Unit
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var startMarker by remember { mutableStateOf<org.osmdroid.views.overlay.Marker?>(null) }
    var destinationMarker by remember { mutableStateOf<org.osmdroid.views.overlay.Marker?>(null) }
    var selectionCount by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance()
                    .load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setBuiltInZoomControls(true)
                    setMultiTouchControls(true)
                    controller.setZoom(13.0)
                    controller.setCenter(GeoPoint(-1.286389, 36.817223)) // Default center

                    val addMarker: (GeoPoint, Boolean) -> Unit = { location, isStart ->
                        val newMarker = org.osmdroid.views.overlay.Marker(this).apply {
                            position = location
                            setAnchor(
                                org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                                org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                            )
                            title = if (isStart) "Starting Location" else "Destination"
                        }

                        if (isStart) {
                            startMarker?.let { overlays.remove(it) }
                            startMarker = newMarker
                        } else {
                            destinationMarker?.let { overlays.remove(it) }
                            destinationMarker = newMarker
                        }

                        overlays.add(newMarker)
                        invalidate() // Refresh map to show markers
                    }

                    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let { geoPoint ->
                                if (selectionCount == 0) {
                                    addMarker(geoPoint, true)
                                    onLocationSelected(geoPoint, true)
                                } else {
                                    addMarker(geoPoint, false)
                                    onLocationSelected(geoPoint, false)
                                }
                                selectionCount = (selectionCount + 1) % 2
                            }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    })
                    overlays.add(mapEventsOverlay)
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
                Toast.makeText(context, "TextToSpeech initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
        textToSpeech = tts
    }

    return textToSpeech
}

private fun onSpeak(text: String, textToSpeech: TextToSpeech?, context: Context) {
    if (textToSpeech == null) {
        Toast.makeText(context, "Text-to-Speech not initialized yet", Toast.LENGTH_SHORT).show()
        return
    }

    if (textToSpeech.isSpeaking) {
        textToSpeech.stop() // Stop speaking if already speaking
        Toast.makeText(context, "Speech stopped", Toast.LENGTH_SHORT).show()
    } else {
        if (text.isNotEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Toast.makeText(context, "No text to speak", Toast.LENGTH_SHORT).show()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SuggestedRouteScreenPreview() {
    SuggestedRouteScreen()
}
