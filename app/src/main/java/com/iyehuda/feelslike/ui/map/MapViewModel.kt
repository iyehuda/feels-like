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
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import com.google.firebase.storage.FirebaseStorage

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

    fun getDefaultLocation(): LatLng {
        return LatLng(32.0853, 34.7818) // Tel Aviv
    }

    fun calculateOffsetPosition(latLng: LatLng, index: Int, total: Int): LatLng {
        // Calculate a small offset based on the index
        // This creates a circular pattern around the original position
        val radius = 0.0001 // Approximately 10 meters
        val angle = (2 * Math.PI * index) / total
        val offsetLat = latLng.latitude + (radius * cos(angle))
        val offsetLng = latLng.longitude + (radius * sin(angle))
        return LatLng(offsetLat, offsetLng)
    }

    fun getUserProfilePicture(userId: String, callback: (String?) -> Unit) {
        if (userId.isBlank()) {
            callback(null)
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val avatarRef = storageRef.child("avatars/$userId")
        
        avatarRef.downloadUrl
            .addOnSuccessListener { uri ->
                callback(uri.toString())
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
