package com.example.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PasswordSetupScreen(onPasswordSet: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Calculator",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Set up a security PIN for your calculator",
            fontSize = 16.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                // Remove spaces and newlines
                password = it.filter { char -> char != ' ' && char != '\n' }
                errorMessage = ""
            },
            label = { Text("Enter PIN") },
            placeholder = { Text("4-9 digits and % sign") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.Blue,
                unfocusedLabelColor = Color.Gray,
                focusedPlaceholderColor = Color.DarkGray
            )
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                // Remove spaces and newlines
                confirmPassword = it.filter { char -> char != ' ' && char != '\n' }
                errorMessage = ""
            },
            label = { Text("Confirm PIN") },
            placeholder = { Text("Re-enter your PIN") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.Blue,
                unfocusedLabelColor = Color.Gray,
                focusedPlaceholderColor = Color.DarkGray
            )
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                val validationError = validatePassword(password)
                when {
                    validationError != null -> {
                        errorMessage = validationError
                    }
                    confirmPassword.isEmpty() -> {
                        errorMessage = "Please confirm your PIN"
                    }
                    password != confirmPassword -> {
                        errorMessage = "PINs do not match"
                    }
                    else -> {
                        onPasswordSet(password)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Set PIN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PIN Requirements:\n• 4-9 characters\n• Numbers (0-9) only",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Validates the password against the requirements:
 * - 4-9 characters
 * - Only numbers (0-9) and percent sign (%)
 * Returns error message if invalid, null if valid
 */
private fun validatePassword(password: String): String? {
    return when {
        password.isEmpty() -> "PIN is required"
        password.length < 4 -> "PIN must be at least 4 characters"
        password.length > 9 -> "PIN must not exceed 9 characters"
        !isValidPINFormat(password) -> "PIN can only contain digits (0-9) and % sign"
        else -> null
    }
}

/**
 * Checks if the password contains only allowed characters
 * Allowed: digits (0-9) and percent sign (%)
 */
private fun isValidPINFormat(password: String): Boolean {
    val allowedCharacters = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '%')
    return password.all { it in allowedCharacters }
}
