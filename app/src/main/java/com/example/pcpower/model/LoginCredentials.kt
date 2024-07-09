package com.example.pcpower.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginCredentials(
    val username: String,
    val password: String
)