package com.alfianyulianto.flippoview

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout



class FlippoView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val TAG = this.javaClass.simpleName
    private val idAnimRightIn = R.animator.flip_right_in
    private val idAnimRightOut = R.animator.flip_right_out
    private val idAnimLeftIn = R.animator.flip_left_in
    private val idAnimLeftOut = R.animator.flip_left_out
    private val DEFAULT_FLIP_DURATION = 800

    private lateinit var animSetOut : AnimatorSet
    private lateinit var animSetIn : AnimatorSet

    private var cardFrontView : View? = null
    private var cardBackView : View? = null

    private var sideState = SideState.FRONT_SIDE
    private var onFlipListener : OnFlipAnimationListener? = null

    private var flipDuration = DEFAULT_FLIP_DURATION
    private var flipEnabled = true
    private var flipCloseReverse = false

    val LEFT_SIDE = resources.getString(R.string.left_side)
    val RIGHT_SIDE = resources.getString(R.string.right_side)

    private var flipTypeFrom = LEFT_SIDE

    enum class SideState {
        FRONT_SIDE, BACK_SIDE
    }


    init {

        val attrArray = context.obtainStyledAttributes(attrs, R.styleable.FlippoViewStyleable)

        val n = attrArray.indexCount
        for (i in 0..n) {
            val attr = attrArray.getIndex(i)
            when (attr) {
                R.styleable.FlippoViewStyleable_flipFrom -> flipTypeFrom = attrArray.getString(attr)
                R.styleable.FlippoViewStyleable_flipDuration -> flipDuration = attrArray.getInt(attr, DEFAULT_FLIP_DURATION)
                R.styleable.FlippoViewStyleable_flipEnabled -> flipEnabled = attrArray.getBoolean(attr, true)
                R.styleable.FlippoViewStyleable_closeReverse -> flipCloseReverse = attrArray.getBoolean(attr, false)

            }
        }

        attrArray.recycle()

        loadAnimations()
    }


    override fun onFinishInflate() {
        super.onFinishInflate()

        if (childCount > 2) {
            throw IllegalStateException("FlippoView can only has 2 child views")
        }

        setViews()
        setCameraDistance()
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (childCount == 2) {
            throw IllegalStateException("FlippoView can only has 2 child views")
        }
        super.addView(child, index, params)

        setViews()
        setCameraDistance()
    }

    override fun removeView(view: View?) {
        super.removeView(view)

        setViews()
    }

    override fun removeAllViewsInLayout() {
        super.removeAllViewsInLayout()

        sideState = SideState.FRONT_SIDE
        setViews()
    }

    fun setViews() {

        cardFrontView = null
        cardBackView = null

        val childs = childCount
        if (childs < 1)
            return

        if (childs < 2) {
            sideState = SideState.FRONT_SIDE
            cardFrontView = getChildAt(0)
        } else if (childs == 2) {
            cardFrontView = getChildAt(1)
            cardBackView = getChildAt(0)
            cardBackView?.visibility = View.GONE
        }

    }

    private fun setCameraDistance() {
        val distance = 4000
        val scale = resources.displayMetrics.density * distance
        Log.d("scale", scale.toString())
        cardFrontView?.setCameraDistance(scale)
        cardBackView?.setCameraDistance(scale)
    }


    private fun loadAnimations() {

        if (flipTypeFrom == RIGHT_SIDE) {
            animSetOut = AnimatorInflater.loadAnimator(context, idAnimRightOut) as AnimatorSet
            animSetIn = AnimatorInflater.loadAnimator(context, idAnimRightIn) as AnimatorSet
        } else {
            animSetOut = AnimatorInflater.loadAnimator(context, idAnimLeftOut) as AnimatorSet
            animSetIn = AnimatorInflater.loadAnimator(context, idAnimLeftIn) as AnimatorSet
        }

        animSetOut.removeAllListeners()

        animSetOut.addListener( object  : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                Log.d("side_state", sideState.name)
                if (sideState == SideState.FRONT_SIDE) {
                    Log.d("side_state", "back gone")
                    cardBackView?.visibility = View.GONE
                    cardFrontView?.visibility = View.VISIBLE
                    onFlipListener?.onFlipDone(this@FlippoView, SideState.FRONT_SIDE)

                    if (flipCloseReverse)
                        switchFlip()
                } else {
                    Log.d("side_state", "front gone")
                    cardFrontView?.visibility = View.GONE
                    cardBackView?.visibility = View.VISIBLE
                    onFlipListener?.onFlipDone(this@FlippoView, SideState.BACK_SIDE)

                    if (flipCloseReverse)
                        switchFlip()
                }
            }
        })

        setFlipDuration(flipDuration)
    }

    private fun switchFlip() {
        if (flipTypeFrom == RIGHT_SIDE) {
            flipTypeFrom = LEFT_SIDE
        } else {
            flipTypeFrom = RIGHT_SIDE
        }

        loadAnimations()
    }

    fun startFlip() {
        if (!flipEnabled || childCount < 2) return

        if (animSetIn.isRunning || animSetOut.isRunning) return

        cardFrontView?.visibility = View.VISIBLE
        cardBackView?.visibility = View.VISIBLE
        Log.d("side_state_start", sideState.name)

        if (sideState == SideState.FRONT_SIDE) {
            animSetOut.setTarget(cardFrontView)
            animSetIn.setTarget(cardBackView)
            animSetOut.start()
            animSetIn.start()
            sideState = SideState.BACK_SIDE
        } else {
            animSetOut.setTarget(cardBackView)
            animSetIn.setTarget(cardFrontView)
            animSetOut.start()
            animSetIn.start()
            sideState = SideState.FRONT_SIDE
        }
    }

    fun startFlip(animate : Boolean) {
        if (!flipEnabled || childCount < 2) return

        if (!animate) {
            animSetOut.setDuration(0)
            animSetIn
            animSetIn.setDuration(0)
            val oldFlipEnabled = flipEnabled
            flipEnabled = true

            startFlip()

            animSetOut.setDuration(flipDuration.toLong())
            animSetIn.setDuration(flipDuration.toLong())
            flipEnabled = oldFlipEnabled
        } else {
            startFlip()
        }
    }

    fun getCurrentSideState() : SideState {
        return sideState
    }

    fun isFrontSide() : Boolean {
        return sideState == SideState.FRONT_SIDE
    }

    fun isBackSide() : Boolean {
        return sideState == SideState.BACK_SIDE
    }

    fun setFlipDuration(flipDuration: Int) {
        this.flipDuration = flipDuration
        animSetOut.getChildAnimations().get(0).setDuration(flipDuration.toLong())
        animSetOut.getChildAnimations().get(1).setStartDelay(flipDuration.toLong() / 2)

        animSetIn.getChildAnimations().get(1).setDuration(flipDuration.toLong())
        animSetIn.getChildAnimations().get(2).setStartDelay(flipDuration.toLong() / 2)

    }

    fun setOnFlipAnimationListener(onFlipAnimationListener: OnFlipAnimationListener) {
        this.onFlipListener = onFlipAnimationListener
    }

    interface OnFlipAnimationListener {
        fun onFlipDone(flippoView: FlippoView, sideState: SideState)
    }
}