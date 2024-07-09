package com.example.pcpower.api

import com.example.pcpower.BuildConfig
import com.example.pcpower.exceptions.InvalidCredentialsException
import com.example.pcpower.exceptions.InvalidInputProvidedException
import com.example.pcpower.exceptions.UnexpectedServerErrorException
import com.example.pcpower.exceptions.UsernameAlreadyInUseException
import com.example.pcpower.model.ApiError
import com.example.pcpower.model.ApiErrorDetails
import com.example.pcpower.model.AuthError
import com.example.pcpower.model.LoginCredentials
import com.example.pcpower.model.RegisterCredentials
import com.example.pcpower.model.Token
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

object PcPowerAPIService {
    private val apiUrl = BuildConfig.apiUrl
    private val client = HttpClient(CIO){
        install(ContentNegotiation){
            json()
        }
    }

    suspend fun login(username: String, password: String): Token{
        val resp = client.request("$apiUrl/auth/login") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
            setBody(LoginCredentials(username, password))
        }
        when(resp.status){
            HttpStatusCode.OK -> {
                return resp.body<Token>()
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
        val resp = client.request("$apiUrl/auth/register") {
            method = HttpMethod.Post
            contentType(ContentType.Application.Json)
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
}