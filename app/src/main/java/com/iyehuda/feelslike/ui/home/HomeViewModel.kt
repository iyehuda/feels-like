package com.iyehuda.feelslike.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.iyehuda.feelslike.data.model.Post

class HomeViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        firestore.collection("posts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Optionally log or handle the error
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Map each document to a Post object. Ensure the field names match!
                    val postsList = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                    _posts.value = postsList
                }
            }
    }
}