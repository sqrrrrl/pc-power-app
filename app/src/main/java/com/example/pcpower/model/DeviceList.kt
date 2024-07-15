package com.example.pcpower.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceList(
    val online: List<Device> = emptyList(),
    val offline: List<Device> = emptyList()
)
