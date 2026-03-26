package com.example.calculator

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import java.util.*

class crDateOverlayDrawable(
    private val baseDrawable: Drawable,
    private val date: String
) : Drawable() {

    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#FF6200EE")
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas) {
        // Draw the base calendar image
        baseDrawable.draw(canvas)

        val width = bounds.width()
        val height = bounds.height()

        // Draw a circular background for the date at bottom right
        val circleRadius = width * 0.15f
        val circleCenterX = width - circleRadius - width * 0.05f
        val circleCenterY = height - circleRadius - height * 0.05f

        // Draw semi-transparent background circle
        bgPaint.apply {
            color = Color.parseColor("#FF6200EE")
            alpha = 220
        }
        canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, bgPaint)

        // Draw date text in the circle
        paint.apply {
            textSize = circleRadius * 1.3f
            color = Color.WHITE
        }
        val fm = paint.fontMetrics
        val textY = circleCenterY - (fm.descent + fm.ascent) / 2
        canvas.drawText(date, circleCenterX, textY, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = baseDrawable.intrinsicWidth

    override fun getIntrinsicHeight(): Int = baseDrawable.intrinsicHeight
}
