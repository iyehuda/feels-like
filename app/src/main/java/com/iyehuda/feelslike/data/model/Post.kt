package com.iyehuda.feelslike.data.model

import com.google.android.gms.maps.model.LatLng

data class Post(
    val id: String,
    val location: LatLng,
    val title: String,
    val content: String
)
