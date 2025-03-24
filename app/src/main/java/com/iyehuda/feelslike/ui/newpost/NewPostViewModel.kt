package com.iyehuda.feelslike.ui.newpost

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.iyehuda.feelslike.data.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class NewPostViewModel @Inject constructor() : ViewModel() {


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

        // Generate a unique ID for the post
        val postId = UUID.randomUUID().toString()
        val userId = user.uid
        val username = user.displayName ?: "Anonymous"
        val weather = "Sunny" // Replace with dynamic weather info
        val temperature = 30.0 // Replace with dynamic temperature
        val location = LatLng(1.0,1.0) // Replace with dynamic location (or pass as parameter)

        val imageUri = _postImageUri.value

        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("posts/$postId.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val post = Post(
                            id = postId,
                            username = username,
                            weather = weather,
                            temperature = temperature,
                            description = text,
                            imageUrl = uri.toString(),
                            latitude = location.latitude,
                            longitude = location.longitude,
                            userId = userId
                        )
                        savePostToFirestore(post, onSuccess, onError)
                    }.addOnFailureListener { e -> onError(e) }
                }
                .addOnFailureListener { e -> onError(e) }
        } else {
            val post = Post(
                id = postId,
                username = username,
                weather = weather,
                temperature = temperature,
                description = text,
                imageUrl = null,
                latitude = location.latitude,
                longitude = location.longitude,
                userId = userId
            )
            savePostToFirestore(post, onSuccess, onError)
        }
    }

    // Helper function to save the post document to Firestore
    private fun savePostToFirestore(
        post: Post,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance().collection("posts")
            .document(post.id)
            .set(post)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}