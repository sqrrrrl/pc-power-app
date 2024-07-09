package com.example.pcpower.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pcpower.api.PcPowerAPIService
import com.example.pcpower.model.DeviceList
import com.example.pcpower.persistance.AuthRepo
import kotlinx.coroutines.launch

class HomeViewModel(application: Application): AndroidViewModel(application) {

    var devices by mutableStateOf(DeviceList())
        private set

    private var isInitialized = false
    private lateinit var apiService: PcPowerAPIService

    fun initialize(){
        if(isInitialized) return
        val context = getApplication<Application>().applicationContext
        val authRepo = AuthRepo(context)
        apiService = PcPowerAPIService(authRepo)
        viewModelScope.launch {
            devices = apiService.getDevices()
        }
        isInitialized = true
    }
}