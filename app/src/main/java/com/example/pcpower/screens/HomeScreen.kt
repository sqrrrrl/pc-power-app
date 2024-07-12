package com.example.pcpower.screens

import android.graphics.BlurMaskFilter
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
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
            .padding(innerPadding)
        ){
            if(homeViewModel.state == AppState.LOADING){
                CircularProgressIndicator(modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center))
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(homeViewModel.devices.online){
                    DeviceCard(it)
                }
                items(homeViewModel.devices.offline){
                    DeviceCard(it)
                }
            }
            PullToRefreshContainer(state = pullState, modifier = Modifier.align(Alignment.TopCenter))
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

@Composable
fun DeviceCard(device: Device){
    var shadowColor = Color.Transparent
    if(device.online){
        shadowColor = if(device.status == 1) Color.Green else Color.Red
    }
    Card(
        modifier = Modifier
            .padding(remember { PaddingValues(20.dp, 10.dp) })
            .shadowCustom(color = shadowColor, offsetX = 4.dp, offsetY = 4.dp, blurRadius = 4.dp, shapeRadius = 10.dp)
    ) {
        Column (modifier = Modifier.padding(10.dp)){
            Text(text = device.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.size(5.dp))
            InfoRow(title = "ID:", info = device.code)
            InfoRow(title = "Secret:", info = device.secret)
            InfoRow(title = "PC status:", info = if (device.status == 1) "On" else "Off")
            InfoRow(title = "Online:", info = if(device.online) "Yes" else "No")
        }
    }
}

@Composable
fun InfoRow(title: String, info: String){
    Row (
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(text = title)
        Text(text = info)
    }
}

//https://gist.github.com/Andrew0000/3edb9c25ebc20a2935c9ff4805e05f5d
fun Modifier.shadowCustom(
    color: Color = Color.Black,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
    shapeRadius: Dp = 0.dp,
) = composed {
    val paint: Paint = remember { Paint() }
    val blurRadiusPx = blurRadius.px(LocalDensity.current)
    val maskFilter = remember {
        BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)
    }
    drawBehind {
        drawIntoCanvas { canvas ->
            val frameworkPaint = paint.asFrameworkPaint()
            if (blurRadius != 0.dp) {
                frameworkPaint.maskFilter = maskFilter
            }
            frameworkPaint.color = color.toArgb()

            val leftPixel = offsetX.toPx()
            val topPixel = offsetY.toPx()
            val rightPixel = size.width + leftPixel
            val bottomPixel = size.height + topPixel

            if (shapeRadius > 0.dp) {
                val radiusPx = shapeRadius.toPx()
                canvas.drawRoundRect(
                    left = leftPixel,
                    top = topPixel,
                    right = rightPixel,
                    bottom = bottomPixel,
                    radiusX = radiusPx,
                    radiusY = radiusPx,
                    paint = paint,
                )
            } else {
                canvas.drawRect(
                    left = leftPixel,
                    top = topPixel,
                    right = rightPixel,
                    bottom = bottomPixel,
                    paint = paint,
                )
            }
        }
    }
}

private fun Dp.px(density: Density): Float =
    with(density) { toPx() }