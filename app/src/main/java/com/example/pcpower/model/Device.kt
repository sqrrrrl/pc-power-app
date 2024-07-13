package com.example.pcpower.model

import kotlinx.serialization.Serializable

@Serializable
data class Device(
    val id: String,
    var name: String,
    val code: String,
    val secret: String,
    var status: Int = 0,
    var online: Boolean = false
)
