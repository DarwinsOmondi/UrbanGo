package com.example.urbango.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.viewModels.DelayReport
import com.example.urbango.viewModels.DelayReportViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrowdedScreen(navController: NavHostController) {
    val viewModel: DelayReportViewModel = viewModel()
    val delayReports by viewModel.delayReports.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Fetch reports when the screen is loaded
    LaunchedEffect(Unit) {
        viewModel.fetchDelayReports()
    }

    val filteredReports = delayReports.filter { report ->
        val areaName = remember { mutableStateOf<String?>(null) }
        viewModel.fetchAreaName(LocalContext.current, report.latitude, report.longitude) { name ->
            areaName.value = name
        }
        areaName.value?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Crowded Reports") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Area Name") },
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor =Color.Black,
                    focusedBorderColor = Color.DarkGray,
                    unfocusedBorderColor = Color.Black
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredReports) { report ->
                    DelayReportCard(
                        report = report,
                        onUpvote = { viewModel.upvoteReport(report.documentId) },
                        onDownvote = { viewModel.downvoteReport(report.documentId) }
                    )
                }
            }
        }
    }
}


@Composable
fun DelayReportCard(
    report: DelayReport,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val userVote = report.votedUsers[userId]
    var areaName by remember { mutableStateOf<String?>(null) }
    val delayReportViewModel: DelayReportViewModel = viewModel()

    val accuracyPercentage = delayReportViewModel.calculateAccuracyPercentage(report.upvotes, report.downvotes)



    delayReportViewModel.fetchAreaName(LocalContext.current, report.latitude, report.longitude) { name ->
        areaName = name
    }

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
            Text(
                "Report by : ${report.userId}",
                style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Problem : ${report.problemReport}",
                style = MaterialTheme.typography.bodySmall
            )
            areaName?.let { Text(text = "Area: $it",style = MaterialTheme.typography.bodySmall) }

            Text(
                text = "severity: ${report.severity}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Accuracy: $accuracyPercentage%",
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(onClick = onUpvote) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Upvote",
                        tint = if (userVote == "upvote") Color.Green else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(text = "${report.upvotes}")
                IconButton(onClick = onDownvote) {
                    Icon(
                        imageVector = Icons.Default.ThumbDown,
                        contentDescription = "Down vote",
                        tint = if (userVote == "downvote") Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(text = "${report.downvotes}")
            }
        }
    }
}