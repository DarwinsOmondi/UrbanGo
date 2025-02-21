package com.example.urbango.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.urbango.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrowdSourcedScreen(navController:NavHostController){
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CrowdSourced", style = MaterialTheme.typography.titleMedium) })
        },
        bottomBar ={
            BottomNavigationBar(navController)
        }
    ){ paddingValue ->
        Text("Hellow crowd whats new",Modifier.padding(paddingValue))
    }
}


@Composable
fun CrowdDataDisplay(){

}

@Composable
fun DataCardView(){

}