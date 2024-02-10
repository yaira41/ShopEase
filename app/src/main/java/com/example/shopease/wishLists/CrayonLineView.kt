package com.example.shopease.wishLists

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CrayonLineView : View {

    private val paint: Paint = Paint()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        paint.strokeWidth = 5f // Adjust the line thickness as needed
        paint.color = resources.getColor(android.R.color.holo_green_dark) // Set line color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw a line from left to right in the middle of the view
        val y = height / 2f
        canvas.drawLine(0f, y, width.toFloat(), y, paint)
    }
}
