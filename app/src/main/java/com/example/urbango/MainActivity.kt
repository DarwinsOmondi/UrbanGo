package com.example.urbango

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
import com.example.urbango.screens.HomeScreen
import com.example.urbango.screens.OnboardingScreen
import com.example.urbango.screens.SignInScreen
import com.example.urbango.screens.SignUpScreen
import com.example.urbango.ui.theme.UrbanGoTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()
            UrbanGoApp(navController,auth)
        }
    }
}


@Composable
fun UrbanGoApp(navController: NavHostController,auth: FirebaseAuth){
    val startDestination = remember { mutableStateOf("") }

    if (auth.currentUser != null){
        startDestination.value = "home"
    }else{
        startDestination.value = "onboarding"
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
            HomeScreen()
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