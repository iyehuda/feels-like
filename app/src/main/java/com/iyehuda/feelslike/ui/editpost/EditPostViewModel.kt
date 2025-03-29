package com.iyehuda.feelslike.ui.editpost

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyehuda.feelslike.data.local.dao.PostDao
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.data.repository.PostRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val postDao: PostDao,
    private val postRepository: PostRepository
) : ViewModel() {
    
    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post
    
    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun loadPost(postId: String) {
        viewModelScope.launch {
            postDao.getPostById(postId)?.let { postEntity ->
                _post.value = postEntity.toPost()
            }
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> = 
        postRepository.deletePost(postId)

    fun setNewImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    suspend fun updatePost(postId: String, newDescription: String): Result<Unit> =
        postRepository.updatePost(postId, newDescription, _selectedImageUri.value)
} 