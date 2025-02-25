package com.example.urbango.screens

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.viewModels.DelayReport
import com.example.urbango.viewModels.DelayReportViewModel
import com.example.urbango.viewModels.GeminiRouteViewModel
import com.example.urbango.viewModels.PermissionViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController,onNavigateToSuggestedRoute:() -> Unit) {
    val locationViewModel: PermissionViewModel = viewModel()
    val delayReportViewModel: DelayReportViewModel = viewModel()
    val geminiRouteViewModel: GeminiRouteViewModel = viewModel()
    val context = LocalContext.current
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)

    LaunchedEffect(Unit) {
        delayReportViewModel.fetchDelayReports()
    }

    val delayReports by delayReportViewModel.delayReports.collectAsState()
    var selectedReport by remember { mutableStateOf<DelayReport?>(null) }
    var areaName by remember { mutableStateOf<String?>(null) }



// State for user-selected locations
    var locationOfDelay by remember { mutableStateOf<GeoPoint?>(null) }
    var desiredDestination by remember { mutableStateOf<GeoPoint?>(null) }
    var startingLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var suggestedRoute by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    Configuration.getInstance().userAgentValue = context.packageName

    Scaffold(
        topBar = { TopAppBar(title = { Text("Home", style = MaterialTheme.typography.titleMedium) }) },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (locationOfDelay == null || desiredDestination == null || startingLocation == null) {
                    Toast.makeText(context, "Mark all locations on the map", Toast.LENGTH_SHORT).show()
                    return@FloatingActionButton
                }

                geminiRouteViewModel.suggestRoute(
                    locationOfDelay = locationOfDelay!!.toPair(),
                    desiredDestination = desiredDestination!!.toPair(),
                    startingLocation = startingLocation!!.toPair()
                )

                suggestedRoute = parseRouteCoordinates(geminiRouteViewModel.routeResults.value ?: "")
                Log.d("HomeScreen", "Suggested Route: $suggestedRoute")
                Toast.makeText(context, "Route suggested", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.Directions, contentDescription = "Suggest Route")
            }
        }
    ) { paddingValues ->
        OSMDroidMapView(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            context = context,
            locationPermissionGranted = locationPermissionGranted,
            locationViewModel = locationViewModel,
            delayReports = delayReports,
            onMarkerClick = { report ->
                selectedReport = report
                // Fetch area name when a report is selected
                delayReportViewModel.fetchAreaName(context, report.latitude, report.longitude) { name ->
                    areaName = name
                }
            }
        )
    }

    // Show Report Details with Area Name
    selectedReport?.let { report ->
        AlertDialog(
            onDismissRequest = { selectedReport = null },
            title = { Text("Delay Report") },
            text = {
                Column {
                    Text("Problem: ${report.problemReport}")
                    Text("Accuracy: ${delayReportViewModel.calculateAccuracyPercentage(report.upvotes, report.downvotes)}%")
                    Text("Area: ${areaName ?: "Fetching..."}")
                    Text("Reported by: ${report.userId}")
                }
            },
            confirmButton = {
                Button(onClick = {
                    delayReportViewModel.deleteDelayReport(report.documentId)
                    selectedReport = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { selectedReport = null }) {
                    Text("Suggest")
                }
            }
        )
    }
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
    val selectedPoints = remember { mutableStateListOf<GeoPoint>() }
    val polyline = remember { Polyline().apply { color = android.graphics.Color.RED; width = 5.0f } }
    val geminiRouteViewModel: GeminiRouteViewModel = viewModel()

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

        // Add markers dynamically
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
            mapView.overlays.add(marker)
        }

        // Add polyline only once
        if (!mapView.overlays.contains(polyline)) {
            mapView.overlays.add(polyline)
        }

        mapView.invalidate()
    }


    // Function to draw the selected path
//    fun drawPath() {
//        polyline.setPoints(selectedPoints)
//        mapView.invalidate()
//    }
//
    // Handle map clicks
    val mapEventsReceiver = object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
            selectedPoints.add(p)
            return true
        }

        override fun longPressHelper(p: GeoPoint): Boolean {
            return false
        }
    }
//
    // Add click listener to map
    val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
    mapView.overlays.add(mapEventsOverlay)

    AndroidView(
        factory = {
            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(14.0)
                setMultiTouchControls(true)
            }
        },
        modifier = modifier
    )
}


fun parseRouteCoordinates(response: String): List<GeoPoint> {
    return response.split(";").mapNotNull { coordinate ->
        val parts = coordinate.split(",")
        if (parts.size == 2) {
            val (lat, lon) = parts
            GeoPoint(lat.toDouble(), lon.toDouble())
        } else null
    }
}

fun GeoPoint.toPair(): Pair<Float, Float> = Pair(latitude.toFloat(), longitude.toFloat())


fun onRequestPermissionsResult(
    context: Context,
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    if (requestCode == 1001) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}