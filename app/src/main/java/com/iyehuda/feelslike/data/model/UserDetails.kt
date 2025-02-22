package com.iyehuda.feelslike.data.model

/**
 * Data class that captures user information for logged in users retrieved from AuthRepository
 */
data class UserDetails(
    val email: String,
    val displayName: String,
)
