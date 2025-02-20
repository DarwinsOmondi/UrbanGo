package com.example.urbango.screens

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.viewModels.PermissionViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController,onNavigateToReportScreen: () -> Unit = {}) {
    val locationViewModel:PermissionViewModel = viewModel()
    val context = LocalContext.current
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)
    Configuration.getInstance().userAgentValue = context.packageName

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home", style = MaterialTheme.typography.titleMedium) })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        OSMDroidMapView(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            context = context,
            locationPermissionGranted = locationPermissionGranted,
            locationViewModel
        )
    }
}

@Composable
fun OSMDroidMapView(modifier: Modifier = Modifier, context: Context, locationPermissionGranted: Boolean,locationViewModel: PermissionViewModel) {
    Column {
        AndroidView(
            factory = { appContext -> // Renamed the inner context to appContext
                MapView(appContext).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(15.0)

                    // Enable multitouch controls (pinch-to-zoom)
                    setMultiTouchControls(true)

                    // Enable location tracking
                    val locationOverlay = MyLocationNewOverlay(this)
                    locationOverlay.enableMyLocation()
                    overlays.add(locationOverlay)

                    // If permission is granted, enable location tracking
                    if (locationPermissionGranted) {
                        locationOverlay.enableFollowLocation()
                    } else {
                        // Request permission if not granted
                        locationViewModel.requestLocationPermission(context)
                    }

                    // Add a marker for the user's location
                    locationOverlay.run {
                        if (myLocation != null) {
                            controller.setCenter(myLocation)
                            val marker = Marker(this@apply)
                            marker.position = GeoPoint(myLocation.latitude, myLocation.longitude)
                            marker.title = "You are here"
                            overlays.add(marker)
                        }
                    }
                }
            },
            modifier = modifier,
            update = { mapView ->
                mapView.onResume() // Handle onResume lifecycle
            }
        )
        FloatingActionButton(
            onClick = {}
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
}


// Handle permission result and enable map if permission is granted
fun onRequestPermissionsResult(
    context: Context,
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    if (requestCode == 1001) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission granted", Toast.LENGTH_SHORT).show()
            // Here you can update the map view
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = NavHostController(LocalContext.current))
}