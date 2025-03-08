package com.iyehuda.feelslike.ui.newpost

import androidx.lifecycle.ViewModel

class NewPostViewModel : ViewModel() {

    fun uploadPost(text: String) {
        // TODO: Implement actual upload logic
        // For now, just print or log it
        // e.g., call repository to save the post
        println("Uploading post: $text")
    }
}