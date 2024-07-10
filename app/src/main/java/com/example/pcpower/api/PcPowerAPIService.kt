package com.example.pcpower.api

import com.example.pcpower.BuildConfig
import com.example.pcpower.exceptions.InvalidCredentialsException
import com.example.pcpower.exceptions.InvalidInputProvidedException
import com.example.pcpower.exceptions.TokenInvalidException
import com.example.pcpower.exceptions.UnexpectedServerErrorException
import com.example.pcpower.exceptions.UsernameAlreadyInUseException
import com.example.pcpower.model.ApiError
import com.example.pcpower.model.AuthError
import com.example.pcpower.model.DeviceList
import com.example.pcpower.model.LoginCredentials
import com.example.pcpower.model.RegisterCredentials
import com.example.pcpower.model.Token
import com.example.pcpower.persistance.AuthRepo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
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
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer

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
        install(Auth){
            bearer {
                loadTokens {
                    BearerTokens(authRepo.getToken(), "")
                }
                refreshTokens {
                    val resp = client.request("/auth/refresh_token") {
                        method = HttpMethod.Get
                        markAsRefreshTokenRequest()
                    }
                    when(resp.status){
                        HttpStatusCode.OK -> {
                            val token: Token = resp.body()
                            authRepo.saveToken(token)
                            BearerTokens(token.token, "")
                        }
                    }
                    null
                }
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
                throw UnexpectedServerErrorException()
            }
        }
    }

    suspend fun register(username: String, password: String, confirm: String){
        val resp = client.request("/auth/register") {
            method = HttpMethod.Post
            setBody(RegisterCredentials(username, password, confirm))
        }
        when(resp.status){
            HttpStatusCode.UnprocessableEntity -> {
                val error = resp.body<ApiError>().error
                throw UsernameAlreadyInUseException(error.message)
            }
            HttpStatusCode.BadRequest -> {
                val error = resp.body<ApiError>().error
                throw InvalidInputProvidedException(error.description, error.errors)
            }
            else -> {
                throw UnexpectedServerErrorException()
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
            HttpStatusCode.Unauthorized -> {
                throw TokenInvalidException(resp.body<AuthError>().message)
            }
            else -> {
                throw UnexpectedServerErrorException()
            }
        }
    }
}