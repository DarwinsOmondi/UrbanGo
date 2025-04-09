package com.example.urbango.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import com.example.urbango.R

sealed class BottomNavigationBar(val route: String, val title: String){
    sealed class BottomNavigationBarItem(val bRoute:String, val bTitle:String, val bIcon:Int): BottomNavigationBar(route = bRoute, title = bTitle){
        data object Home: BottomNavigationBarItem(bRoute = "home", bTitle = "Home", bIcon = R.drawable.baseline_home_24)
        data object Reports: BottomNavigationBarItem(bRoute = "reports", bTitle = "Reports", bIcon = R.drawable.baseline_report_24)
        data object Crowdsourced: BottomNavigationBarItem(bRoute = "crowdsourced", bTitle = "Crowdsourced", bIcon = R.drawable.baseline_view_timeline_24)
        data object Profile: BottomNavigationBarItem(bRoute = "profile", bTitle = "Profile", bIcon = R.drawable.baseline_person_24)
    }
}

val listOfBottomItems = listOf(
    BottomNavigationBar.BottomNavigationBarItem.Home,
    BottomNavigationBar.BottomNavigationBarItem.Reports,
    BottomNavigationBar.BottomNavigationBarItem.Crowdsourced,
    BottomNavigationBar.BottomNavigationBarItem.Profile
)

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        listOfBottomItems.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.bRoute,
                onClick = {
                    navController.navigate(screen.bRoute) {
                        navController.graph.startDestinationRoute?.let {
                            popUpTo(it) {
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
                        modifier = Modifier.size(24.dp),
                        tint = if (currentRoute == screen.bRoute) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottomNavigationBarPreview() {
    BottomNavigationBar(navController = NavHostController(LocalContext.current))
}
