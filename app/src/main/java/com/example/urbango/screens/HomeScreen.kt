package com.example.urbango.screens

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.R
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.viewModels.DelayReport
import com.example.urbango.viewModels.DelayReportViewModel
import com.example.urbango.viewModels.GeminiRouteViewModel
import com.example.urbango.viewModels.PermissionViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    onNavigateToSuggestedRoute: () -> Unit,
    locationViewModel: PermissionViewModel = viewModel(),
    delayReportViewModel: DelayReportViewModel = viewModel(),
    geminiRouteViewModel: GeminiRouteViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)

    LaunchedEffect(Unit) {
        delayReportViewModel.fetchDelayReports()
    }

    val delayReports by delayReportViewModel.delayReports.collectAsState()
    var selectedReport by remember { mutableStateOf<DelayReport?>(null) }
    var areaName by remember { mutableStateOf<String?>(null) }
    var routeSuggestionDialogVisible by remember { mutableStateOf(false) }

    Configuration.getInstance().userAgentValue = context.packageName

    Scaffold(
        topBar = { TopAppBar(title = { Text("Home", style = MaterialTheme.typography.titleMedium) }) },
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
                delayReportViewModel.fetchAreaName(context, report.latitude, report.longitude) { name ->
                    areaName = name
                }
            }
        )
    }

    if (routeSuggestionDialogVisible) {
        RouteSuggestionDialog(
            delayReports = delayReports,
            onDismiss = { routeSuggestionDialogVisible = false },
            geminiRouteViewModel = geminiRouteViewModel,
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
            onDismiss = { selectedReport = null }
        )
    }
}

@Composable
fun ReportDetailsDialog(
    report: DelayReport,
    areaName: String?,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delay Report") },
        text = {
            Column {
                Text("Problem: ${report.problemReport}")
                Text("Severity: ${report.severity}")
                Text("Accuracy: ${calculateAccuracyPercentage(report.upvotes, report.downvotes)}%")
                Text("Area: ${areaName ?: "Fetching..."}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSuggestionDialog(
    delayReports: List<DelayReport>,
    onDismiss: () -> Unit,
    geminiRouteViewModel: GeminiRouteViewModel,
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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
    val polyline = remember { Polyline().apply { color = Color.RED; width = 5.0f } }

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

        if (!mapView.overlays.contains(polyline)) {
            mapView.overlays.add(polyline)
        }

        mapView.invalidate()
    }

    AndroidView(
        factory = { mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(20.0)
            setMultiTouchControls(true)
        } },
        modifier = modifier
    )
}

fun createMarkerWithColor(context: Context,color:Int): Drawable {
    val drawable = ContextCompat.getDrawable(context, R.drawable.baseline_location_pin_24)!!
    drawable.setTint(color)
    return drawable
}

fun calculateAccuracyPercentage(upvotes: Int, downvotes: Int): Int {
    val totalVotes = upvotes + downvotes
    return if (totalVotes > 0) (upvotes * 100) / totalVotes else 0
}