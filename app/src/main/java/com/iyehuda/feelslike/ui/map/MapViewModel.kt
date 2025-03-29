package com.iyehuda.feelslike.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _searchResult = MutableLiveData<LatLng?>()
    val searchResult: LiveData<LatLng?> = _searchResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            postRepository.getAllPosts().collect { posts ->
                _posts.value = posts
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

    /**
     * Get user profile picture from Firestore by userId
     */
    fun getUserProfilePicture(userId: String, callback: (String?) -> Unit) {
        if (userId.isBlank()) {
            callback(null)
            return
        }

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val profileImageUrl = document.getString("profilePictureUrl")
                    callback(profileImageUrl)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
