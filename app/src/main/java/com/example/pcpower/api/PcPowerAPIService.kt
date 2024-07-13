package com.example.pcpower.api

import android.util.Log
import com.example.pcpower.BuildConfig
import com.example.pcpower.exceptions.DeviceCommandFailedException
import com.example.pcpower.exceptions.DeviceCreateUpdateInfo
import com.example.pcpower.exceptions.InvalidCredentialsException
import com.example.pcpower.exceptions.InvalidInputProvidedException
import com.example.pcpower.exceptions.TokenInvalidException
import com.example.pcpower.exceptions.UnexpectedServerErrorException
import com.example.pcpower.exceptions.UsernameAlreadyInUseException
import com.example.pcpower.model.ApiError
import com.example.pcpower.model.AuthError
import com.example.pcpower.model.DeviceCommand
import com.example.pcpower.model.DeviceList
import com.example.pcpower.model.LoginCredentials
import com.example.pcpower.model.RegisterCredentials
import com.example.pcpower.model.Token
import com.example.pcpower.persistance.AuthRepo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.plugin
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders

const val ERR_NO_TOKEN = "No token has been found in storage"

class PcPowerAPIService(private val authRepo: AuthRepo) {
    private val apiUrl = BuildConfig.apiUrl
    private val client = HttpClient(CIO).config{
        defaultRequest {
            url(apiUrl)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
        install(ContentNegotiation){
            json()
        }
        install(HttpTimeout){
            connectTimeoutMillis = 3000
            requestTimeoutMillis = 10000
        }
    }

    init {
        client.plugin(HttpSend).intercept { request ->
            request.headers.remove(HttpHeaders.Authorization) //Token is set twice after 3XX which causes an authentication error
            when(request.url.buildString().removePrefix(apiUrl)){
                "/auth/login" -> {
                    execute(request)
                }
                "/auth/register" -> {
                    execute(request)
                }
                "/auth/refresh_token" -> {
                    request.bearerAuth(getTokenOrThrow())
                    execute(request)
                }
                else -> {
                    request.bearerAuth(getTokenOrThrow())
                    val initialRequest = execute(request)
                    if(initialRequest.response.status == HttpStatusCode.Unauthorized){
                        refreshToken()
                        request.headers.remove(HttpHeaders.Authorization)
                        request.bearerAuth(getTokenOrThrow())
                        execute(request)
                    }else{
                        initialRequest
                    }
                }
            }
        }
    }

    private suspend fun getTokenOrThrow(): String{
        return authRepo.getToken() ?: throw TokenInvalidException(ERR_NO_TOKEN)
    }

    private suspend fun refreshToken(){
        val resp = client.request("/auth/refresh_token") {
            method = HttpMethod.Get
        }
        when(resp.status){
            HttpStatusCode.OK -> {
                authRepo.saveToken(resp.body<Token>())
            }
            HttpStatusCode.Unauthorized -> {
                val error = resp.body<AuthError>()
                throw TokenInvalidException(error.message)
            }
            else -> {
                throw UnexpectedServerErrorException(resp.bodyAsText())
            }
        }
    }

    suspend fun login(username: String, password: String){
        val resp = client.request("/auth/login") {
            method = HttpMethod.Post
            setBody(LoginCredentials(username, password))
        }
        when(resp.status){
            HttpStatusCode.OK -> {
                authRepo.saveToken(resp.body<Token>())
            }
            HttpStatusCode.Unauthorized -> {
                val error = resp.body<AuthError>()
                throw InvalidCredentialsException(error.message)
            }
            else -> {
                throw UnexpectedServerErrorException(resp.bodyAsText())
            }
        }
    }

    suspend fun register(username: String, password: String, confirm: String){
        val resp = client.request("/auth/register") {
            method = HttpMethod.Post
            setBody(RegisterCredentials(username, password, confirm))
        }
        when(resp.status){
            HttpStatusCode.NoContent -> {
                return
            }
            HttpStatusCode.UnprocessableEntity -> {
                val error = resp.body<ApiError>().error
                throw UsernameAlreadyInUseException(error.message)
            }
            HttpStatusCode.BadRequest -> {
                val error = resp.body<ApiError>().error
                throw InvalidInputProvidedException(error.description, error.errors)
            }
            else -> {
                throw UnexpectedServerErrorException(resp.bodyAsText())
            }
        }
    }

    suspend fun getDevices(): DeviceList{
        val resp = client.request("/user/devices") {
            method = HttpMethod.Get
        }
        when(resp.status){
            HttpStatusCode.OK -> {
                return resp.body<DeviceList>()
            }
            HttpStatusCode.ServiceUnavailable -> {
                val error = resp.body<ApiError>().error
                throw DeviceCommandFailedException(error.message)
            }
            else -> {
                throw UnexpectedServerErrorException(resp.bodyAsText())
            }
        }
    }

    suspend fun sendPowerSwitch(deviceId: String, hard: Boolean){
        val resp = client.request("/devices/power-switch"){
            method = HttpMethod.Post
            setBody(DeviceCommand(deviceId, hard))
        }
        when(resp.status){
            HttpStatusCode.NoContent -> {
                return
            }
            HttpStatusCode.ServiceUnavailable -> {
                val error = resp.body<ApiError>().error
                throw DeviceCommandFailedException(error.message)
            }
            else -> {
                throw UnexpectedServerErrorException(resp.bodyAsText())
            }
        }
    }

    suspend fun sendResetSwitch(deviceId: String){
        val resp = client.request("/devices/reset-switch"){
            method = HttpMethod.Post
            setBody(DeviceCommand(deviceId))
        }
        when(resp.status){
            HttpStatusCode.NoContent -> {
                return
            }
            else -> {
                val error = resp.body<ApiError>().error
                throw DeviceCommandFailedException(error.message)
            }
        }
    }

    suspend fun deleteDevice(deviceId: String){
        val resp = client.request("/user/devices/$deviceId"){
            method = HttpMethod.Delete
        }
        if(resp.status != HttpStatusCode.NoContent){
            throw UnexpectedServerErrorException(resp.bodyAsText())
        }
    }

    suspend fun renameDevice(deviceId: String, newName: String){
        val resp = client.request("/user/devices/$deviceId"){
            method = HttpMethod.Put
            setBody(DeviceCreateUpdateInfo(newName))
        }
        when(resp.status){
            HttpStatusCode.OK -> {
                return
            }
            HttpStatusCode.BadRequest -> {
                val error = resp.body<ApiError>().error
                throw InvalidInputProvidedException(error.description, error.errors)
            }
            else -> {
                throw UnexpectedServerErrorException(resp.bodyAsText())
            }
        }
    }
}