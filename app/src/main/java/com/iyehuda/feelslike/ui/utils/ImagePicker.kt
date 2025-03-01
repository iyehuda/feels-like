package com.iyehuda.feelslike.ui.utils

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class ImagePicker private constructor(private val launcher: ActivityResultLauncher<PickVisualMediaRequest>) {
    companion object {
        fun create(
            fragment: Fragment,
            callback: (Uri) -> Unit,
        ): ImagePicker {
            return ImagePicker(fragment.registerForActivityResult(
                ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                uri?.let { callback(it) }
            })
        }
    }

    fun pickSingleImage() {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}
