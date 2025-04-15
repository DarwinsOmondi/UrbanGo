package com.example.urbango.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbango.R

@Composable
fun OnboardingScreen1(
    onNavigateToSignUP: () -> Unit = {},
    onNavigateToOnboardingScreen2: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero image section
            Image(
                painter = painterResource(id = R.drawable.onboardimage),
                contentDescription = "Transit companion illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp) // Fixed height for better control
            )

            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                // Subtitle
                Text(
                    "GET STARTED",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color(0xFF8F8F8F),
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Main headline
                Text(
                    "Your Smart\nTransit Companion",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 42.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Never miss a ride again with real-time updates and personalized recommendations.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Black.copy(alpha = 0.7f),
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (index == 0) Color(0xFF1976D2) else Color(0xFFE0E0E0),
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            Spacer(Modifier.weight(0.5f))
            // Bottom button section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Skip button
                TextButton(
                    onClick = { onNavigateToSignUP() },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Next button
                Button(
                    onClick = {
                        onNavigateToOnboardingScreen2()
                    },
                    modifier = Modifier
                        .width(120.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Text(
                        "Next",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingPreview() {
    MaterialTheme {
        OnboardingScreen1()
    }
}
