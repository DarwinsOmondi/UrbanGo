package com.example.urbango

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.urbango.screens.OnboardingScreen
import com.example.urbango.screens.SignUpScreen
import com.example.urbango.ui.theme.UrbanGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            UrbanGoApp(navController)
        }
    }
}


@Composable
fun UrbanGoApp(navController: NavHostController){
    val startDestination = "onboarding"
    NavHost(navController = navController, startDestination = startDestination){
        composable("onboarding"){
            OnboardingScreen(
                onNavigateToSignUP = {
                    navController.navigate("signup")
                }
            )
        }
        composable("signup"){
            SignUpScreen(
                onNavigateToLogin = {
                    navController.navigate("onboarding")
                }
            )
        }
    }
}