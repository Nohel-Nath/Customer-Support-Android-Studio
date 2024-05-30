package com.example.customersupport

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.widget.AppCompatImageView

class TouchImageView(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private val matrix = Matrix()
    private var mode = NONE
    private val last = PointF()
    private val start = PointF()
    private var minScale = 1f
    private var maxScale = 3f
    private val m = FloatArray(9)
    private var viewWidth = 0
    private var viewHeight = 0
    private var saveScale = 1f
    private var origWidth = 0f
    private var origHeight = 0f
    private var oldMeasuredWidth = 0
    private var oldMeasuredHeight = 0

    private val mScaleDetector: ScaleGestureDetector
    private val mGestureDetector: GestureDetector

    init {
        super.setClickable(true)
        mGestureDetector = GestureDetector(context, this)
        mGestureDetector.setOnDoubleTapListener(this)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        imageMatrix = matrix
        scaleType = ImageView.ScaleType.MATRIX

        setOnTouchListener { _, event ->
            mScaleDetector.onTouchEvent(event)
            mGestureDetector.onTouchEvent(event)
            val curr = PointF(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    start.set(last)
                    mode = DRAG
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mode == DRAG) {
                        val deltaX = curr.x - last.x
                        val deltaY = curr.y - last.y
                        val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(), origWidth * saveScale)
                        val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(), origHeight * saveScale)
                        matrix.postTranslate(fixTransX, fixTransY)
                        fixTrans()
                        last.set(curr.x, curr.y)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = (curr.x - start.x).toInt()
                    val yDiff = (curr.y - start.y).toInt()
                    if (xDiff < CLICK && yDiff < CLICK) performClick()
                }
                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            imageMatrix = matrix
            invalidate()
            true
        }
    }

    fun setMaxZoom(x: Float) {
        maxScale = x
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean = false

    override fun onDoubleTap(e: MotionEvent): Boolean {
        Log.i("MAIN_TAG", "Double tap detected")
        val origScale = saveScale
        val mScaleFactor: Float = if (saveScale == maxScale) {
            saveScale = minScale
            minScale / origScale
        } else {
            saveScale = maxScale
            maxScale / origScale
        }
        matrix.postScale(mScaleFactor, mScaleFactor, (viewWidth / 2).toFloat(), (viewHeight / 2).toFloat())
        fixTrans()
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean = false

    override fun onDown(e: MotionEvent): Boolean = false

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean =
        false


    override fun onLongPress(e: MotionEvent) {}
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean =
        false

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val origScale = saveScale
            saveScale *= mScaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / origScale
            }
            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, (viewWidth / 2).toFloat(), (viewHeight / 2).toFloat())
            else
                matrix.postScale(mScaleFactor, mScaleFactor, detector.focusX, detector.focusY)
            fixTrans()
            return true
        }
    }

    private fun fixTrans() {
        matrix.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]
        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), origWidth * saveScale)
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), origHeight * saveScale)
        if (fixTransX != 0f || fixTransY != 0f) matrix.postTranslate(fixTransX, fixTransY)
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float
        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }
        return when {
            trans < minTrans -> -trans + minTrans
            trans > maxTrans -> -trans + maxTrans
            else -> 0f
        }
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        return if (contentSize <= viewSize) 0f else delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)

        if (oldMeasuredHeight == viewHeight && oldMeasuredWidth == viewWidth || viewWidth == 0 || viewHeight == 0) return
        oldMeasuredHeight = viewHeight
        oldMeasuredWidth = viewWidth

        if (saveScale == 1f) {
            val scale: Float

            val drawable: Drawable? = drawable
            if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) return
            val bmWidth = drawable.intrinsicWidth
            val bmHeight = drawable.intrinsicHeight

            Log.d("bmSize", "bmWidth: $bmWidth bmHeight : $bmHeight")

            val scaleX = viewWidth.toFloat() / bmWidth.toFloat()
            val scaleY = viewHeight.toFloat() / bmHeight.toFloat()
            scale = Math.min(scaleX, scaleY)
            matrix.setScale(scale, scale)

            val redundantYSpace = viewHeight.toFloat() - scale * bmHeight.toFloat()
            val redundantXSpace = viewWidth.toFloat() - scale * bmWidth.toFloat()
            matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2)

            origWidth = viewWidth - 2 * redundantXSpace / 2
            origHeight = viewHeight - 2 * redundantYSpace / 2
            imageMatrix = matrix
        }
        fixTrans()
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
        private const val CLICK = 3
    }
}