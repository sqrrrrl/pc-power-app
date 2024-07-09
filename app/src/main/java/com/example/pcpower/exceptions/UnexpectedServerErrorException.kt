package com.example.pcpower.exceptions

const val UNKNOWN_ERROR_MESSAGE = "The server returned an unknown error"

class UnexpectedServerErrorException: Exception(UNKNOWN_ERROR_MESSAGE)