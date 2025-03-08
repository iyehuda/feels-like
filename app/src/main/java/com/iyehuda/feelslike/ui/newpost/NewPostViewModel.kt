package com.iyehuda.feelslike.ui.newpost

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iyehuda.feelslike.data.model.Post

class NewPostViewModel : ViewModel() {

    // LiveData to hold the selected image URI
    private val _postImageUri = MutableLiveData<Uri?>()
    val postImageUri: LiveData<Uri?> get() = _postImageUri

    // Call this from the fragment when a new image is picked
    fun setPostImage(uri: Uri) {
        _postImageUri.value = uri
    }

    // A mock upload function – later connect this to your repository
    fun uploadPost(text: String) {
        // Here, you can use _postImageUri.value (if not null) to upload the image along with the post text.
        // For now, simply print or log the values.
        println("Uploading post: $text, with image: ${_postImageUri.value}")
    }
}