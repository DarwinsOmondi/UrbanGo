package com.example.urbango.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutUsScreen() {
    Scaffold(
        Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AboutUsCard(
                "About UrbanGo",
                "Welcome to UrbanGo, your ultimate real-time public transport tracker designed to make commuting smoother, " +
                        "faster, and more reliable. Whether you're catching a bus, navigating traffic delays, or planning your route," +
                        " UrbanGo keeps you informed every step of the way.",
            )
            Spacer(modifier = Modifier.height(10.dp))
            AboutUsCard(
                "Why UrbanGo?",
                "\uD83D\uDE8D Live Public Transport Tracking – Stay updated with real-time bus and transit locations.\n\uD83D\uDED1 Delay Reports – Get instant alerts on traffic congestion, route changes, and delays.\n\uD83D\uDCCD Smart Navigation – Find the best and fastest routes for your journey.\n\uD83D\uDD14 Personalized Alerts – Receive notifications tailored to your daily commute.\n\uD83D\uDC9A Eco-Friendly Choices – Reduce your carbon footprint by optimizing travel time and route selection."
            )
            Spacer(modifier = Modifier.height(10.dp))
            AboutUsCard(
                "Our Mission",
                "At UrbanGo, we believe in enhancing mobility through technology and real-time data." +
                        "Our goal is to empower commuters with accurate, up-to-date transport information," +
                        " making public transport more efficient, predictable, and stress-free.",
            )
            Spacer(modifier = Modifier.height(10.dp))
            AboutUsCard(
                "Get in Touch",
                "📩 Email: support@urbango.app \n🌍 Website: www.urbango.app \n 📱 Follow Us: @UrbanGo on Twitter, Facebook, and Instagram"
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "🚀 UrbanGo – Smarter Travel, Better City Life!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun AboutUsCard(title: String, content: String) {
    val brush = Brush.horizontalGradient(
        colors = listOf(
            Color.White,
            MaterialTheme.colorScheme.primary
        ),
        startX = 0f,
        endX = 500f
    )
    Card(
        Modifier
            .fillMaxWidth()
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(3.dp))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .align(Alignment.Start),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}
