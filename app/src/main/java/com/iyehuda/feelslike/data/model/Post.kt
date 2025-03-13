package com.iyehuda.feelslike.data.model

data class Post(
    val id: String = "",
    val username: String = "",
    val weather: String = "",
    val temperature: Double = 0.0,
    val description: String = "",
    val imageUrl: String? = null,
    val userId: String = "",
    val location: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
