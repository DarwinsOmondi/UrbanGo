package com.example.urbango.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        CrowdScreen(Modifier.padding(paddingValue))
    }
}

@Composable
fun CrowdScreen(
    modifier: Modifier,
) {
    var searchAlert by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        OutlinedTextField(
            value = searchAlert,
            onValueChange = { searchAlert = it },
            label = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            trailingIcon = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))
    }
}

val AlertItems = listOf<String>(
    "Cat","Dog","Camel","Hen","Tiger","Lion","Giraffe"
)
@Composable
fun DataCardView(
    alertList:List<String>
){
    val voted by remember { mutableStateOf(false) }
    Box(
        Modifier.fillMaxWidth()
    ) {
        Card(
            onClick = {},
            shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 15.dp, bottomEnd = 15.dp),
            colors = CardDefaults.cardColors(Color.Gray),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
                .padding(4.dp)
        ) {
            alertList.forEach { item ->
                Text(item)
            }
            Box(Modifier.fillMaxWidth()){
                Row(Modifier.align(Alignment.BottomEnd)){
                    IconButton(
                        onClick = {
                            voted != voted
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            if (voted){
                                Color.Green
                            }else{
                                Color.White
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleUp,
                            contentDescription = "Vote Down",
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    IconButton(
                        onClick = {
                            voted != voted
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            if (voted){
                                Color.Red
                            }else{
                                Color.White
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleDown,
                            contentDescription = "Vote Down",
                        )
                    }
                }
            }
        }
    }
}



//@Preview(showBackground = true)
//@Composable
//fun CrowdSourcedScreenPreview(){
//    CrowdSourcedScreen(navController = NavHostController(LocalContext.current))
//}

@Preview(showBackground = true)
@Composable
fun DataCardViewPreview(){
    DataCardView(AlertItems)
}