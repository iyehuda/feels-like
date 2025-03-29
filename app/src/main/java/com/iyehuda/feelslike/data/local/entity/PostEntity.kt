package com.iyehuda.feelslike.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iyehuda.feelslike.data.model.Post

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val weather: String,
    val temperature: Double,
    val description: String,
    val imageUrl: String?,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val locationString: String?,
    val createdAt: Long,
    val isSync: Boolean = false // To track if the post is synced with Firebase
) {
    fun toPost(): Post = Post(
        id = id,
        username = username,
        weather = weather,
        temperature = temperature,
        description = description,
        imageUrl = imageUrl,
        userId = userId,
        latitude = latitude,
        longitude = longitude,
        locationString = locationString,
        createdAt = createdAt
    )

    companion object {
        fun fromPost(post: Post, isSync: Boolean = true) = PostEntity(
            id = post.id,
            username = post.username,
            weather = post.weather,
            temperature = post.temperature,
            description = post.description,
            imageUrl = post.imageUrl,
            userId = post.userId,
            latitude = post.latitude,
            longitude = post.longitude,
            locationString = post.locationString,
            createdAt = post.createdAt,
            isSync = isSync
        )
    }
} 