package com.example.urbango.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.urbango.R
import com.example.urbango.repository.SupabaseClient.client
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    auth: FirebaseAuth,
    onNavigateToSignUp: () -> Unit = {},
    onSignInSuccess: () -> Unit = {},
    onNaviagtetoResetPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with sign up option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onNavigateToSignUp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF61ABF3),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Sign Up", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Image section
            Image(
                painter = painterResource(R.drawable.signinimage),
                contentDescription = "Sign in illustration",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            )

            // Title
            Text(
                "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .align(Alignment.Start)
            )

            // Subtitle
            Text(
                "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email field
            OutlinedTextField(
                value = email, textStyle = MaterialTheme.typography.bodyMedium,
                onValueChange = { email = it },
                label = {
                    Text(
                        "Email",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.outline,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Password field
            OutlinedTextField(
                value = password, textStyle = MaterialTheme.typography.bodyMedium,
                onValueChange = { password = it },
                label = {
                    Text(
                        "Password",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.outline,
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisibility = !passwordVisibility },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            )

            // Forgot password option
            Text(
                "Forgot password?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 24.dp, top = 4.dp)
                    .clickable { onNaviagtetoResetPassword() }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Sign in button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val results = signInUser(auth, email, password, context)
                            client.auth.signInWith(Email) {
                                this.email = email
                                this.password = password
                            }
                            if (results.isSuccess) {
                                isLoading = false
                                onSignInSuccess()
                            } else {
                                errorMessage = results.exceptionOrNull()?.message
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "An error occurred"
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        "Sign In",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Error message
            errorMessage?.let { message ->
                val userFriendlyMessage = when {
                    // Field validation errors
                    message.contains(
                        "missing email",
                        ignoreCase = true
                    ) -> "Please enter your email address"

                    message.contains(
                        "missing password",
                        ignoreCase = true
                    ) -> "Please enter your password"

                    message.contains(
                        "invalid email",
                        ignoreCase = true
                    ) -> "Please enter a valid email address"

                    message.contains(
                        "invalid login",
                        ignoreCase = true
                    ) -> "Invalid email or password"

                    // Credential errors
                    message.contains(
                        "wrong credentials",
                        ignoreCase = true
                    ) -> "Incorrect email or password"

                    message.contains(
                        "wrong password",
                        ignoreCase = true
                    ) -> "Incorrect password"

                    message.contains(
                        "invalid password",
                        ignoreCase = true
                    ) -> "Incorrect password"

                    message.contains(
                        "incorrect password",
                        ignoreCase = true
                    ) -> "Incorrect password"

                    message.contains(
                        "user not found",
                        ignoreCase = true
                    ) -> "No account found with this email"

                    // Account status errors
                    message.contains(
                        "email already in use",
                        ignoreCase = true
                    ) -> "This email is already registered"

                    message.contains(
                        "account exists",
                        ignoreCase = true
                    ) -> "Account already exists"

                    message.contains(
                        "unverified email",
                        ignoreCase = true
                    ) -> "Please verify your email first"

                    // Password requirements
                    message.contains(
                        "weak password",
                        ignoreCase = true
                    ) -> "Password must be at least 6 characters"

                    message.contains(
                        "password too short",
                        ignoreCase = true
                    ) -> "Password must be at least 6 characters"

                    message.contains(
                        "password requirement",
                        ignoreCase = true
                    ) -> "Password doesn't meet requirements"

                    // Network/security errors
                    message.contains(
                        "network error",
                        ignoreCase = true
                    ) -> "Network error - please check your connection"

                    message.contains(
                        "too many requests",
                        ignoreCase = true
                    ) -> "Too many attempts - try again later"

                    message.contains(
                        "rate limit",
                        ignoreCase = true
                    ) -> "Too many attempts - try again in a few minutes"

                    message.contains(
                        "timeout",
                        ignoreCase = true
                    ) -> "Request timed out - please try again"

                    // Supabase-specific errors
                    message.contains("auth_failure", ignoreCase = true) -> "Authentication failed"
                    message.contains("provider_error", ignoreCase = true) -> "Login service error"
                    message.contains(
                        "invalid_refresh_token",
                        ignoreCase = true
                    ) -> "Session expired - please login again"

                    // Fallback with cleaned up message
                    else -> "Error: ${
                        message.substringBefore("(").trim()
                    }"
                }

                Text(
                    text = userFriendlyMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            // Or divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = DividerDefaults.Thickness,
                    color = Color.Black.copy(alpha = 0.7f)
                )
                Text(
                    "OR",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.7f),
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = DividerDefaults.Thickness,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }

            // Social login options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                // Google login button
                IconButton(
                    onClick = { /* Handle Google login */ },
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = "Sign in with Google",
                        tint = Color.Unspecified
                    )
                }

                // Facebook login button
                IconButton(
                    onClick = { /* Handle Facebook login */ },
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = "Sign in with Facebook",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

private suspend fun signInUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    context: Context
): Result<Unit> {
    return try {
        val isUserLoggedIn = mutableStateOf(getUserLoggedInStates(context))
        if (email.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password).await()
            val user = auth.currentUser
            if (user != null) {
                isUserLoggedIn.value = true
                saveUserLoggedInStates(context, isUserLoggedIn.value)
            } else {
                isUserLoggedIn.value = true
                saveUserLoggedInStates(context, isUserLoggedIn.value)
            }
            Result.success(Unit)
        } else {
            Result.failure(Exception("Email and password cannot be empty"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Preview(showBackground = true)
@Composable
fun SignInPreview() {
    SignInScreen(
        auth = FirebaseAuth.getInstance(),
        onNavigateToSignUp = {},
        onSignInSuccess = {}
    )
}