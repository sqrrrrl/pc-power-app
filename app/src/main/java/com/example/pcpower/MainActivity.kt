package com.example.pcpower

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pcpower.screens.HomeScreen
import com.example.pcpower.screens.LoginScreen
import com.example.pcpower.screens.RegisterScreen
import com.example.pcpower.screens.Screens
import com.example.pcpower.ui.theme.PcPowerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PcPowerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PcPowerApp()
                }
            }
        }
    }
}

@Composable
fun PcPowerApp() {
    val navigationController = rememberNavController()
    NavHost(navController = navigationController, startDestination = Screens.LoginScreen.route){
        composable(Screens.LoginScreen.route){
            LoginScreen(
                onSuccess = {
                    navigationController.navigate(Screens.HomeScreen.route){
                        popUpTo(Screens.LoginScreen.route){ inclusive = true }
                    }
                },
                goToRegister = {
                    navigationController.navigate(Screens.RegisterScreen.route)
                }
            )
        }
        composable(Screens.RegisterScreen.route){
            RegisterScreen {
                navigationController.navigate(Screens.LoginScreen.route)
            }
        }
        composable(Screens.HomeScreen.route){
            HomeScreen()
        }
    }
}