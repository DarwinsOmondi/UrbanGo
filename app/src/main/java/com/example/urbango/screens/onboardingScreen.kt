package com.example.urbango.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.urbango.R

@Composable
fun OnboardingScreen(
    onNavigateToSignUP: () -> Unit = {},
) {
    Scaffold(
        Modifier.background(Color(0xFFFFFFFF))
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboardimage),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        "Get Started",
                        style = TextStyle(
                            color = Color(0xFF8F8F8F),
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = MaterialTheme.typography.titleMedium.fontWeight
                        )
                    )

                    Spacer(modifier = Modifier.height(50.dp))

                    Text(
                        "Your Smart Transit Companion.",
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 40.sp,
                            fontWeight = MaterialTheme.typography.titleLarge.fontWeight
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Never miss a ride again!",
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 40.sp,
                            fontWeight = MaterialTheme.typography.titleLarge.fontWeight
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { /* Navigate or handle logic */ },
                        modifier = Modifier.padding(end = 16.dp, top = 16.dp)
                    ) {
                        Text(
                            "Skip",
                            style = TextStyle(
                                color = Color(0xFF1976D2),
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = MaterialTheme.typography.labelLarge.fontWeight
                            )
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = { onNavigateToSignUP()},
                        modifier = Modifier
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text(
                            "Next",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = MaterialTheme.typography.labelLarge.fontWeight
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPreview() {
    OnboardingScreen()
}
