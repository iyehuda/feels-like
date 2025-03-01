package com.iyehuda.feelslike.ui.utils

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable

class CircleShapeDrawable(wrappedDrawable: Drawable) : DrawableWrapper(wrappedDrawable) {
    override fun draw(canvas: Canvas) {
        val save = canvas.save()

        val path = Path()
        val bounds = bounds
        val centerX = bounds.width() / 2f
        val centerY = bounds.height() / 2f
        val radius = kotlin.math.min(centerX, centerY)

        path.addCircle(centerX, centerY, radius, Path.Direction.CW)

        canvas.clipPath(path)

        super.draw(canvas)

        canvas.restoreToCount(save)
    }
}

class ImagePlaceholder {
    companion object {
        fun create(): Drawable = ShimmerDrawable().apply {
            setShimmer(
                Shimmer.AlphaHighlightBuilder().setDuration(1000).setBaseAlpha(0.7f)
                    .setHighlightAlpha(0.9f).setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                    .setAutoStart(true).build()
            )
        }

        fun circle(): Drawable = CircleShapeDrawable(create())
    }
}
