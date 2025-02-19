package com.example.urbango.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.viewModels.PermissionViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(navController: NavHostController,onNavigateToCameraScreen:()-> Unit) {
    val CardColors = listOf(
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

    val context = LocalContext.current
    val locationViewModel: PermissionViewModel = viewModel()
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)
    val cardColor by remember { mutableStateOf(CardColors.random()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report") },
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        ReportMap(
            modifier = Modifier.padding(paddingValues),
            context,
            locationPermissionGranted,
            locationViewModel,
            cardColor,
            onNavigateToCameraScreen
        )
    }
}

@Composable
fun ReportMap(
    modifier: Modifier,
    context: Context,
    locationPermissionGranted: Boolean,
    locationViewModel: PermissionViewModel,
    cardColor: Color,
    onNavigateToCameraScreen:() -> Unit
) {
    var reportDetails by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        Text("Help other commuters by reporting delays, overcrowding, or incidents in real time.",style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(40.dp))

        CardView(cardItems = listOfCardItems, modifier = Modifier.height(100.dp),cardColor)

        Spacer(modifier = Modifier.height(40.dp))
        OutlinedTextField(
            value = reportDetails,
            onValueChange = { reportDetails = it },
            label = { Text("Report Type") },
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp),
            trailingIcon = {
                IconButton(
                    onClick = {
                        onNavigateToCameraScreen()
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(15.0)
                    setMultiTouchControls(true)
                    clipToOutline = true

                    val locationOverlay = MyLocationNewOverlay(this)
                    locationOverlay.enableMyLocation()
                    overlays.add(locationOverlay)

                    if (locationPermissionGranted) {
                        locationOverlay.enableFollowLocation()
                    } else {
                        locationViewModel.requestLocationPermission(context)
                    }

                    locationOverlay.run {
                        if (myLocation != null) {
                            controller.setCenter(myLocation)
                            val marker = Marker(this@apply)
                            marker.position = GeoPoint(myLocation.latitude, myLocation.longitude)
                            marker.title = "Report here"
                            overlays.add(marker)
                        }
                    }
                }
            },
            update = { mapView ->
                mapView.onResume()
                mapView.clipToOutline = true
            }
        )

    }
}

@Composable
fun CardView(cardItems: List<CardItem.CardItems>, modifier: Modifier = Modifier,cardColor: Color) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(cardItems) { cardItem ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .height(250.dp)
                    .width(200.dp),
                onClick = { /* Handle card click */ },
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
    sealed class CardItems(val content: String) : CardItem(title = content) {
        object Card1 : CardItems("⏳ Delay")
        object Card2 : CardItems("⚠\uFE0F Accident")
        object Card3 : CardItems("\uD83D\uDE8D Overcrowding")
        object Card4 : CardItems("❌ Cancellation")
        object Card5 : CardItems("\uD83D\uDEA6 Traffic Jam")
        object Card6 : CardItems("\uD83D\uDED1 Other ")
    }
}

val listOfCardItems = listOf(
    CardItem.CardItems.Card1,
    CardItem.CardItems.Card2,
    CardItem.CardItems.Card3,
    CardItem.CardItems.Card4,
    CardItem.CardItems.Card5,
    CardItem.CardItems.Card6,
)