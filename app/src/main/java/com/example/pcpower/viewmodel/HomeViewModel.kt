package com.example.pcpower.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pcpower.api.PcPowerAPIService
import com.example.pcpower.exceptions.TokenInvalidException
import com.example.pcpower.model.DeviceList
import com.example.pcpower.persistance.AuthRepo
import com.example.pcpower.state.AppState
import kotlinx.coroutines.launch

class HomeViewModel(application: Application): AndroidViewModel(application) {

    var devices by mutableStateOf(DeviceList())
        private set
    var state by mutableStateOf(AppState.IDLE)
        private set

    private lateinit var apiService: PcPowerAPIService

    fun initialize(){
        state = AppState.IDLE
        val context = getApplication<Application>().applicationContext
        val authRepo = AuthRepo(context)
        apiService = PcPowerAPIService(authRepo)
    }

    fun fetchDevices(){
        viewModelScope.launch {
            try {
                devices = apiService.getDevices()
            }catch (e: TokenInvalidException){
                state = AppState.UNAUTHENTICATED
            }
        }
    }
}