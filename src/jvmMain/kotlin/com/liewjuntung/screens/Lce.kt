package com.liewjuntung.screens

sealed class Lce<out T> {
    object Loading : Lce<Nothing>() // 1

    data class Content<T>(val data: T, val error: String? = null, val success: String? = null) : Lce<T>() // 2
    data class Error(val error: Throwable) : Lce<Nothing>() // 3
}