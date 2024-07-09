package com.example.pcpower.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterCredentials(
    val username: String,
    val password: String,
    val confirm: String
)