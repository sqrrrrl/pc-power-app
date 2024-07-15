package com.example.pcpower.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pcpower.api.PcPowerAPIService
import com.example.pcpower.persistance.AuthRepo
import com.example.pcpower.state.AppState
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var state by mutableStateOf(AppState.IDLE)
        private set

    private lateinit var apiService: PcPowerAPIService

    fun changeUsername(username: String){ this.username = username }

    fun changePassword(password: String){ this.password = password }

    fun submit() {
        error = null
        state = AppState.LOADING
        viewModelScope.launch {
            try {
                apiService.login(username, password)
                state = AppState.SUCCESS
            }catch (e: Exception){
                error = e.message
                state = AppState.ERROR
            }
        }
    }

    fun initialize(){
        state = AppState.IDLE
        val context = getApplication<Application>().applicationContext
        val authRepo = AuthRepo(context)
        apiService = PcPowerAPIService(authRepo)
    }
}