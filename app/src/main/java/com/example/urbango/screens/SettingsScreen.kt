package com.example.urbango.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavHostController
import com.example.urbango.components.PreferencesKeys
import com.example.urbango.components.dataStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navHostController: NavHostController) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    val auth = FirebaseAuth.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = context.dataStore
    val darkModeFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DARK_MODE] ?: false
    }
    val darkMode by darkModeFlow.collectAsState(initial = false)

    var isSheetOpen by remember { mutableStateOf(false) }
    var currentSheetType by remember { mutableStateOf(SheetType.None) }
    var showDeleteAccountAlertDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsOption(
                title = "Dark Mode",
                description = if (darkMode) "Enabled" else "Disabled",
                showSwitch = true,
                switchChecked = darkMode,
                onSwitchChange = {
                    scope.launch {
                        dataStore.edit { preferences ->
                            preferences[PreferencesKeys.DARK_MODE] = it
                        }
                    }
                }
            )

            HorizontalDivider()

            SettingsOption(
                title = "Notifications",
                description = if (notificationsEnabled) "Enabled" else "Disabled",
                showSwitch = true,
                switchChecked = notificationsEnabled,
                onSwitchChange = { notificationsEnabled = it }
            )

            HorizontalDivider()

            SettingsOption(
                title = "Change Password",
                description = "Update your password",
                onClick = {
                    currentSheetType = SheetType.Password
                    isSheetOpen = true
                }
            )

            HorizontalDivider()

            SettingsOption(
                title = "Update Email",
                description = auth.currentUser?.email ?: "No email set",
                onClick = {
                    currentSheetType = SheetType.Email
                    isSheetOpen = true
                }
            )

            HorizontalDivider()

            SettingsOption(
                title = "Delete Account",
                description = "Permanently delete your account",
                isDanger = true,
                onClick = {
                    showDeleteAccountAlertDialog = !showDeleteAccountAlertDialog
                }
            )
        }

        if (isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { isSheetOpen = false },
                sheetState = sheetState
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
                title = {
                    Text(
                        "Are you sure you want to delete your account ?",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                onDismissRequest = {
                    showDeleteAccountAlertDialog = !showDeleteAccountAlertDialog
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            auth.currentUser?.delete()
                        }
                    ) {
                        Text(
                            "Delete",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Red
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteAccountAlertDialog = !showDeleteAccountAlertDialog
                        },
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth(),
            )
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
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Update Email",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = {
                Text(
                    "New Email",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.Black
            ),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onClose) {
                Text(
                    "Cancel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.weight(.5f))
            TextButton(onClick = {
                auth.currentUser?.updateEmail(email)?.addOnCompleteListener { task ->
                    scope.launch {
                        if (task.isSuccessful) {
                            snackbarHostState.showSnackbar("Email updated successfully")
                        } else {
                            snackbarHostState.showSnackbar("Failed to update email")
                        }
                    }
                    onClose()
                }
            }) {
                Text(
                    "Update",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
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
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Update Password",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = {
                Text(
                    "New Password",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.DarkGray,
                unfocusedBorderColor = Color.Black
            ),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onClose) {
                Text(
                    "Cancel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.weight(.5f))
            TextButton(
                onClick = {
                    auth.currentUser?.updatePassword(password)?.addOnCompleteListener { task ->
                        scope.launch {
                            if (task.isSuccessful) {
                                snackbarHostState.showSnackbar("Password updated successfully")
                            } else {
                                snackbarHostState.showSnackbar("Failed to update password")
                            }
                        }
                        onClose()
                    }
                }) {
                Text(
                    "Update",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDanger) Color.Red else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        if (showSwitch && onSwitchChange != null) {
            Switch(
                checked = switchChecked,
                onCheckedChange = onSwitchChange
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Bottom sheet types
enum class SheetType { Email, Password, None }
