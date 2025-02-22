package com.example.urbango.screens

import android.content.Context
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
fun ReportScreen(navController: NavHostController,onNavigateToCameraScreen:()-> Unit) {
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

    val context = LocalContext.current
    val locationViewModel: PermissionViewModel = viewModel()
    val locationPermissionGranted = locationViewModel.checkLocationPermission(context)
    val cardColor by remember { mutableStateOf(cardColors.random()) }

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
    onNavigateToCameraScreen: () -> Unit
) {
    var reportDetails by remember { mutableStateOf("") }
    var selectedCardTitle by remember { mutableStateOf("") }
    var selectedGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        Text(
            "Help other commuters by reporting delays, overcrowding, or incidents in real time.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(40.dp))

        // Pass lambda to update selected card title
        CardView(
            cardItems = listOfCardItems,
            modifier = Modifier.height(100.dp),
            cardColor = cardColor,
            onCardClick = { title -> selectedCardTitle = title }
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = reportDetails,
            onValueChange = { reportDetails = it },
            label = { Text("Report Issue") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            trailingIcon = {
                IconButton(onClick = { onNavigateToCameraScreen() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

    Box(Modifier.height(800.dp)){
         AndroidView(
        modifier = Modifier
            .wrapContentHeight()
            .align(Alignment.BottomCenter)
            .height(800.dp),
        factory = { ctx ->
            Configuration.getInstance().userAgentValue = ctx.packageName
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(15.0)
                setMultiTouchControls(true)
                clipToOutline = true

                val locationOverlay = MyLocationNewOverlay(this).apply {
                    enableMyLocation()
                }
                overlays.add(locationOverlay)

                if (locationPermissionGranted) {
                    locationOverlay.enableFollowLocation()
                } else {
                    locationViewModel.requestLocationPermission(context)
                }

                locationOverlay.run {
                    if (myLocation != null) {
                        controller.setCenter(myLocation)
                    }
                }

                // Set up touch listener with performClick()
                setOnTouchListener { view, event ->
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        val projection = projection
                        val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                        selectedGeoPoint = geoPoint

                        // Update report details with selected card title + location
                        reportDetails = "$selectedCardTitle, ${selectedGeoPoint!!.latitude}, ${selectedGeoPoint!!.longitude}"

                        view.performClick() // Call performClick for accessibility
                        true
                    } else {
                        false
                    }
                }
            }
        },
        update = { mapView ->
            mapView.onResume()
            mapView.clipToOutline = true

            selectedGeoPoint?.let { geoPoint ->
                // Clear existing markers
                mapView.overlays.removeAll { it is Marker }

                // Add marker at tapped location
                val marker = Marker(mapView).apply {
                    position = geoPoint
                    title = "Report Here"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }

                mapView.overlays.add(marker)
                mapView.controller.animateTo(geoPoint)
                mapView.invalidate()
            }
                 }
             )
        }
    }
}


@Composable
fun CardView(
    cardItems: List<CardItem.CardItems>,
    modifier: Modifier = Modifier,
    cardColor: Color,
    onCardClick: (String) -> Unit // Callback for updating selected title
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
                    .height(250.dp)
                    .width(200.dp),
                onClick = {
                    onCardClick(cardItem.title) // Pass selected card title
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
        data object Card6 : CardItems("\uD83D\uDED1 Other ")
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

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview(){
    ReportScreen(navController = NavHostController(LocalContext.current), onNavigateToCameraScreen ={})
}