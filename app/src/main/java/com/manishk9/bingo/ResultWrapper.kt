package com.manishk9.bingo

sealed class ResultWrapper<out T>() {
    data class Success<T>(val data: T) : ResultWrapper<T>()
    data class Error(val error: String) : ResultWrapper<Nothing>()
    object Loading : ResultWrapper<Nothing>()
}