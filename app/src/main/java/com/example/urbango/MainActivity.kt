package com.example.urbango

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.urbango.components.PreferencesKeys
import com.example.urbango.components.dataStore
import com.example.urbango.repository.SupabaseClient.client
import com.example.urbango.screens.CrowdedScreen
import com.example.urbango.screens.HomeScreen
import com.example.urbango.screens.OnboardingScreen1
import com.example.urbango.screens.OnboardingScreen2
import com.example.urbango.screens.OnboardingScreen3
import com.example.urbango.screens.PredictedDelayScreen
import com.example.urbango.screens.ProfileScreen
import com.example.urbango.screens.ReportScreen
import com.example.urbango.screens.ResetPassword
import com.example.urbango.screens.ResetPasswordScreen
import com.example.urbango.screens.SignInScreen
import com.example.urbango.screens.SignUpScreen
import com.example.urbango.screens.SuggestedRouteScreen
import com.example.urbango.ui.LeaderboardScreen
import com.example.urbango.ui.theme.UrbanGoTheme
import com.google.accompanist.navigation.animation.AnimatedNavHost
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
            UrbanGoTheme() {
                UrbanGoApp(navController, auth, this)
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UrbanGoApp(navController: NavHostController, auth: FirebaseAuth, context: Context) {
    val startDestination: String = if (auth.currentUser != null) {
        "home"
    } else {
        "onboarding1"
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) }) {
        composable("onboarding1") {
            OnboardingScreen1(
                onNavigateToSignUP = {
                    navController.navigate("signup")
                },
                onNavigateToOnboardingScreen2 = {
                    navController.navigate("onboarding2")
                }
            )
        }
        composable("onboarding2") {
            OnboardingScreen2(
                onNavigateToNext = {
                    navController.navigate("onboarding3")
                },
                onSkip = {
                    navController.navigate("signup")
                }
            )
        }
        composable("onboarding3") {
            OnboardingScreen3(
                onNavigateToSignUP = {
                    navController.navigate("signup")
                },
                onSkip = {
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
                },
                onNaviagtetoResetPassword = {
                    navController.navigate("resetpassword")
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
            SuggestedRouteScreen(navController)
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
        composable(
            route = "reset-password",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "myapp://reset-password"
                }
            )
        ) {
            ResetPasswordScreen(
                onNavigateToLogin = {
                    navController.navigate("signin")
                }
            )
        }
        composable("resetpassword") {
            ResetPassword(onBackToLogin = { navController.navigate("signin") })
        }
        composable("userPointsScreen") {
            LeaderboardScreen()
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