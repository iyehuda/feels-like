package com.iyehuda.feelslike.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iyehuda.feelslike.data.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // LiveData for the list of posts
    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error if needed
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val postsList = it.toObjects(Post::class.java)
                    _posts.value = postsList
                }
            }
    }
}