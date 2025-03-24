package com.iyehuda.feelslike.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.iyehuda.feelslike.data.model.Post
import java.io.IOException

class MapViewModel : ViewModel() {
    
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts
    
    private val _searchResult = MutableLiveData<LatLng?>()
    val searchResult: LiveData<LatLng?> = _searchResult
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    init {
        //loadMockPosts()
    }
    
//    private fun loadMockPosts() {
//        val mockPosts = listOf(
//            Post("1", LatLng(40.7128, -74.0060), "New York Post", "Content 1", "28°C", "Sunny"),
//            Post("2", LatLng(34.0522, -118.2437), "LA Post", "Content 2", "24°C", "Clear"),
//            Post("3", LatLng(51.5074, -0.1278), "London Post", "Content 3", "18°C", "Cloudy"),
//            Post("4", LatLng(35.6762, 139.6503), "Tokyo Post", "Content 4", "22°C", "Partly Cloudy"),
//            Post("5", LatLng(32.0853, 34.7818), "Tel Aviv Post", "Welcome to Tel Aviv!", "30°C", "Hot and Sunny")
//        )
//        _posts.value = mockPosts
//    }
    
    fun searchLocation(geocoder: (String) -> Pair<Double, Double>?, locationName: String) {
        try {
            val result = geocoder(locationName)
            if (result != null) {
                val (latitude, longitude) = result
                _searchResult.value = LatLng(latitude, longitude)
            } else {
                _errorMessage.value = "Location not found"
            }
        } catch (e: IOException) {
            _errorMessage.value = "Error searching for location: ${e.message}"
        }
    }
    
    fun getDefaultLocation(): LatLng {
        return LatLng(32.0853, 34.7818) // Tel Aviv
    }
}
