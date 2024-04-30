package com.example.shopease.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class StrikeThroughTextView : AppCompatTextView {

    private val strikeThroughPaint = Paint()
    private var strikeThroughEnabled = false
    private var strikeThroughEndPosition = 0f

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        strikeThroughPaint.color = currentTextColor
        strikeThroughPaint.strokeWidth = textSize / 12 // Adjust the line thickness as needed
        strikeThroughPaint.flags = Paint.STRIKE_THRU_TEXT_FLAG
    }

    fun setStrikeThroughTextFlag(enabled: Boolean) {
        if (enabled) {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    fun setStrikeThrough(enabled: Boolean, endPosition: Float = 0f) {
        strikeThroughEnabled = enabled
        if (enabled) {
            strikeThroughEndPosition = endPosition
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (strikeThroughEnabled) {
            val lineY = (height / 2).toFloat() + 2
            canvas.drawLine(0f, lineY, strikeThroughEndPosition, lineY, strikeThroughPaint)
        }
    }
}
