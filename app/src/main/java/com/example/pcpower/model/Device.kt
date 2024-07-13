package com.example.pcpower.model

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    val name: String,
    val code: String,
    val secret: String,
    val status: Int = 0,
    val online: Boolean = false
)
