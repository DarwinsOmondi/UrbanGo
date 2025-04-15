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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.urbango.R
import com.example.urbango.repository.SupabaseClient.client
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import androidx.core.content.edit
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    auth: FirebaseAuth
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("") }
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
                .verticalScroll(rememberScrollState()) // Add scroll for smaller screens
        ) {
            // Header with sign in option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onNavigateToLogin,
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
                    Text("Sign In", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Image section
            Image(
                painter = painterResource(R.drawable.signiupimage),
                contentDescription = "Sign up illustration",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            )

            // Title
            Text(
                "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .align(Alignment.Start)
            )

            // Subtitle
            Text(
                "Join us to explore amazing places",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username field
            OutlinedTextField(
                value = userName, textStyle = MaterialTheme.typography.bodyMedium,
                onValueChange = { userName = it },
                label = {
                    Text(
                        "Username",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
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

            // Password requirements hint
            Text(
                "Use at least 8 characters with a mix of letters and numbers",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sign up button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val results = signUpUser(auth, userName, email, password, context)
                            client.auth.signUpWith(Email) {
                                this.email = email
                                this.password = password
                                data = buildJsonObject {
                                    put("name", JsonPrimitive(userName))
                                }
                            }
                            if (results.isSuccess) {
                                onSignUpSuccess()
                            } else {
                                errorMessage = results.exceptionOrNull()?.message
                                isLoading = false
                            }
                            isLoading = false
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
                ),
                enabled = userName.isNotEmpty() && email.isNotEmpty() && password.length >= 8
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        "Sign Up",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Error message
            errorMessage?.let { message ->
                val userFriendlyMessage = when {
                    // Email errors
                    message.contains(
                        "Email is required",
                        ignoreCase = true
                    ) -> "Please enter your email address"

                    message.contains(
                        "Invalid email address",
                        ignoreCase = true
                    ) -> "Please enter a valid email address"

                    message.contains(
                        "already registered",
                        ignoreCase = true
                    ) -> "This email is already registered"

                    // Password errors
                    message.contains(
                        "Password is required",
                        ignoreCase = true
                    ) -> "Please enter your password"

                    message.contains("Password should be at least", ignoreCase = true) ->
                        "Password must be at least 6 characters"

                    message.contains("weak_password", ignoreCase = true) ->
                        "Password is too weak - include numbers and special characters"

                    // User metadata errors
                    message.contains(
                        "name is required",
                        ignoreCase = true
                    ) -> "Please enter your name"

                    message.contains(
                        "username is required",
                        ignoreCase = true
                    ) -> "Please choose a username"

                    // Supabase-specific errors
                    message.contains(
                        "User already registered",
                        ignoreCase = true
                    ) -> "Account already exists"

                    message.contains(
                        "Signup requires a valid password",
                        ignoreCase = true
                    ) -> "Invalid password format"

                    message.contains(
                        "Unable to validate email address",
                        ignoreCase = true
                    ) -> "Invalid email format"

                    message.contains("For security purposes", ignoreCase = true) ->
                        "Too many attempts - try again later or reset password"

                    // Network/technical errors
                    message.contains(
                        "Network error",
                        ignoreCase = true
                    ) -> "Network error - please check your connection"

                    message.contains(
                        "Failed to fetch",
                        ignoreCase = true
                    ) -> "Connection failed - try again"

                    message.contains(
                        "Internal server error",
                        ignoreCase = true
                    ) -> "Server error - please try again later"

                    // Fallback
                    else -> "Sign up failed: Please check your details and try again"
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

            // Terms and conditions
            Text(
                "By signing up, you agree to our Terms and Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable { /* Show terms dialog */ }
            )

            // Or divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
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

            // Social sign up options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                // Google sign up button
                IconButton(
                    onClick = { /* Handle Google sign up */ },
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24), // Add your Google icon
                        contentDescription = "Sign up with Google",
                        tint = Color.Unspecified
                    )
                }

                // Facebook sign up button
                IconButton(
                    onClick = { /* Handle Facebook sign up */ },
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24), // Add your Facebook icon
                        contentDescription = "Sign up with Facebook",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

private suspend fun signUpUser(
    auth: FirebaseAuth,
    userName: String,
    userEmail: String,
    userPassword: String,
    context: Context
): Result<Unit> {
    val isUserLoggedIn = mutableStateOf(getUserLoggedInStates(context))
    return try {
        if (userName.isNotEmpty() && userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(userEmail, userPassword).await()
            val user = auth.currentUser
            if (user != null) {
                isUserLoggedIn.value = true
                saveUserLoggedInStates(context, isUserLoggedIn.value)
            } else {
                isUserLoggedIn.value = false
                saveUserLoggedInStates(context, isUserLoggedIn.value)
            }
            Result.success(Unit)
        } else {
            Result.failure(Exception("Please fill every fields"))
        }
    } catch (e: Exception) {
        return Result.failure(e)
    }

}

fun saveUserLoggedInStates(context: Context, isLoggedIn: Boolean) {
    val sharedPreferences = context.getSharedPreferences("UrbanGo", Context.MODE_PRIVATE)
    sharedPreferences.edit() { putBoolean("isLoggedIn", isLoggedIn) }
}

fun getUserLoggedInStates(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("UrbanGo", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("isLoggedIn", false)
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    SignUpScreen(
        onNavigateToLogin = {},
        onSignUpSuccess = {},
        auth = FirebaseAuth.getInstance()
    )
}
