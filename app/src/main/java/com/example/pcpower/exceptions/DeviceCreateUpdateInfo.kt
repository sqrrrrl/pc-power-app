package com.example.pcpower.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class DeviceCreateUpdateInfo(
    val name: String
)
