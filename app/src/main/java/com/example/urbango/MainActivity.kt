package com.example.urbango

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.urbango.components.PreferencesKeys
import com.example.urbango.components.dataStore
import com.example.urbango.screens.CrowdedScreen
import com.example.urbango.screens.HomeScreen
import com.example.urbango.screens.OnboardingScreen
import com.example.urbango.screens.PredictedDelayScreen
import com.example.urbango.screens.ProfileScreen
import com.example.urbango.screens.ReportScreen
import com.example.urbango.screens.SignInScreen
import com.example.urbango.screens.SignUpScreen
import com.example.urbango.screens.SuggestedRouteScreen
import com.example.urbango.screens.getUserLoggedInStates
import com.example.urbango.ui.theme.UrbanGoTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()
            val context = LocalContext.current
            val dataStore = context.dataStore
            val darkModeFlow = dataStore.data.map { preference ->
                preference[PreferencesKeys.DARK_MODE] ?: false
            }
            val darkMode = darkModeFlow.collectAsState(initial = false)
            UrbanGoTheme(darkTheme = darkMode.value){
                UrbanGoApp(navController, auth,this)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UrbanGoApp(navController: NavHostController, auth: FirebaseAuth,context: Context) {
    val imageUriState = remember { mutableStateOf<Uri?>(null) }
    val isUserLoggedIn = remember { mutableStateOf(getUserLoggedInStates(context)) }

    val startDestination: String = if (isUserLoggedIn.value) {
        "home"
    } else {
        "onboarding"
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToSignUP = {
                    navController.navigate("signup")
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.navigate("signin")
                },
                onSignUpSuccess = {
                    navController.navigate("home")
                },
                auth = auth
            )
        }
        composable("signin") {
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
        composable("home") {
            HomeScreen(
                navController,
                onNavigateToSuggestedRoute = {
                    navController.navigate("suggestedroute")
                })
        }
        composable("reports") {
            ReportScreen(
                navController = navController,
            )
        }
        composable("crowdsourced") {
            CrowdedScreen(navController)
        }
        composable("suggestedroute") {
            SuggestedRouteScreen()
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("predicteddelay") {
            PredictedDelayScreen(navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun UrbanGoAppPreview() {
    UrbanGoTheme {
        val navController = rememberNavController()
        UrbanGoApp(
            navController,
            auth = FirebaseAuth.getInstance(),
            LocalContext.current
        )
    }
}