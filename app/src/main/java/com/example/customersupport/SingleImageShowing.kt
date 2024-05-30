package com.example.customersupport


import android.annotation.SuppressLint
import android.view.Window
import android.view.WindowManager
import android.graphics.PointF
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.customersupport.databinding.SingleImageBinding
import kotlin.math.atan2
import kotlin.math.sqrt

class SingleImageShowing : AppCompatActivity() {
    private lateinit var binding: SingleImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SingleImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val imageName = intent.getStringExtra("IMAGE_NAME")
        binding.imageName.text = imageName ?: "Unknown"
        imageUrl?.let {
            Glide.with(this).load(it).into(binding.singleImageView)
        }

        binding.singleImageViewCancel.setSafeOnClickListener {
            // Close the activity when cancel is clicked
            onBackPressedDispatcher.onBackPressed()
        }
    }
}


/*
private lateinit var binding: SingleImageBinding
    private var lastEvent: FloatArray? = null
    private var d = 0f
    private var newRot = 0f
    private var isZoomAndRotate = false
    private var isOutSide = false

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var xCoOrdinate = 0f
    private var yCoOrdinate = 0f
    private lateinit var gestureDetector: GestureDetector
    @SuppressLint("ClickableViewAccessibility")
    requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // Reset image to its original position
                binding.singleImageView.animate().x(0f).y(0f).scaleX(1f).scaleY(1f).rotation(0f)
                    .setDuration(300).start()
                return true
            }
        })

        binding.singleImageView.setOnTouchListener { v, event ->
            val view = v as ImageView
            view.bringToFront()
            viewTransformation(view, event)
            gestureDetector.onTouchEvent(event) // Pass the event to GestureDetector
            true
        }

        private fun viewTransformation(view: View, event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                xCoOrdinate = view.x - event.rawX
                yCoOrdinate = view.y - event.rawY
                start.set(event.x, event.y)
                isOutSide = false
                mode = DRAG
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    midPoint(mid, event)
                    mode = ZOOM
                }

                lastEvent = FloatArray(4).apply {
                    this[0] = event.getX(0)
                    this[1] = event.getX(1)
                    this[2] = event.getY(0)
                    this[3] = event.getY(1)
                }
                d = rotation(event)
            }

            MotionEvent.ACTION_UP -> {
                isZoomAndRotate = false
                if (mode == DRAG) {
                    val x = event.x
                    val y = event.y
                }
            }

            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false
                        view.animate()
                            .x(event.rawX + xCoOrdinate)
                            .y(event.rawY + yCoOrdinate)
                            .setDuration(0)
                            .start()
                    }
                    if (mode == ZOOM && event.pointerCount == 2) {
                        val newDist = spacing(event)
                        if (newDist > 10f) {
                            val scale = newDist / oldDist * view.scaleX
                            view.scaleX = scale
                            view.scaleY = scale
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event)
                            view.rotation = view.rotation + (newRot - d)
                        }
                    }
                }
            }
        }
    }

    private fun rotation(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1)).toDouble()
        val deltaY = (event.getY(0) - event.getY(1)).toDouble()
        val radians = atan2(deltaY, deltaX)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

 */