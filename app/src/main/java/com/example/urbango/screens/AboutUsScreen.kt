package com.example.urbango.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbango.ui.theme.UrbanGoTheme
import kotlinx.coroutines.delay

@Composable
fun AboutUsScreen() {
    UrbanGoTheme {
        var isVisible by remember { mutableStateOf(false) }

        // Trigger animation on screen entry
        LaunchedEffect(Unit) {
            delay(300)
            isVisible = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with UrbanGo Branding
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                        animationSpec = tween(
                            1000
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "UrbanGo",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Smarter Travel, Better City Life",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // About Us Sections
                AboutUsCard(
                    title = "About UrbanGo",
                    content = "Welcome to UrbanGo, your ultimate real-time public transport tracker designed to make commuting smoother, faster, and more reliable. Whether you're catching a bus, navigating traffic delays, or planning your route, UrbanGo keeps you informed every step of the way.",
                    isVisible = isVisible,
                    delay = 0L
                )
                Spacer(modifier = Modifier.height(16.dp))

                AboutUsCard(
                    title = "Why UrbanGo?",
                    content = listOf(
                        "\uD83D\uDE8D Live Public Transport Tracking – Stay updated with real-time bus and transit locations.",
                        "\uD83D\uDED1 Delay Reports – Get instant alerts on traffic congestion, route changes, and delays.",
                        "\uD83D\uDCCD Smart Navigation – Find the best and fastest routes for your journey.",
                        "\uD83D\uDD14 Personalized Alerts – Receive notifications tailored to your daily commute.",
                        "\uD83D\uDC9A Eco-Friendly Choices – Reduce your carbon footprint by optimizing travel time and route selection."
                    ),
                    isVisible = isVisible,
                    delay = 100L
                )
                Spacer(modifier = Modifier.height(16.dp))

                AboutUsCard(
                    title = "Our Mission",
                    content = "At UrbanGo, we believe in enhancing mobility through technology and real-time data. Our goal is to empower commuters with accurate, up-to-date transport information, making public transport more efficient, predictable, and stress-free.",
                    isVisible = isVisible,
                    delay = 200L
                )
                Spacer(modifier = Modifier.height(16.dp))

                AboutUsCard(
                    title = "Get in Touch",
                    content = listOf(
                        "📩 Email: support@urbango.app",
                        "🌍 Website: www.urbango.app",
                        "📱 Follow Us: @UrbanGo on Twitter, Facebook, and Instagram"
                    ),
                    isVisible = isVisible,
                    delay = 300L
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Footer Slogan
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                        animationSpec = tween(
                            1000
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    Text(
                        text = "🚀 UrbanGo – Smarter Travel, Better City Life!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AboutUsCard(
    title: String,
    content: Any,
    isVisible: Boolean,
    delay: Long
) {
    var expanded by remember { mutableStateOf(false) }
    var cardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(delay)
            cardVisible = true
        }
    }

    AnimatedVisibility(
        visible = cardVisible,
        enter = fadeIn(animationSpec = tween(800)) + slideInVertically(animationSpec = tween(800)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { expanded = !expanded }
                .background(Color.White),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title Row with Expand/Collapse Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                when (content) {
                    is String -> {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = if (expanded) Int.MAX_VALUE else 2
                        )
                    }

                    is List<*> -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            (content as List<String>).forEach { item ->
                                if (expanded || content.indexOf(item) < 2) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutUsScreenPreview() {
    AboutUsScreen()
}