package com.jeketos.waveanimationlayout

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.annotation.IdRes
import android.util.AttributeSet
import android.util.Property
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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
    private var animDuration: Long = 0
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
        startX = array.getDimension(R.styleable.WaveAnimateRelativeLayout_startX, -1f)
        startY = array.getDimension(R.styleable.WaveAnimateRelativeLayout_startY, -1f)
        relativeTo = array.getResourceId(R.styleable.WaveAnimateRelativeLayout_relativeTo, -1)
        startSize = array.getDimension(R.styleable.WaveAnimateRelativeLayout_startSize, 1f)
        startColor = array.getColor(R.styleable.WaveAnimateRelativeLayout_startColor, Color.argb(128,255,255,255))
        endColor = changeColorAlpha(startColor, 0)
        animDuration = array.getInt(R.styleable.WaveAnimateRelativeLayout_animDuration, DEFAULT_ANIM_DURATION).toLong()
        wavesCount = array.getInt(R.styleable.WaveAnimateRelativeLayout_wavesCount, 3)
        wavesCount = array.getInt(R.styleable.WaveAnimateRelativeLayout_wavesCount, 3)
        array.recycle()
    }

    fun startAnim(){
        onLaidOut {
            val waveIndex = wavesCount - 1
            val delay = animDuration / wavesCount
            calculateZoom()
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


    private fun startViewWaveAnimation(startDelay: Long){
        val view = createView()
        addView(view, 0)
        animatedViews.add(view)
        val scaleX = createScaleAnimator(view, View.SCALE_X, zoom)
        val scaleY = createScaleAnimator(view, View.SCALE_Y, zoom)
        val colorAnim = createColorAnimator(view)
        val animSet = AnimatorSet()
        animSet.startDelay = startDelay
        animSet.playTogether(scaleX, scaleY, colorAnim)
        animSet.start()
    }

    private fun createView(): View {
        val view = View(context)
        val layoutParams = LayoutParams(startSize.toInt(), startSize.toInt())
        if (relativeTo >= 0) {
            val relatedView = findViewById(relativeTo)
            layoutParams.leftMargin = (relatedView.x + relatedView.width / 2 - startSize / 2).toInt()
            layoutParams.topMargin = (relatedView.y + relatedView.height / 2 - startSize / 2).toInt()
        } else {
            if(startX < 0 && startY < 0){
                layoutParams.leftMargin = (this.width / 2 - startSize / 2).toInt()
                layoutParams.topMargin = (this.height / 2 - startSize / 2).toInt()
            } else {
               layoutParams.leftMargin = if(startX < 0) 0 else startX.toInt()
               layoutParams.topMargin  = if(startY < 0) 0 else startY.toInt()
            }
        }
        view.layoutParams = layoutParams
        view.background = createGradientDrawable()
        return view
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

    private fun createScaleAnimator(view: View, property: Property<View, Float>, zoom: Float): ObjectAnimator{
        return ObjectAnimator.ofFloat(view, property, startSize, zoom).apply {
            duration = animDuration
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
        }
    }

    private fun createColorAnimator(view: View): ValueAnimator{
        val colorAnim = ObjectAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
        colorAnim.duration = animDuration
        colorAnim.interpolator = AccelerateDecelerateInterpolator()
        colorAnim.repeatCount = ObjectAnimator.INFINITE
        colorAnim.repeatMode = ObjectAnimator.RESTART
        colorAnim.addUpdateListener {
            (view.background as GradientDrawable).colors = intArrayOf(getAnimStartColor(it.animatedFraction), it.animatedValue as Int)
        }
        return colorAnim
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
        val valueToDivide: Float
        if(relativeTo > 0) {
            val relatedView = findViewById(relativeTo)
            val viewCenterX = relatedView.x + relatedView.width / 2
            val viewCenterY = relatedView.y + relatedView.height / 2
            val array = arrayOf(viewCenterX, viewCenterY, width - viewCenterX, height - viewCenterY)
            valueToDivide = array.max()!!
        } else {
            if(startX < 0 && startY < 0){
                valueToDivide = Math.max(width / 2f, height / 2f)
            } else {
                val array = arrayOf(startX, startY, width - startX, height - startY)
                valueToDivide = array.max()!!
            }
        }
        zoom = valueToDivide / (startSize / 2)
    }

}