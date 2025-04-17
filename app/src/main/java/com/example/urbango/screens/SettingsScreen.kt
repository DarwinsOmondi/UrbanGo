package com.example.urbango.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.components.DelayReportViewModelFactory
import com.example.urbango.components.PreferencesKeys
import com.example.urbango.components.dataStore
import com.example.urbango.repository.SupabaseClient.client
import com.example.urbango.ui.theme.UrbanGoTheme
import com.example.urbango.viewModels.DelayReportViewModel
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navHostController: NavHostController) {
    UrbanGoTheme {
        val auth = FirebaseAuth.getInstance()
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val dataStore = context.dataStore
        val viewModel: DelayReportViewModel = viewModel(
            factory = DelayReportViewModelFactory(context)
        )
        val userEmail = auth.currentUser?.email ?: ""
        val (userScreenState, userNotificationState) = viewModel.returnUserScreenState(userEmail)
        val darkModeFlow = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DARK_MODE] ?: false
        }
        var notificationState by remember { mutableStateOf(userNotificationState) }
        val darkMode by darkModeFlow.collectAsState(initial = userScreenState)
        val currentUser = auth.currentUser

        var isSheetOpen by remember { mutableStateOf(false) }
        var currentSheetType by remember { mutableStateOf(SheetType.None) }
        var showDeleteAccountAlertDialog by remember { mutableStateOf(false) }
        var isVisible by remember { mutableStateOf(false) }

        // Trigger animation on screen entry
        LaunchedEffect(Unit) {
            isVisible = true
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
                        )
                    )
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                            animationSpec = tween(
                                800
                            )
                        ),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        Column {
                            SettingsOption(
                                title = "Dark Mode",
                                description = if (darkMode) "Enabled" else "Disabled",
                                showSwitch = true,
                                switchChecked = darkMode,
                                onSwitchChange = { newValue ->
                                    scope.launch {
                                        dataStore.edit { prefs ->
                                            prefs[PreferencesKeys.DARK_MODE] = newValue
                                        }
                                        viewModel.saveUserScreenState(
                                            screenModeEnabled = newValue,
                                            notificationEnabled = notificationState,
                                            userEmail = auth.currentUser?.email ?: "No email set"
                                        )
                                    }
                                }
                            )

                            SettingsOption(
                                title = "Notifications",
                                description = if (notificationState) "Enabled" else "Disabled",
                                showSwitch = true,
                                switchChecked = notificationState,
                                onSwitchChange = { newValue ->
                                    notificationState = newValue
                                    viewModel.saveUserScreenState(
                                        screenModeEnabled = darkMode,
                                        notificationEnabled = newValue,
                                        userEmail = auth.currentUser?.email ?: "No email set"
                                    )
                                }
                            )

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            SettingsOption(
                                title = "Change Password",
                                description = "Update your password",
                                onClick = {
                                    currentSheetType = SheetType.Password
                                    isSheetOpen = true
                                }
                            )

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            SettingsOption(
                                title = "Update Email",
                                description = auth.currentUser?.email ?: "No email set",
                                onClick = {
                                    currentSheetType = SheetType.Email
                                    isSheetOpen = true
                                }
                            )

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            SettingsOption(
                                title = "Delete Account",
                                description = "Permanently delete your account",
                                isDanger = true,
                                onClick = {
                                    showDeleteAccountAlertDialog = !showDeleteAccountAlertDialog
                                }
                            )
                        }
                    }
                }

                if (isSheetOpen) {
                    ModalBottomSheet(
                        onDismissRequest = { isSheetOpen = false },
                        sheetState = rememberModalBottomSheetState(),
                        containerColor = Color.White
                    ) {
                        when (currentSheetType) {
                            SheetType.Email -> EmailUpdateSheet(
                                onClose = { isSheetOpen = false },
                                snackbarHostState = snackbarHostState
                            )

                            SheetType.Password -> PasswordUpdateSheet(
                                onClose = { isSheetOpen = false },
                                snackbarHostState = snackbarHostState
                            )

                            else -> {}
                        }
                    }
                }

                if (showDeleteAccountAlertDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteAccountAlertDialog = false },
                        title = {
                            Text(
                                text = "Delete Account",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        text = {
                            Text(
                                text = "Are you sure you want to delete your account? This action cannot be undone.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (currentUser != null) {
                                        auth.currentUser?.delete()?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Account deleted successfully")
                                                }
                                                navHostController.navigate("signin") {
                                                    popUpTo(navHostController.graph.startDestinationId) {
                                                        inclusive = true
                                                    }
                                                }
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Failed to delete account")
                                                }
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("User not logged in")
                                        }
                                    }
                                    showDeleteAccountAlertDialog = false
                                }
                            ) {
                                Text(
                                    text = "Delete",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDeleteAccountAlertDialog = false }
                            ) {
                                Text(
                                    text = "Cancel",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailUpdateSheet(
    onClose: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val auth = FirebaseAuth.getInstance()
    var newEmail by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Update Email",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            label = { Text("New Email", style = MaterialTheme.typography.bodyLarge) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose, enabled = !isLoading) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    if (newEmail.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a valid email")
                        }
                        return@Button
                    }
                    isLoading = true
                    auth.currentUser?.updateEmail(newEmail)?.addOnCompleteListener { task ->
                        scope.launch {
                            if (task.isSuccessful) {
                                client.auth.modifyUser { email = newEmail }
                                snackbarHostState.showSnackbar("Email updated successfully")
                            } else {
                                snackbarHostState.showSnackbar("Failed to update email: ${task.exception?.message}")
                            }
                            isLoading = false
                            onClose()
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Update",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordUpdateSheet(
    onClose: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val auth = FirebaseAuth.getInstance()
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Update Password",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password", style = MaterialTheme.typography.bodyLarge) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose, enabled = !isLoading) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    if (newPassword.length < 6) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Password must be at least 6 characters")
                        }
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        val user = client.auth.currentUserOrNull()
                        if (user != null) {
                            try {
                                client.auth.modifyUser { password = newPassword }
                                auth.currentUser?.updatePassword(newPassword)
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Password updated successfully")
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to update password: ${task.exception?.message}")
                                            }
                                        }
                                        isLoading = false
                                        onClose()
                                    }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error updating password: ${e.message}")
                                isLoading = false
                                onClose()
                            }
                        } else {
                            snackbarHostState.showSnackbar("User not logged in")
                            isLoading = false
                            onClose()
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Update",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsOption(
    title: String,
    description: String,
    isDanger: Boolean = false,
    showSwitch: Boolean = false,
    switchChecked: Boolean = false,
    onSwitchChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDanger) Color.Red else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (showSwitch && onSwitchChange != null) {
                Switch(
                    checked = switchChecked,
                    onCheckedChange = onSwitchChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = if (isDanger) Color.Red else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

enum class SheetType { Email, Password, None }