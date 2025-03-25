package com.iyehuda.feelslike.data.model

import android.net.Uri
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
    val locationString: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val location: LatLng
        get() = if (latitude != 0.0 && longitude != 0.0) {
            LatLng(latitude, longitude)
        } else {
            // Default to Tel Aviv if no coordinates are provided
            LatLng(32.0853, 34.7818)
        }

    val imageUri: Uri?
        get() = imageUrl?.let { Uri.parse(it) }
}