package com.example.urbango.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.model.PredictionResult
import com.example.urbango.viewModels.DelayReportViewModel
import com.example.urbango.viewModels.PredictionViewModelML

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictedDelayScreen(
    navController: NavHostController,
    mlViewModelML: PredictionViewModelML = viewModel(),
    delayReportViewModel: DelayReportViewModel = viewModel(),
) {
    val trafficDelays by delayReportViewModel.trafficDelays.collectAsState()
    val predictionResults by mlViewModelML.predictionResults.collectAsState()

    LaunchedEffect(trafficDelays) {
        delayReportViewModel.fetchDelayReports()
        delayReportViewModel.fetchTrafficDelaysFromSupabase()
        if (trafficDelays.isNotEmpty()) {
            mlViewModelML.predictTrafficDelays(trafficDelays)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Prediction",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (trafficDelays.isEmpty()) {
                Text(
                    "No traffic delays available to predict",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (predictionResults.isEmpty()) {
                Text(
                    "Predictions loading...",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(predictionResults) { prediction ->
                        PredictionCard(prediction)
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionCard(
    predictionResult: PredictionResult,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Text("ML Prediction:", style = MaterialTheme.typography.titleSmall)
            Text(
                "Expect a ${predictionResult.predictedSeverity} "+
                        "likely on ${predictionResult.predictedDay} around ${predictionResult.predictedTime}. " +
                        "Location: ${predictionResult.location}. Weather: ${predictionResult.weather}.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}