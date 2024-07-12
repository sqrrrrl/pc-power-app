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

const val ERR_DEVICE_LOAD_FAILED = "Couldn't load the device list"

class HomeViewModel(application: Application): AndroidViewModel(application) {

    var devices by mutableStateOf(DeviceList())
        private set
    var state by mutableStateOf(AppState.IDLE)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private lateinit var apiService: PcPowerAPIService
    private lateinit var authRepo: AuthRepo

    fun initialize(){
        state = AppState.LOADING
        val context = getApplication<Application>().applicationContext
        authRepo = AuthRepo(context)
        apiService = PcPowerAPIService(authRepo)
    }

    fun fetchDevices(){
        viewModelScope.launch {
            try {
                devices = apiService.getDevices()
                state = AppState.IDLE
            }catch (e: TokenInvalidException){
                state = AppState.UNAUTHENTICATED
            }catch (e: Exception){
                error = ERR_DEVICE_LOAD_FAILED
                state = AppState.IDLE
            }
        }
    }

    fun collectError(): String?{
        val tempError = error
        error = null
        return tempError
    }

    fun logout(){
        viewModelScope.launch {
            authRepo.clear()
        }
        state = AppState.UNAUTHENTICATED
    }
}