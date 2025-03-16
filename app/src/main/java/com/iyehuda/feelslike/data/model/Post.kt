package com.iyehuda.feelslike.data.model

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

data class Post(
    val id: String,
    val location: LatLng,
    val title: String,
    val content: String,
    val temperature: String,
    val weatherDescription: String,
    val profileImageUri: Uri? = null
)
