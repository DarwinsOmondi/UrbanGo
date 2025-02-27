package com.example.urbango.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen() {
    var darkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Theme Selection
        SettingsOption(
            title = "Dark Mode",
            description = if (darkMode) "Enabled" else "Disabled"
        ) {
            darkMode = !darkMode
        }

        HorizontalDivider()

        // Notifications
        SettingsOption(
            title = "Notifications",
            description = if (notificationsEnabled) "Enabled" else "Disabled"
        ) {
            notificationsEnabled = !notificationsEnabled
        }

        HorizontalDivider()

        // Account Settings
        SettingsOption(
            title = "Change Password",
            description = "Update your password"
        ) {
            // TODO: Implement password change logic
        }

        HorizontalDivider()

        SettingsOption(
            title = "Update Email",
            description = auth.currentUser?.email ?: "No email set"
        ) {
            // TODO: Implement email update logic
        }

        HorizontalDivider()

        SettingsOption(
            title = "Delete Account",
            description = "Permanently delete your account",
            isDanger = true
        ) {
            // TODO: Implement account deletion logic
        }
    }
}

@Composable
fun SettingsOption(
    title: String,
    description: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
    }
}

