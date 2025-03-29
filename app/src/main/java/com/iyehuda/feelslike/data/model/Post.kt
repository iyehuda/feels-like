package com.iyehuda.feelslike.data.model

import android.net.Uri
import androidx.core.net.toUri
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
    val createdAt: Long = System.currentTimeMillis(),
) {
    val location: LatLng
        get() = LatLng(latitude, longitude)

    val imageUri: Uri?
        get() = imageUrl?.toUri()
}
