package com.example.urbango.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbango.components.BottomNavigationBar
import com.example.urbango.components.DelayReportViewModelFactory
import com.example.urbango.components.UserPointsViewModelFactory
import com.example.urbango.model.DelayReport
import com.example.urbango.repository.SupabaseClient.client
import com.example.urbango.viewModels.DelayReportViewModel
import com.example.urbango.viewModels.UserPointsViewModel
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.gotrue.auth
import kotlinx.serialization.json.jsonPrimitive


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrowdedScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: DelayReportViewModel = viewModel(
        factory = DelayReportViewModelFactory(context)
    )
    val delayReports by viewModel.delayReports.collectAsState()
    var searchQuery by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        viewModel.fetchDelayReports()
    }

    val filteredReports = delayReports.filter { report ->
        val areaName = remember { mutableStateOf<String?>(null) }
        viewModel.fetchAreaName(report.latitude, report.longitude) { name ->
            areaName.value = name
        }
        areaName.value?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crowded Reports",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.background
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    MaterialTheme.colorScheme.primary
                )
            )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
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
                        onDownvote = { viewModel.downvoteReport(report.documentId) },
                        context
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
    onDownvote: () -> Unit,
    context: Context
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val userVote = report.votedUsers[userId]
    var areaName by remember { mutableStateOf<String?>(null) }
    val delayReportViewModel: DelayReportViewModel = viewModel()

    val accuracyPercentage =
        delayReportViewModel.calculateAccuracyPercentage(report.upvotes, report.downvotes)
    val userPointViewModel: UserPointsViewModel = viewModel(
        factory = UserPointsViewModelFactory()
    )
    val userPoints = userPointViewModel.userPoints.collectAsState().value
    val userName =
        client.auth.currentUserOrNull()?.userMetadata?.get("email")?.jsonPrimitive?.content


    delayReportViewModel.fetchAreaName(
        report.latitude,
        report.longitude
    ) { name ->
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
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Problem : ${report.problemReport}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            areaName?.let {
                Text(
                    text = "Area: $it",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Text(
                text = "severity: ${report.severity}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Accuracy: $accuracyPercentage%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Row {
                    IconButton(
                        onClick = {
                            onUpvote()
                            userName?.let { name ->
                                if (userPoints > 0) {
                                    userPointViewModel.updateUserPoints(userPoints + 5, name)
                                } else {
                                    if (userPoints == 0){
                                        userPointViewModel.savePointsToSupabase(10, name)
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Upvote",
                            tint = if (userVote == "upvote") Color.Green else Color.Gray,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Text(
                        text = "${report.upvotes}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            onDownvote()

                            userName?.let { name ->
                                if (userPoints > 0) {
                                    userPointViewModel.updateUserPoints(userPoints + 5, name)
                                } else {
                                    userPointViewModel.savePointsToSupabase(10, name)
                                }
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Down vote",
                            tint = if (userVote == "downvote") Color.Red else Color.Gray,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Text(
                        text = "${report.downvotes}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}