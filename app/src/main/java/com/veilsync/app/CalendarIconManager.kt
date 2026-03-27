package com.veilsync.app

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object CalendarIconManager {
    private const val CACHE_DIR = "icon_cache"
    private const val CALENDAR_ICON_FILE = "calendar_icon.png"
    private const val TAG = "CalendarIconManager"

    fun generateCalendarIconWithDate(context: Context): BitmapDrawable? {
        return try {
            // Get current date
            val calendar = Calendar.getInstance()
            val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
            val currentDay = dayFormat.format(calendar.time)

            // Load the calendar base image
            val calendarBitmap = loadCalendarImage(context)
            if (calendarBitmap == null) {
                Log.e(TAG, "Failed to load calendar image")
                return null
            }

            // Create a composite bitmap with date overlay
            val compositeBitmap = createCompositeIcon(calendarBitmap, currentDay)
            
            // Save to cache
            saveBitmapToCache(context, compositeBitmap)
            
            // Return as BitmapDrawable
            BitmapDrawable(context.resources, compositeBitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating calendar icon", e)
            null
        }
    }

    private fun loadCalendarImage(context: Context): Bitmap? {
        return try {
            // Try to load from drawable resources
            val drawable = context.resources.getDrawable(
                context.resources.getIdentifier(
                    "ic_launcher_calendar",
                    "drawable",
                    context.packageName
                ),
                null
            )
            drawableToBitmap(drawable)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading calendar image from drawable", e)
            // Try from raw assets
            try {
                val inputStream = context.assets.open("ic_launcher-calender.png")
                BitmapFactory.decodeStream(inputStream)
            } catch (e2: Exception) {
                Log.e(TAG, "Error loading from assets", e2)
                null
            }
        }
    }

    private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable?): Bitmap? {
        if (drawable == null) return null

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun createCompositeIcon(baseIcon: Bitmap, dateText: String): Bitmap {
        val width = baseIcon.width
        val height = baseIcon.height

        // Create new bitmap for composite
        val composite = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(composite)

        // Draw base icon
        canvas.drawBitmap(baseIcon, 0f, 0f, null)

        // Prepare paint for date circle and text
        val circlePaint = Paint().apply {
            color = Color.parseColor("#FF6200EE")
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = width * 0.35f
        }

        // Draw circle at bottom-right corner
        val circleRadius = width * 0.25f
        val circleCenterX = width - circleRadius - width * 0.05f
        val circleCenterY = height - circleRadius - height * 0.05f

        // Draw semi-transparent circle background
        canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, circlePaint)

        // Draw date text
        val fm = textPaint.fontMetrics
        val textY = circleCenterY - (fm.descent + fm.ascent) / 2
        canvas.drawText(dateText, circleCenterX, textY, textPaint)

        return composite
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap) {
        try {
            val cacheDir = File(context.cacheDir, CACHE_DIR)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val iconFile = File(cacheDir, CALENDAR_ICON_FILE)
            FileOutputStream(iconFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d(TAG, "Calendar icon saved to cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap to cache", e)
        }
    }

    fun getCachedIconBitmap(context: Context): Bitmap? {
        return try {
            val cacheDir = File(context.cacheDir, CACHE_DIR)
            val iconFile = File(cacheDir, CALENDAR_ICON_FILE)
            if (iconFile.exists()) {
                BitmapFactory.decodeFile(iconFile.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached icon", e)
            null
        }
    }
}

