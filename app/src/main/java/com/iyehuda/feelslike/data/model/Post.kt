package com.iyehuda.feelslike.data.model

import com.google.android.gms.maps.model.LatLng

data class Post(
    val id: String = "",
    val username: String = "",
    val weather: String = "",
    val temperature: Double = 0.0,
    val description: String = "",
    val imageUrl: String? = null,
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
) {
    fun getLocation() = LatLng(latitude, longitude)
}
