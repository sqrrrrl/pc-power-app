package com.example.pcpower.exceptions

const val UNKNOWN_ERROR_MESSAGE = "The server returned an unknown error"

class UnexpectedServerErrorException(data: String = ""): Exception("$UNKNOWN_ERROR_MESSAGE: $data")