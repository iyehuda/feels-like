package com.iyehuda.feelslike.ui.editpost

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iyehuda.feelslike.data.local.dao.PostDao
import com.iyehuda.feelslike.data.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val postDao: PostDao
) : ViewModel() {
    
    private val _post = MutableLiveData<Post>()
    val post: LiveData<Post> = _post

    fun loadPost(postId: String) {
        viewModelScope.launch {
            postDao.getPostById(postId)?.let { postEntity ->
                _post.value = postEntity.toPost()
            }
        }
    }
} 