package com.example.urbango.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.urbango.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit = {},
){
   var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }

    Scaffold(
        Modifier.background(Color(0xFFFFFFFF))
    ) { paddingValues ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            Column (
                Modifier.fillMaxWidth()
            ){
                Box (modifier = Modifier.fillMaxWidth()){

                    Row (
                        Modifier.align(Alignment.TopEnd)
                            .padding(16.dp)
                    ){
                        Text("Already have an account ?",
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                            ),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onNavigateToLogin()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF61ABF3)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sign In")
                        }
                    }
                }
                Image(
                    painter = painterResource(R.drawable.signinimage),
                    contentDescription = null,
                    modifier =
                        Modifier.fillMaxWidth()
                            .fillMaxHeight(0.5f),
                    contentScale = ContentScale.Crop
                )
                OutlinedTextField(
                    value = userName,
                    onValueChange = {userName = it},
                    label = {
                        Text("Username",
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                            ),
                            modifier = Modifier.align(Alignment.Start)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor =Color.Black,
                        focusedBorderColor = Color.DarkGray,
                        unfocusedBorderColor = Color.Black
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp),
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {email = it},
                    label = {
                        Text("Email",
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                            ),
                            modifier = Modifier.align(Alignment.Start)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor =Color.Black,
                        focusedBorderColor = Color.DarkGray,
                        unfocusedBorderColor = Color.Black
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp),
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {password = it},
                    label = {
                        Text("Password",
                            style = TextStyle(
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = MaterialTheme.typography.bodyMedium.fontWeight,
                                fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                            ),
                            modifier = Modifier.align(Alignment.Start)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                        .padding(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor =Color.Black,
                        focusedBorderColor = Color.DarkGray,
                        unfocusedBorderColor = Color.Black
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 15.dp, bottomEnd = 15.dp),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(
                                imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Password Visibility",
                            )
                        }
                    }
                )
                Button(
                    onClick = {},
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(
                        "Sign Up",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            fontWeight = MaterialTheme.typography.labelLarge.fontWeight
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview(){
    SignUpScreen(onNavigateToLogin = {})
}
