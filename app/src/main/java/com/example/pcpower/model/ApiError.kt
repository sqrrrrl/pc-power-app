package com.example.pcpower.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val error: ApiErrorDetails
)

@Serializable
data class ApiErrorDetails(
    val id: String,
    val status: Int,
    val title: String,
    val description: String,
    val message: String = "",
    val errors: List<String> = emptyList(),
    val expected: Boolean
)