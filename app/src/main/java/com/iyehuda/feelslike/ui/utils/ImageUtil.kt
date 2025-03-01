package com.iyehuda.feelslike.ui.utils

import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class ImageUtil {
    companion object {
        fun loadImage(fragment: Fragment, view: ImageView, uri: Uri, circle: Boolean = false) {
            val placeholder = if (circle) ImagePlaceholder.circle() else ImagePlaceholder.create()
            var loader = Glide.with(fragment).load(uri).placeholder(placeholder)

            if (circle) {
                loader = loader.circleCrop()
            }

            loader.into(view)
        }
    }
}
