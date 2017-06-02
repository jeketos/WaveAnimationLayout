package com.jeketos.waveanimationlayout

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.support.annotation.IdRes
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.RelativeLayout

private val DEFAULT_ANIM_DURATION = 3000
private val ALPHA_START_FRACTION = 0.5
private val ALPHA_ANIM_SPEED = 1 / ALPHA_START_FRACTION
class WaveAnimateRelativeLayout: RelativeLayout {

    private var startX: Float = 0f
    private var startY: Float = 0f
    private var startSize: Float = 1f
    private var startColor: Int = 0
    private var endColor: Int = 0
    private var animDuration: Int = 0
    private var wavesCount: Int = 0
    private var zoom: Float = 0f
    private var animatedViews: ArrayList<View> = ArrayList()
    @IdRes private var relativeTo: Int = 0

    constructor(context: Context) : super(context, null)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, 0){
        getAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle){
        getAttributes(context, attrs)
    }

    private fun getAttributes(context: Context, attrs: AttributeSet) {
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.WaveAnimateRelativeLayout, 0, 0)
        startX = array.getDimension(R.styleable.WaveAnimateRelativeLayout_startX, 0f)
        startY = array.getDimension(R.styleable.WaveAnimateRelativeLayout_startY, 0f)
        startSize = array.getDimension(R.styleable.WaveAnimateRelativeLayout_startSize, 1f)
        startColor = array.getColor(R.styleable.WaveAnimateRelativeLayout_startColor, Color.argb(128,255,255,255))
        endColor = changeColorAlpha(startColor, 0)
        animDuration = array.getInt(R.styleable.WaveAnimateRelativeLayout_animDuration, DEFAULT_ANIM_DURATION)
        wavesCount = array.getInt(R.styleable.WaveAnimateRelativeLayout_wavesCount, 3)
        wavesCount = array.getInt(R.styleable.WaveAnimateRelativeLayout_wavesCount, 3)
        relativeTo = array.getResourceId(R.styleable.WaveAnimateRelativeLayout_relativeTo, 0)
        array.recycle()
    }

    fun startAnim(){
        onLaidOut {
            val waveIndex = wavesCount - 1
            val delay = animDuration / wavesCount
            calculateZoom()
            Log.d("WaveAnimate","zoom - $zoom")
            (0..waveIndex).forEachIndexed { index, _ ->
                startViewWaveAnimation((waveIndex - index) * delay)
            }
        }
    }

    fun stopAnim(){
        animatedViews.listIterator().forEach {
            it.clearAnimation()
            removeView(it)
        }
        animatedViews.clear()
    }


    private fun startViewWaveAnimation(startDelay: Int){
        val view = View(context)
        val layoutParams = LayoutParams(startSize.toInt(), startSize.toInt())
        if(relativeTo != 0){
            val relatedView = findViewById(relativeTo)
            layoutParams.leftMargin = (relatedView.x + relatedView.width/2 - startSize/2).toInt()
            layoutParams.topMargin =  (relatedView.y + relatedView.height/2 - startSize/2).toInt()
        } else {
            layoutParams.leftMargin = (startX).toInt()
            layoutParams.topMargin = startY.toInt()
        }
        view.layoutParams = layoutParams
        view.background = createGradientDrawable()
        view.visibility = View.GONE
        addView(view, 0)
        animatedViews.add(view)
        startScaleAnimation(view, startDelay)
    }

    private fun createGradientDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            orientation = GradientDrawable.Orientation.TL_BR
            gradientType = GradientDrawable.RADIAL_GRADIENT
            cornerRadius = startSize
            gradientRadius = startSize/4
            colors = intArrayOf(startColor, startColor)
        }
    }

    private fun startScaleAnimation(view: View, delay: Int){
        Log.d("WaveAnimate","width - $width")
        val animation = ScaleAnimation(1f, zoom, 1f, zoom, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        animation.duration = animDuration.toLong()
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation) {
                view.startAnimation(animation)
            }

            override fun onAnimationStart(animation: Animation?) {
                startColorAnim(view)
            }

        })
        Handler().postDelayed({
            view.visibility = View.VISIBLE
            view.startAnimation(animation)
        }, delay.toLong())
    }



    private fun startColorAnim(view: View){
        val colorAnim = ObjectAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        colorAnim.duration = animDuration.toLong()
        colorAnim.interpolator = AccelerateDecelerateInterpolator()
        colorAnim.addUpdateListener {
            (view.background as GradientDrawable).colors = intArrayOf(getAnimStartColor(it.animatedFraction), it.animatedValue as Int)
        }
        colorAnim.start()
    }

    private fun getAnimStartColor(fraction: Float): Int{
        if(fraction > ALPHA_START_FRACTION){
            val colorAlpha = Color.alpha(startColor)
            val alpha = (colorAlpha - ((fraction - ALPHA_START_FRACTION) * ALPHA_ANIM_SPEED * colorAlpha)).toInt()
            return changeColorAlpha(startColor, alpha)
        }
        return startColor
    }

    private fun calculateZoom() {
        if(relativeTo > 0){
            val relatedView = findViewById(relativeTo)
            val x = (relatedView.x + relatedView.width / 2 - startSize / 2).toInt()
            if (width / 2 > x) {
                zoom = (width - x) / (startSize / 2)
            } else {
                zoom = (x + startSize) / (startSize / 2)
            }
        } else {
            if (width / 2 > startX) {
                zoom = (width - startX) / (startSize / 2)
            } else {
                zoom = (startX + startSize) / (startSize / 2)
            }
        }
    }

}