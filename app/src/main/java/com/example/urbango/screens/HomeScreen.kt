package com.example.urbango.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val locationPermissionGranted = checkLocationPermission(context)
    Configuration.getInstance().userAgentValue = context.packageName

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("OSMDroid Map") })
        }
    ) { paddingValues ->
        OSMDroidMapView(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            context = context,
            locationPermissionGranted = locationPermissionGranted
        )
    }
}

// Function to check location permission status
fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun OSMDroidMapView(modifier: Modifier = Modifier, context: Context, locationPermissionGranted: Boolean) {
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
                    requestLocationPermission(appContext)
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
}

// Function to request location permission
fun requestLocationPermission(context: Context) {
    if (ActivityCompat.shouldShowRequestPermissionRationale(
            context as Activity, Manifest.permission.ACCESS_FINE_LOCATION
        )) {
        Toast.makeText(context, "Location permission is required for map", Toast.LENGTH_SHORT).show()
    } else {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1001
        )
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
