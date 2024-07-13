package com.example.pcpower.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceCommand(
    @SerialName("device_id")
    val deviceId: String,
    val hard: Boolean = false
)
