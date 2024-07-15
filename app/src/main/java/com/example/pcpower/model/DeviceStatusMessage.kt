package com.example.pcpower.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatusMessage(
    val id: String,
    val status: Int,
    val online: Boolean
)
