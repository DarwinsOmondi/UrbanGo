package com.example.urbango.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val UrbanGoColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2), // Deep blue for primary elements
    secondary = Color(0xFF4CAF50), // Green for secondary elements (eco-friendly vibe)
    background = Color(0xFFF5F7FA), // Light gray-blue background
    surface = Color.White, // White for cards
    onPrimary = Color.White, // Text on primary color
    onSecondary = Color.White, // Text on secondary color
    onBackground = Color(0xFF212121), // Dark text on background
    onSurface = Color(0xFF212121) // Dark text on cards
)

@Composable
fun UrbanGoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UrbanGoColorScheme,
        typography = MaterialTheme.typography.copy(
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp
            )
        ),
        content = content
    )
}