package com.iyehuda.feelslike.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iyehuda.feelslike.data.model.Post
import java.io.IOException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {
    
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts
    
    private val _searchResult = MutableLiveData<LatLng?>()
    val searchResult: LiveData<LatLng?> = _searchResult
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    init {
        loadMockPosts()
        fetchPosts()
    }
    
    private fun loadMockPosts() {
        val mockPosts = listOf(
            Post(
                id = "mock1",
                username = "New York User",
                weather = "Sunny",
                temperature = 28.0,
                description = "Beautiful day in NYC!",
                latitude = 40.7128,
                longitude = -74.0060,
                createdAt = System.currentTimeMillis()
            ),
            Post(
                id = "mock2",
                username = "LA User",
                weather = "Clear",
                temperature = 24.0,
                description = "Perfect weather in LA",
                latitude = 34.0522,
                longitude = -118.2437,
                createdAt = System.currentTimeMillis()
            ),
            Post(
                id = "mock3",
                username = "London User",
                weather = "Cloudy",
                temperature = 18.0,
                description = "Typical London weather",
                latitude = 51.5074,
                longitude = -0.1278,
                createdAt = System.currentTimeMillis()
            ),
            Post(
                id = "mock4",
                username = "Tokyo User",
                weather = "Partly Cloudy",
                temperature = 22.0,
                description = "Nice day in Tokyo",
                latitude = 35.6762,
                longitude = 139.6503,
                createdAt = System.currentTimeMillis()
            )
        )
        _posts.value = mockPosts
    }
    
    private fun fetchPosts() {
        firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Error loading posts: ${error.message}"
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val postsList = it.toObjects(Post::class.java)
                    // Combine mock posts with real posts
                    val currentPosts = _posts.value ?: emptyList()
                    _posts.value = currentPosts + postsList
                }
            }
    }
    
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

    fun calculateOffsetPosition(latLng: LatLng, index: Int, total: Int): LatLng {
        // Calculate a small offset based on the index
        // This creates a circular pattern around the original position
        val radius = 0.0001 // Approximately 10 meters
        val angle = (2 * Math.PI * index) / total
        val offsetLat = latLng.latitude + (radius * Math.cos(angle))
        val offsetLng = latLng.longitude + (radius * Math.sin(angle))
        return LatLng(offsetLat, offsetLng)
    }
}
