package com.iyehuda.feelslike.ui.editpost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyehuda.feelslike.data.local.dao.PostDao
import com.iyehuda.feelslike.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val postDao: PostDao
) : ViewModel() {
    
    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun loadPost(postId: String) {
        viewModelScope.launch {
            postDao.getPostById(postId)?.let { postEntity ->
                _post.value = postEntity.toPost()
            }
        }
    }

    // Update delete function to handle both Room and Firestore
    suspend fun deletePost(postId: String): Result<Unit> = runCatching {
        // Delete from Firestore
        firestore.collection("posts").document(postId).delete().await()
        
        // If Firestore deletion succeeds, delete from Room
        postDao.deletePost(postId)
        
        // Delete the post image if it exists
        _post.value?.imageUrl?.let { imageUrl ->
            if (imageUrl.isNotEmpty()) {
                storage.getReferenceFromUrl(imageUrl).delete().await()
            }
        }
    }
} 