package com.example.urbango.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.urbango.viewModels.GeminiRouteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestedRouteScreen() {
    val viewModel: GeminiRouteViewModel = viewModel()
    val routeResults by viewModel.routeResults
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suggested Routes", style = MaterialTheme.typography.titleMedium) }
            )
        }
    ) {paddingValue ->
        Column(Modifier.padding(paddingValue)) {
            Text("Suggested Routes $routeResults")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuggestedRouteScreenPreview() {
    SuggestedRouteScreen()
}
