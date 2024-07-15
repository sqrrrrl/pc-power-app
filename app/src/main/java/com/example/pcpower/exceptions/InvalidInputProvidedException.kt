package com.example.pcpower.exceptions

class InvalidInputProvidedException(message: String, private val errors: List<String>): Exception(message) {
    fun getErrors(): List<String>{
        return this.errors
    }
}