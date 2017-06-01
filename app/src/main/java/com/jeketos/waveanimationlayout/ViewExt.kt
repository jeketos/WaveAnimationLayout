package com.jeketos.waveanimationlayout

import android.os.Build
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.ViewTreeObserver

fun View.onLaidOut(action: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (ViewCompat.isLaidOut(this@onLaidOut)) {
                viewTreeObserver.removeOnGlobalOnLayoutListener(this)
                action()
            }
        }
    })
}

fun ViewTreeObserver.removeOnGlobalOnLayoutListener(victim: ViewTreeObserver.OnGlobalLayoutListener) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        removeOnGlobalLayoutListener(victim)
    } else {
        @Suppress("DEPRECATION")
        removeGlobalOnLayoutListener(victim)
    }
}