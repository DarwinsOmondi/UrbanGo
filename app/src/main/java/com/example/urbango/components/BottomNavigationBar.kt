package com.example.urbango.components

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.example.urbango.R

sealed class BottomNavigationBar(val route: String, val title: String){
    sealed class BottomNavigationBarItem(val bRoute:String, val bTitle:String, val bIcon:Int): BottomNavigationBar(route = bRoute, title = bTitle){
        object Home: BottomNavigationBarItem(bRoute = "home", bTitle = "Home", bIcon = R.drawable.baseline_home_24)
        object Reports: BottomNavigationBarItem(bRoute = "reports", bTitle = "Reports", bIcon = R.drawable.baseline_report_24)
        object Alerts: BottomNavigationBarItem(bRoute = "alerts", bTitle = "Alerts", bIcon = R.drawable.baseline_crisis_alert_24)
        object Profile: BottomNavigationBarItem(bRoute = "profile", bTitle = "Profile", bIcon = R.drawable.baseline_person_24)
    }
}

val listOfBottomItems = listOf(
    BottomNavigationBar.BottomNavigationBarItem.Home,
    BottomNavigationBar.BottomNavigationBarItem.Reports,
    BottomNavigationBar.BottomNavigationBarItem.Alerts,
    BottomNavigationBar.BottomNavigationBarItem.Profile
)

@Composable
fun BottomNavigationBar(navController: NavHostController){
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    NavigationBar (

    ){
        listOfBottomItems.forEach { screen->
            NavigationBarItem(
                selected = currentRoute == screen.bRoute,
                onClick = {
                    navController.navigate(screen.bRoute){
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it){
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = screen.bIcon),
                        contentDescription = screen.bTitle,
                        tint =
                            if (currentRoute == screen.bRoute){
                                MaterialTheme.colorScheme.primary
                            }else{
                                MaterialTheme.colorScheme.onBackground
                            }
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview(){
    BottomNavigationBar(navController = NavHostController(LocalContext.current))
}