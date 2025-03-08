package com.iyehuda.feelslike.data.model

data class Post(
    val id: String,
    val username: String,
    val weather: String,
    val temperature: Double,
    val description: String,
    // Optionally add an image URL or resource reference if needed
    val imageUrl: String? = null
)
