package com.example.pcpower.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pcpower.R
import com.example.pcpower.state.AppState
import com.example.pcpower.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(onSuccess: () -> Unit){
    val registerViewModel = viewModel<RegisterViewModel>()
    LaunchedEffect(Unit) {
        registerViewModel.initialize()
    }
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ){
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ){
            Text(text = stringResource(R.string.register), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(20.dp))
            TextField(
                placeholder = { Text(text = stringResource(R.string.username)) },
                value = registerViewModel.username,
                onValueChange = { registerViewModel.changeUsername(it) }
            )
            TextField(
                placeholder = { Text(text = stringResource(R.string.password)) },
                value = registerViewModel.password,
                onValueChange = { registerViewModel.changePassword(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
            TextField(
                placeholder = { Text(text = stringResource(R.string.confirm_password)) },
                value = registerViewModel.confirm,
                onValueChange = { registerViewModel.changeConfirm(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation()
            )
            if(registerViewModel.state == AppState.ERROR){
                Column {
                    for(error in registerViewModel.errors){
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                Button(onClick = {
                    registerViewModel.submit()
                }) {
                    Text(text = stringResource(R.string.submit))
                }
                if(registerViewModel.state == AppState.LOADING){
                    CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(25.dp))
                }
            }
            if (registerViewModel.state == AppState.SUCCESS){
                LaunchedEffect(Unit) {
                    onSuccess()
                }
            }
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = stringResource(R.string.have_an_account))
            Button(onClick = { onSuccess() }) {
                Text(text = stringResource(id = R.string.login))
            }
        }
    }
}