package com.example.pcpower.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pcpower.state.AppState
import com.example.pcpower.viewmodel.HomeViewModel

@Composable
fun HomeScreen(onLogout: () -> Unit){
    val homeViewModel = viewModel<HomeViewModel>()
    LaunchedEffect(Unit) {
        homeViewModel.initialize()
        homeViewModel.fetchDevices()
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        for(device in homeViewModel.devices.online){
            Text(text = device.toString())
        }
        for (device in homeViewModel.devices.offline){
            Text(text = device.toString())
        }
    }
    if(homeViewModel.state == AppState.UNAUTHENTICATED){
        LaunchedEffect(Unit) {
            onLogout()
        }
    }
}