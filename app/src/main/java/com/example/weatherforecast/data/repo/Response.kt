package com.example.weatherforecast.data.repo

sealed class Response<T> {
    data object Loading : Response<Nothing>()
    data object Success : Response<Nothing>()
    data class Failure(val error: Throwable) : Response<Nothing>()
}

