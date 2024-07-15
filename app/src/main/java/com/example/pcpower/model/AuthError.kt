package com.example.pcpower.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthError(
    val code: Int,
    val message: String
)
