package com.iyehuda.feelslike.ui.myprofile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iyehuda.feelslike.data.auth.AuthRepository
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.ui.base.BaseAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(authRepository: AuthRepository) :
    BaseAuthViewModel(authRepository) {
    
    private val TAG = "MyProfileViewModel"
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    // LiveData for the list of user posts
    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> get() = _userPosts
    
    init {
        fetchUserPosts()
    }
    
    private fun fetchUserPosts() {
        userDetails.value?.let { user ->
            firestore.collection("posts")
                .whereEqualTo("username", user.displayName)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error fetching user posts: ${error.message}", error)
                        return@addSnapshotListener
                    }
                    snapshot?.let {
                        val postsList = it.toObjects(Post::class.java)
                        _userPosts.value = postsList
                        Log.d(TAG, "Fetched ${postsList.size} posts for user ${user.displayName}")
                    }
                }
        }
    }
}
