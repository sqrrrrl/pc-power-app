package com.example.pcpower.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pcpower.R
import com.example.pcpower.state.AppState
import com.example.pcpower.viewmodel.LoginViewModel
import kotlin.math.log

@Composable
fun LoginScreen(onSuccess: () -> Unit, goToRegister: () -> Unit){
    val loginViewModel = viewModel<LoginViewModel>()
    LaunchedEffect(Unit) {
        loginViewModel.initialize()
    }
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ){
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ){
            Text(text = stringResource(R.string.login), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp))
            TextField(
                placeholder = { Text(text = stringResource(R.string.username)) },
                value = loginViewModel.username,
                onValueChange = { loginViewModel.changeUsername(it) }
            )
            TextField(
                placeholder = { Text(text = stringResource(R.string.password)) },
                value = loginViewModel.password,
                onValueChange = { loginViewModel.changePassword(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
            if(loginViewModel.state == AppState.ERROR){
                Text(text = loginViewModel.error!!, color = MaterialTheme.colorScheme.error)
            }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                Button(onClick = {
                    loginViewModel.submit()
                }) {
                    Text(text = stringResource(R.string.submit))
                }
                if(loginViewModel.state == AppState.LOADING){
                    CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(25.dp))
                }
            }
            if (loginViewModel.state == AppState.SUCCESS){
                LaunchedEffect(Unit) {
                    onSuccess()
                }
            }
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = stringResource(R.string.no_account_yet))
            Button(onClick = { goToRegister() }) {
                Text(text = stringResource(R.string.register))
            }
        }
    }
}