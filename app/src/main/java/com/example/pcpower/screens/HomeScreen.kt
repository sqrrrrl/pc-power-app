package com.example.pcpower.screens

import android.graphics.BlurMaskFilter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pcpower.R
import com.example.pcpower.action.Action
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
                items(homeViewModel.devices){
                    DeviceCard(it){
                        homeViewModel.openDialog(it)
                    }
                }
            }
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset((-20).dp, (-35).dp),
                onClick = { homeViewModel.changeAction(Action.CREATE) }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create new device")
            }
            ManageUIInteractions(homeViewModel)
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
fun ManageUIInteractions(homeViewModel: HomeViewModel){
    val dialogDevice = homeViewModel.openInDialog
    val currentAction = homeViewModel.currentAction
    if(dialogDevice != null && currentAction == null){
        DeviceDialog(dialogDevice.online, pcStatus = dialogDevice.status) { action ->
            homeViewModel.changeAction(action)
            if(action == Action.DISMISS){
                homeViewModel.closeDialog()
            }
        }
    }
    else if(currentAction != null){
        when(currentAction){
            Action.RENAME -> {
                InputDialog(
                    title = "Rename ${dialogDevice?.name}",
                    placeholder = "name",
                    value = homeViewModel.name,
                    onValueChange = { homeViewModel.changeName(it) },
                    onDismiss = { homeViewModel.closeDialog() },
                    onSubmit = { homeViewModel.confirmAction() }
                )
            }
            Action.CREATE -> {
                InputDialog(
                    title = "Create a new device",
                    placeholder = "name",
                    value = homeViewModel.name,
                    onValueChange = { homeViewModel.changeName(it) },
                    onDismiss = { homeViewModel.closeDialog() },
                    onSubmit = { homeViewModel.confirmAction() }
                )
            }
            else -> {
                ConfirmDialog(action = currentAction, deviceName = dialogDevice?.name ?: "",
                    onConfirm = {
                        homeViewModel.confirmAction()
                    },
                    onCancel = { homeViewModel.closeDialog() }
                )
            }
        }
    }
}

@Composable
fun DeviceCard(device: Device, onClick: () -> Unit){
    var shadowColor = Color.Transparent
    if(device.online){
        shadowColor = if(device.status == 1) Color.Green else Color.Red
    }
    Card(
        onClick = { onClick() },
        modifier = Modifier
            .padding(remember { PaddingValues(20.dp, 10.dp) })
            .shadowCustom(
                color = shadowColor,
                offsetX = 4.dp,
                offsetY = 4.dp,
                blurRadius = 4.dp,
                shapeRadius = 10.dp
            )
    ) {
        Column (modifier = Modifier.padding(10.dp)){
            Text(text = device.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.size(5.dp))
            InfoRow(title = "ID:", info = device.code)
            InfoRow(title = "Secret:", info = device.secret)
            if(device.online){
                InfoRow(title = "PC status:", info = if (device.status == 1) "On" else "Off")
            }else{
                InfoRow(title = "PC status:", info = "Unknown")
            }
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

@Composable
fun InputDialog(title: String, placeholder: String, value: String, onValueChange: (String) -> Unit, onDismiss: () -> Unit, onSubmit: () -> Unit){
    Dialog(onDismissRequest = { onValueChange(""); onDismiss() }) {
        Column (
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ){
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = value, onValueChange = onValueChange, placeholder = { Text(text = placeholder) })
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ){
                Button(onClick = { onValueChange(""); onDismiss() }) {
                    Text(text = "Cancel")
                }
                Button(onClick = { onSubmit() }) {
                    Text(text = "Submit")
                }
            }
        }
    }
}

@Composable
fun ConfirmDialog(action: Action, deviceName: String, onConfirm: () -> Unit, onCancel: () -> Unit){
    AlertDialog(
        onDismissRequest = { onCancel() },
        title = {
            Text(text = "Are you sure?")
        },
        text = {
            Text(text = "This will ${action.text.lowercase()} $deviceName")
        },
        dismissButton = {
            Button(onClick = { onCancel() }) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm() }) {
                Text(text = "Confirm")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun DeviceDialog(online: Boolean, pcStatus: Int, onClick: (Action) -> Unit){
    val modifier = if(isSystemInDarkTheme()) Modifier else Modifier.background(color = Color.White, shape = RoundedCornerShape(10.dp))
    Dialog(onDismissRequest = { onClick(Action.DISMISS) }) {
        Column (
            modifier = modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Text(text = "What do you want to do?", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.size(10.dp))
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                if(online){
                    val powerSwitchAction = if(pcStatus == 0) Action.POWER_ON else Action.POWER_OFF
                    ActionButton(imageVector = ImageVector.vectorResource(id = R.drawable.powerswitch), name = powerSwitchAction.text) {
                        onClick(powerSwitchAction)
                    }
                }
                if(pcStatus == 1){
                    ActionButton(imageVector = Icons.Default.Refresh, name = Action.REBOOT.text) {
                        onClick(Action.REBOOT)
                    }
                    ActionButton(imageVector = Icons.Default.Warning, name = Action.FORCE_SHUTDOWN.text) {
                        onClick(Action.FORCE_SHUTDOWN)
                    }
                }
                ActionButton(imageVector = Icons.Default.Edit, name = Action.RENAME.text) {
                    onClick(Action.RENAME)
                }
                ActionButton(imageVector = Icons.Default.Delete, name = Action.DELETE.text) {
                    onClick(Action.DELETE)
                }
            }
        }
    }
}

@Composable
fun ActionButton(imageVector: ImageVector, name: String, onClick: () -> Unit){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = { onClick() }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(10.dp))) {
            Icon(imageVector = imageVector, contentDescription = null)
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.plus(TextStyle(textAlign = TextAlign.Center)),
            modifier = Modifier.widthIn(max=52.dp)
        )
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