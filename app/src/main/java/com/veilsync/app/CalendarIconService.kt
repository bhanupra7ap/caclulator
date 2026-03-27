package com.veilsync.app

import android.app.Service
import android.content.Intent
import android.graphics.*
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CalendarIconService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): CalendarIconService = this@CalendarIconService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Generate and update the calendar icon
        generateAndSaveCalendarIcon()
        return START_STICKY
    }

    fun generateAndSaveCalendarIcon() {
        try {
            // Get current date
            val calendar = Calendar.getInstance()
            val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
            val currentDay = dayFormat.format(calendar.time)

            Log.d("CalendarIconService", "Generating calendar icon for day: $currentDay")

            // Load the base calendar drawable
            val calendarDrawable = resources.getDrawable(
                resources.getIdentifier("ic_launcher_calendar", "drawable", packageName),
                null
            )

            // Convert to bitmap
            val baseBitmap = calendarDrawable.toBitmap(192, 192, Bitmap.Config.ARGB_8888)

            // Create composite with date overlay
            val compositeBitmap = createIconWithDateOverlay(baseBitmap, currentDay)

            // Save the composite bitmap
            saveBitmapToCache(compositeBitmap)

            Log.d("CalendarIconService", "Calendar icon generated and saved successfully")
        } catch (e: Exception) {
            Log.e("CalendarIconService", "Error generating calendar icon", e)
        }
    }

    private fun createIconWithDateOverlay(baseIcon: Bitmap, dateText: String): Bitmap {
        val width = baseIcon.width
        val height = baseIcon.height

        // Create a new bitmap for the composite
        val composite = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(composite)

        // Draw the base calendar image
        canvas.drawBitmap(baseIcon, 0f, 0f, null)

        // Create paint for the date badge circle
        val badgePaint = Paint().apply {
            color = Color.parseColor("#FF6200EE") // Material Design Primary Purple
            isAntiAlias = true
            setShadowLayer(8f, 0f, 4f, Color.parseColor("#4D000000"))
        }

        // Create paint for the date text
        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = (width * 0.40f)
        }

        // Calculate badge position (bottom-right corner like Android notification badges)
        val badgeRadius = (width * 0.32f)
        val badgeCenterX = (width - badgeRadius - width * 0.02f)
        val badgeCenterY = (height - badgeRadius - height * 0.02f)

        // Draw the badge circle
        canvas.drawCircle(badgeCenterX, badgeCenterY, badgeRadius, badgePaint)

        // Draw the date text inside the circle
        val fontMetrics = textPaint.fontMetrics
        val textY = badgeCenterY - (fontMetrics.descent + fontMetrics.ascent) / 2
        canvas.drawText(dateText, badgeCenterX, textY, textPaint)

        return composite
    }

    private fun saveBitmapToCache(bitmap: Bitmap) {
        try {
            val cacheDir = File(cacheDir, "calendar_icons")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val iconFile = File(cacheDir, "calendar_with_date.png")
            FileOutputStream(iconFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                output.flush()
            }

            Log.d("CalendarIconService", "Icon saved to: ${iconFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("CalendarIconService", "Error saving bitmap", e)
        }
    }
}

