package com.example.urbango.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.components.dataStore
import com.example.urbango.components.PreferencesKeys
import com.example.urbango.components.UserPointsViewModelFactory
import com.example.urbango.viewModels.UserPointsViewModel
import com.example.urbango.model.PointsData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: UserPointsViewModel = viewModel(
        factory = UserPointsViewModelFactory()
    )
) {
    val userPointList by viewModel.userPointList.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val errorState by viewModel.errorState.collectAsState()

    // Read dark mode preference
    val context = LocalContext.current
    val darkModeFlow = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DARK_MODE] ?: false
    }
    val darkMode by darkModeFlow.collectAsState(initial = false)

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = if (darkMode) {
                            listOf(Color(0xFF121212), Color(0xFF1E1E1E))
                        } else {
                            listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
                        }
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        text = "🏆 Leaderboard",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                if (loadingState) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(50.dp)
                            .padding(16.dp)
                    )
                }

                errorState?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (!loadingState && errorState == null) {
                    if (userPointList.isEmpty()) {
                        Text(
                            text = "No data available",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 18.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {
                            itemsIndexed(userPointList) { index, user ->
                                LeaderboardItem(
                                    rank = index + 1,
                                    name = user.userName,
                                    points = user.points,
                                    isVisible = isVisible,
                                    delay = index * 100L,
                                    darkMode = darkMode
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    rank: Int,
    name: String,
    points: Int,
    isVisible: Boolean,
    delay: Long,
    darkMode: Boolean
) {
    var itemVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(delay)
            itemVisible = true
        }
    }

    AnimatedVisibility(
        visible = itemVisible,
        enter = fadeIn(animationSpec = tween(800)) + slideInVertically(animationSpec = tween(800)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = when (rank) {
                    1 -> if (darkMode) Color(0xFFB0BEC5) else Color(0xFFFFD700) // Gold for 1st
                    2 -> if (darkMode) Color(0xFF90A4AE) else Color(0xFFC0C0C0) // Silver for 2nd
                    3 -> if (darkMode) Color(0xFF78909C) else Color(0xFFCD7F32) // Bronze for 3rd
                    else -> MaterialTheme.colorScheme.surface
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "#$rank",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rank <= 3) {
                        if (darkMode) MaterialTheme.colorScheme.onSurface else Color.Black
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (rank <= 3) {
                        if (darkMode) MaterialTheme.colorScheme.onSurface else Color.Black
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$points",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) {
                            if (darkMode) MaterialTheme.colorScheme.onSurface else Color.Black
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = " pts",
                        fontSize = 14.sp,
                        color = if (rank <= 3) {
                            if (darkMode) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else Color.Black.copy(
                                alpha = 0.7f
                            )
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}