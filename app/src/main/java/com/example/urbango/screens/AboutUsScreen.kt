package com.example.urbango.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutUsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "About UrbanGo",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your Smart Companion for Seamless Urban Navigation",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to UrbanGo, your ultimate real-time public transport tracker designed to make commuting smoother, " +
                    "faster, and more reliable. Whether you're catching a bus, navigating traffic delays, or planning your route," +
                    " UrbanGo keeps you informed every step of the way.",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Why UrbanGo?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            listOf(
                "\uD83D\uDE8D Live Public Transport Tracking – Stay updated with real-time bus and transit locations.",
                "\uD83D\uDED1 Delay Reports – Get instant alerts on traffic congestion, route changes, and delays.",
                "\uD83D\uDCCD Smart Navigation – Find the best and fastest routes for your journey.",
                "\uD83D\uDD14 Personalized Alerts – Receive notifications tailored to your daily commute.",
                "\uD83D\uDC9A Eco-Friendly Choices – Reduce your carbon footprint by optimizing travel time and route selection."
            ).forEach {
                Text(text = it, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Our Mission",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "At UrbanGo, we believe in enhancing mobility through technology and real-time data." +
                    "Our goal is to empower commuters with accurate, up-to-date transport information," +
                    " making public transport more efficient, predictable, and stress-free.",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Get in Touch",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            listOf(
                "📩 Email: support@urbango.app",
                "🌍 Website: www.urbango.app",
                "📱 Follow Us: @UrbanGo on Twitter, Facebook, and Instagram"
            ).forEach {
                Text(text = it, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "🚀 UrbanGo – Smarter Travel, Better City Life!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}


