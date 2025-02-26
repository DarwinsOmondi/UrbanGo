package com.example.urbango

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.urbango.screens.CrowdedScreen
import com.example.urbango.screens.HomeScreen
import com.example.urbango.screens.OnboardingScreen
import com.example.urbango.screens.ProfileScreen
import com.example.urbango.screens.ReportScreen
import com.example.urbango.screens.SignInScreen
import com.example.urbango.screens.SignUpScreen
import com.example.urbango.screens.SuggestedRouteScreen
import com.example.urbango.ui.theme.UrbanGoTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()
            UrbanGoTheme {
                UrbanGoApp(navController,auth)
            }
        }
    }
}


@Composable
fun UrbanGoApp(navController: NavHostController,auth: FirebaseAuth){
    val imageUriState = remember { mutableStateOf<Uri?>(null) }
    val startDestination :String

    if (auth.currentUser != null){
        startDestination = "home"
    }else{
        startDestination = "onboarding"
    }
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
                    navController.navigate("signin")
                },
                onSignUpSuccess = {
                    navController.navigate("signin")
                },
                auth = auth
            )
        }
        composable("signin"){
            SignInScreen(
                auth = auth,
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onSignInSuccess = {
                    navController.navigate("home")
                }
            )
        }
        composable("home"){
            HomeScreen(navController,
                onNavigateToSuggestedRoute = {
                    navController.navigate("suggestedroute")
                })
        }
        composable("reports") {
            ReportScreen(
                navController = navController,
            )
        }
        composable("crowdsourced"){
            CrowdedScreen(navController)
        }
        composable("suggestedroute"){
            SuggestedRouteScreen()
        }
        composable("profile"){
            ProfileScreen(navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UrbanGoAppPreview(){
    UrbanGoTheme {
        val navController = rememberNavController()
        UrbanGoApp(
            navController,
            auth = FirebaseAuth.getInstance()
        )
    }
}