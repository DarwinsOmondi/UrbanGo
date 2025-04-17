package com.example.urbango.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.R
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.components.UserPointsViewModelFactory
import com.example.urbango.repository.SupabaseClient.client
import com.example.urbango.ui.LeaderboardScreen
import com.example.urbango.viewModels.UserPointsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    var showAboutUs by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showLeaderBoard by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            showAboutUs -> "About Us"
                            showSettings -> "Settings"
                            showLeaderBoard -> "LeaderBoard"
                            else -> "Profile"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                },
                navigationIcon = {
                    if (showAboutUs || showSettings || showLeaderBoard) {
                        IconButton(onClick = {
                            showAboutUs = false
                            showSettings = false
                            showLeaderBoard = false
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            if (!showSettings && !showAboutUs && !showLeaderBoard) {
                BottomNavigationBar(navController)
            } else {
                BottomNavigationBar(navController)
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                showAboutUs -> AboutUsScreen()
                showSettings -> SettingsScreen(navController)
                showLeaderBoard -> LeaderboardScreen()
                else -> {
                    ProfileHeader(auth)
                    ProfileOptions(
                        onSignOut = {
                            navController.navigate("signin") {
                                popUpTo("profile") { inclusive = true }
                            }
                        },
                        onShowAboutUs = { showAboutUs = true },
                        onShowSettings = { showSettings = true },
                        onShowLeaderBoard = { showLeaderBoard = true },
                        auth
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(auth: FirebaseAuth) {
    val userName = auth.currentUser?.displayName
    val userEmail = auth.currentUser?.email ?: "Email"
    val pointsViewModel: UserPointsViewModel = viewModel(
        factory = UserPointsViewModelFactory()
    )
    val loading by pointsViewModel.loadingState.collectAsState()
    val error by pointsViewModel.errorState.collectAsState()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    LaunchedEffect(Unit) {
        pointsViewModel.fetchSingleUserPoints(userEmail)
    }
    val userPoints = pointsViewModel.userPoints.collectAsState().value
    val badge = getBadge(userPoints)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_person_24),
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = userName.toString().trim(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(text = userEmail.toString(), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        if (loading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text(text = error.toString())
        } else {
            Text(
                text = "Points: $userPoints",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Badge: $badge",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileOptions(
    onSignOut: () -> Unit,
    onShowAboutUs: () -> Unit,
    onShowSettings: () -> Unit,
    onShowLeaderBoard: () -> Unit,
    auth: FirebaseAuth,
) {
    val scope = rememberCoroutineScope()
    fun signout() {
        scope.launch {
            client.auth.signOut()
            onSignOut()
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            ProfileOption(Icons.Default.Settings, false, "Settings") {
                onShowSettings()
            }
            HorizontalDivider()
            ProfileOption(Icons.Default.Info, false, "About Us") {
                onShowAboutUs()
            }
            HorizontalDivider()
            ProfileOption(Icons.Default.Leaderboard, false, "Leaderboard") {
                onShowLeaderBoard()
            }
            HorizontalDivider()
            ProfileOption(Icons.AutoMirrored.Filled.ExitToApp, true, "Log Out") {
                signout()
            }
        }
    }
}

@Composable
fun ProfileOption(
    icon: ImageVector,
    isDanger: Boolean = false,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDanger) Color.Red else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDanger) Color.Red else MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getBadge(points: Int): String {
    return when {
        points >= 1000 -> "Road Hero 🏆"
        points >= 500 -> "Traffic Guru 🚦"
        points >= 200 -> "Urban Helper 🏙️"
        points >= 50 -> "Road Explorer 🚗"
        else -> "Newcomer 🛣️"
    }
}