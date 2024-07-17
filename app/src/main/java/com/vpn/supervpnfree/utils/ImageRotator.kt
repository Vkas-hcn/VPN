package com.vpn.supervpnfree.utils

import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

class ImageRotator {

    private var isRotating = false
    private var rotateAnimation: RotateAnimation? = null

    fun startRotating(imageView: ImageView) {
        if (isRotating) return

        rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1000
            repeatCount = Animation.INFINITE
            repeatMode = Animation.RESTART
        }

        imageView.startAnimation(rotateAnimation)
        isRotating = true
    }

    fun stopRotating(imageView: ImageView) {
        if (!isRotating) return

        imageView.clearAnimation()
        isRotating = false
    }
}
