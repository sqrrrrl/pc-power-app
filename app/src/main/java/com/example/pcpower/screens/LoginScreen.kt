package com.example.pcpower.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pcpower.viewmodel.LoginViewModel
import kotlin.math.log

@Composable
fun LoginScreen(onSuccess: () -> Unit){
    val loginViewModel = viewModel<LoginViewModel>()
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ){
        Text(text = "Login", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp))
        TextField(
            placeholder = { Text(text = "Username") },
            value = loginViewModel.username,
            onValueChange = { loginViewModel.changeUsername(it) }
        )
        TextField(
            placeholder = { Text(text = "Password") },
            value = loginViewModel.password,
            onValueChange = { loginViewModel.changePassword(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )
        Button(onClick = { onSuccess() }) {
            Text(text = "Submit")
        }
    }
}