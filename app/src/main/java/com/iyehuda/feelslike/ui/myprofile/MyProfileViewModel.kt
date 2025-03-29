package com.iyehuda.feelslike.ui.myprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iyehuda.feelslike.data.auth.AuthRepository
import com.iyehuda.feelslike.data.model.Post
import com.iyehuda.feelslike.data.repository.PostRepository
import com.iyehuda.feelslike.ui.base.BaseAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyProfileViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val postRepository: PostRepository
) : BaseAuthViewModel(authRepository) {

    private val TAG = "MyProfileViewModel"

    // LiveData for the list of user posts
    private val _userPosts = MutableLiveData<List<Post>>()
    val userPosts: LiveData<List<Post>> = _userPosts

    init {
        fetchUserPosts()
    }

    private fun fetchUserPosts() {
        userDetails.value?.let { user ->
            viewModelScope.launch {
                postRepository.getPostsByUsername(user.displayName)
                    .collect { posts ->
                        _userPosts.value = posts
                    }
            }
        }
    }
}
