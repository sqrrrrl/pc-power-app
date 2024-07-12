package com.example.pcpower.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pcpower.model.Device
import com.example.pcpower.state.AppState
import com.example.pcpower.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit){
    val homeViewModel = viewModel<HomeViewModel>()
    LaunchedEffect(Unit) {
        homeViewModel.initialize()
        homeViewModel.fetchDevices()
    }
    val pullState = rememberPullToRefreshState()
    Scaffold (
        topBar = { TopAppBar(
            title = {
                Text(text = "Home")
            },
            actions = {
                IconButton(onClick = { homeViewModel.logout() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                }
            }
        )}
    ) { innerPadding ->
        Box(modifier = Modifier
            .nestedScroll(pullState.nestedScrollConnection)
            .padding(innerPadding)){
            PullToRefreshContainer(state = pullState, modifier = Modifier.align(Alignment.TopCenter))
            if(homeViewModel.state == AppState.LOADING){
                CircularProgressIndicator(modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center))
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(homeViewModel.devices.online){
                    Text(text = it.toString())
                }
                items(homeViewModel.devices.offline){
                    Text(text = it.toString())
                }
            }
        }
    }
    if(pullState.isRefreshing){
        LaunchedEffect(Unit) {
            homeViewModel.fetchDevices()
            pullState.endRefresh()
        }
    }
    if(homeViewModel.error != null){
        Toast.makeText(LocalContext.current, homeViewModel.collectError(), Toast.LENGTH_LONG).show()
    }
    if(homeViewModel.state == AppState.UNAUTHENTICATED){
        LaunchedEffect(Unit) {
            onLogout()
        }
    }
}