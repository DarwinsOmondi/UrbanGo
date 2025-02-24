package com.example.urbango.screens

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.urbango.viewModels.PermissionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, onNavigateToReportScreen: () -> Unit = {}) {
    val locationViewModel: PermissionViewModel = viewModel()
    val delayReportViewModel: DelayReportViewModel = viewModel()
    val context = LocalContext.current
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)

    LaunchedEffect(Unit) {
        delayReportViewModel.fetchDelayReports()
    }

    val delayReports by delayReportViewModel.delayReports.collectAsState()
    var selectedReport by remember { mutableStateOf<DelayReport?>(null) }
    var areaName by remember { mutableStateOf<String?>(null) }

    Configuration.getInstance().userAgentValue = context.packageName

    Scaffold(
        topBar = { TopAppBar(title = { Text("Home", style = MaterialTheme.typography.titleMedium) }) },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToReportScreen() }) {
                Icon(Icons.Default.Add, contentDescription = "Report Delay")
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
                    Text("Close")
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

        mapView.invalidate()
    }

    AndroidView(
        factory = { mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            controller.setZoom(14.0)
            setMultiTouchControls(true)
        } },
        modifier = modifier
    )
}




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
