package com.example.pcpower.api

import com.example.pcpower.BuildConfig
import com.example.pcpower.exceptions.InvalidCredentialsException
import com.example.pcpower.exceptions.UnexpectedServerErrorException
import com.example.pcpower.model.AuthError
import com.example.pcpower.model.Credentials
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
            setBody(Credentials(username, password))
        }
        when(resp.status){
            HttpStatusCode.OK -> {
                val rtoken: Token = resp.body()
                return rtoken
            }
            HttpStatusCode.Unauthorized -> {
                val error: AuthError = resp.body()
                throw InvalidCredentialsException(error.message)
            }
            else -> {
                throw UnexpectedServerErrorException()
            }
        }
    }
}