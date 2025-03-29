package com.iyehuda.feelslike.ui.newpost

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewPostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    // LiveData to hold the selected image URI
    private val _postImageUri = MutableLiveData<Uri?>()
    val postImageUri: LiveData<Uri?> get() = _postImageUri

    // Call this from the fragment when a new image is picked
    fun setPostImage(uri: Uri) {
        _postImageUri.value = uri
    }

    // Uploads the post to Firebase
    fun uploadPost(text: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError(Exception("User not logged in"))
            return
        }

        viewModelScope.launch {
            try {
                val postId = UUID.randomUUID().toString()
                val userId = user.uid
                val username = user.displayName ?: "Anonymous"
                val weather = "Sunny" // Replace with dynamic weather info
                val temperature = 30.0 // Replace with dynamic temperature
                val location = LatLng(1.0, 1.0) // Replace with dynamic location

                val imageUri = _postImageUri.value
                val imageUrl = if (imageUri != null) {
                    uploadImage(postId, imageUri)
                } else null

                val post = Post(
                    id = postId,
                    username = username,
                    weather = weather,
                    temperature = temperature,
                    description = text,
                    imageUrl = imageUrl,
                    userId = userId,
                    latitude = location.latitude,
                    longitude = location.longitude
                )

                postRepository.savePost(post)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private suspend fun uploadImage(postId: String, imageUri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference.child("posts/$postId.jpg")
        val uploadTask = storageRef.putFile(imageUri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }
}