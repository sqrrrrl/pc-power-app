package com.example.pcpower.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pcpower.action.Action
import com.example.pcpower.api.PcPowerAPIService
import com.example.pcpower.exceptions.DeviceCommandFailedException
import com.example.pcpower.exceptions.InvalidInputProvidedException
import com.example.pcpower.exceptions.TokenInvalidException
import com.example.pcpower.model.Device
import com.example.pcpower.model.DeviceList
import com.example.pcpower.persistance.AuthRepo
import com.example.pcpower.state.AppState
import kotlinx.coroutines.launch

const val ERR_DEVICE_LOAD_FAILED = "Couldn't load the device list"
const val ERR_DEVICE_UNREACHABLE = "The device couldn't be reached"
const val ERR_UNEXPECTED_API_ERROR = "An error occurred while communicating with the api"

class HomeViewModel(application: Application): AndroidViewModel(application) {

    var devices by mutableStateOf(DeviceList())
        private set
    var state by mutableStateOf(AppState.IDLE)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var openInDialog by mutableStateOf<Device?>(null)
        private set
    var currentAction by mutableStateOf<Action?>(null)
        private set
    var name by mutableStateOf("")
        private set

    private lateinit var apiService: PcPowerAPIService
    private lateinit var authRepo: AuthRepo

    fun initialize() {
        state = AppState.LOADING
        val context = getApplication<Application>().applicationContext
        authRepo = AuthRepo(context)
        apiService = PcPowerAPIService(authRepo)
    }

    suspend fun fetchDevices() {
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

    fun collectError(): String? {
        val tempError = error
        error = null
        return tempError
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.clear()
        }
        state = AppState.UNAUTHENTICATED
    }

    fun changeName(name: String) { this.name = name }

    fun openDialog(device: Device){ this.openInDialog = device }
    fun closeDialog() {
        this.openInDialog = null
        this.currentAction = null
        name = ""
    }

    fun changeAction(action: Action) { this.currentAction = action }
    fun confirmAction(){
        viewModelScope.launch {
            try{
                doAction(currentAction, openInDialog, name)
                closeDialog()
            }catch (e: TokenInvalidException){
                state = AppState.UNAUTHENTICATED
            }catch (e: DeviceCommandFailedException) {
                error = ERR_DEVICE_UNREACHABLE
            }catch (e: InvalidInputProvidedException){
                error = e.getErrors().joinToString("\n")
            }catch (e: Exception){
                error = ERR_UNEXPECTED_API_ERROR
            }
        }
    }

    private suspend fun doAction(action: Action?, device: Device?, name: String){
        when(action){
            Action.POWER_ON, Action.POWER_OFF -> {
                apiService.sendPowerSwitch(device!!.id, false)
            }
            Action.REBOOT -> {
                apiService.sendResetSwitch(device!!.id)
            }
            Action.FORCE_SHUTDOWN -> {
                apiService.sendPowerSwitch(device!!.id, true)
            }
            Action.DELETE -> {
                apiService.deleteDevice(device!!.id)
            }
            Action.RENAME -> {
                apiService.renameDevice(device!!.id, name)
            }
            Action.CREATE -> {
                val newDevice = apiService.createDevice(name)
                devices = DeviceList(devices.online, devices.offline.plus(newDevice))
            }
            else -> {}
        }
    }
}