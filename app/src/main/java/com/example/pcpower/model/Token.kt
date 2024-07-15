package com.example.pcpower.model

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val code: Int,
    val token: String,
    val expire: String
)