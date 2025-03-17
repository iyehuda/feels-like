package com.iyehuda.feelslike.ui.myprofile

import com.iyehuda.feelslike.data.auth.AuthRepository
import com.iyehuda.feelslike.ui.base.BaseAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iyehuda.feelslike.data.model.Post

@HiltViewModel
class MyProfileViewModel @Inject constructor(authRepository: AuthRepository) :
    BaseAuthViewModel(authRepository) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> get() = _userPosts

    init {
        fetchUserPosts()
    }

    private fun fetchUserPosts() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val postsList = it.toObjects(Post::class.java)
                    _userPosts.value = postsList
                }
            }
    }
}
