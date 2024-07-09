package com.example.pcpower.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pcpower.api.PcPowerAPIService
import com.example.pcpower.exceptions.InvalidInputProvidedException
import com.example.pcpower.persistance.AuthRepo
import com.example.pcpower.state.AppState
import kotlinx.coroutines.launch

const val ERR_PASS_CONFIRM_NOT_EQUAL = "Passwords do not match"
const val ERR_UNEXPECTED_ERROR = "An unexpected error occurred"

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirm by mutableStateOf("")
        private set
    var errors by mutableStateOf<List<String>>(emptyList())
        private set
    var state by mutableStateOf(AppState.IDLE)

    private var isInitialized = false
    private lateinit var apiService: PcPowerAPIService

    fun changeUsername(username: String){ this.username = username }

    fun changePassword(password: String){ this.password = password }

    fun changeConfirm(confirm: String){ this.confirm = confirm }

    fun submit() {
        if(this.password != this.confirm){
            errors = listOf(ERR_PASS_CONFIRM_NOT_EQUAL)
            state = AppState.ERROR
            return
        }
        errors = emptyList()
        state = AppState.LOADING
        viewModelScope.launch {
            try {
                apiService.register(username, password, confirm)
                state = AppState.SUCCESS
            }catch (e: InvalidInputProvidedException){
                errors = e.getErrors()
                state = AppState.ERROR
            }catch (e: Exception){
                errors = listOf(e.message ?: ERR_UNEXPECTED_ERROR)
                state = AppState.ERROR
            }
        }
    }

    fun initialize(){
        if(isInitialized) return
        val context = getApplication<Application>().applicationContext
        val authRepo = AuthRepo(context)
        apiService = PcPowerAPIService(authRepo)
        isInitialized = true
    }
}