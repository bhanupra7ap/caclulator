package com.example.calculator

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.*
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun GenerateCalendarIconWithDate(context: Context) {
    try {
        // Get current date (day only)
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val currentDay = dayFormat.format(calendar.time)

        Log.d("IconGeneration", "Generating calendar icon with date: $currentDay")

        // Load calendar image from drawable
        val calendarIcon = try {
            context.resources.getDrawable(
                context.resources.getIdentifier(
                    "ic_launcher_calendar",
                    "drawable",
                    context.packageName
                ),
                null
            )
        } catch (e: Exception) {
            Log.e("IconGeneration", "Could not find ic_launcher_calendar drawable", e)
            return
        }

        // Convert drawable to bitmap (standard app icon size)
        val baseBitmap = calendarIcon.toBitmap(
            width = 192,
            height = 192,
            config = Bitmap.Config.ARGB_8888
        )

        // Create composite bitmap with date overlay
        val compositeBitmap = createCompositeIconBitmap(baseBitmap, currentDay)

        // Save the composite icon
        saveCompositeIconBitmap(context, compositeBitmap)
        
        // Update app shortcut with the new icon
        updateAppShortcutWithIcon(context, compositeBitmap)

        Log.d("IconGeneration", "Calendar icon successfully generated with date overlay")
    } catch (e: Exception) {
        Log.e("IconGeneration", "Error generating calendar icon", e)
    }
}

private fun updateAppShortcutWithIcon(context: Context, iconBitmap: Bitmap) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            
            if (shortcutManager != null) {
                // Create or update shortcut with the calendar icon
                val calendarShortcut = ShortcutInfo.Builder(context, "calendar_mode")
                    .setShortLabel("Calendar")
                    .setLongLabel("Calendar Mode")
                    .setIcon(Icon.createWithBitmap(iconBitmap))
                    .setIntent(
                        android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                            setClass(context, MainActivity::class.java)
                        }
                    )
                    .build()

                // Add dynamic shortcut
                try {
                    shortcutManager.addDynamicShortcuts(listOf(calendarShortcut))
                    Log.d("IconGeneration", "App shortcut updated with calendar icon")
                } catch (e: Exception) {
                    Log.e("IconGeneration", "Error adding shortcut", e)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("IconGeneration", "Failed to update app shortcut with icon", e)
    }
}

private fun createCompositeIconBitmap(baseIcon: Bitmap, dateText: String): Bitmap {
    val width = baseIcon.width
    val height = baseIcon.height

    // Create new bitmap for composition
    val composite = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(composite)

    // Draw base calendar image
    canvas.drawBitmap(baseIcon, 0f, 0f, null)

    // Paint for the date badge circle
    val badgePaint = Paint().apply {
        color = Color.parseColor("#FF6200EE") // Material Purple
        isAntiAlias = true
    }

    // Paint for the date text
    val textPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = width * 0.40f
    }

    // Position badge at bottom-right (notification badge style)
    val badgeRadius = width * 0.32f
    val badgeCenterX = width - badgeRadius - (width * 0.02f)
    val badgeCenterY = height - badgeRadius - (height * 0.02f)

    // Draw filled circle for badge background
    canvas.drawCircle(badgeCenterX, badgeCenterY, badgeRadius, badgePaint)

    // Draw date number in badge
    val fontMetrics = textPaint.fontMetrics
    val textY = badgeCenterY - (fontMetrics.descent + fontMetrics.ascent) / 2
    canvas.drawText(dateText, badgeCenterX, textY, textPaint)

    return composite
}

private fun saveCompositeIconBitmap(context: Context, bitmap: Bitmap) {
    try {
        // Create cache directory for icons
        val cacheDir = File(context.cacheDir, "calendar_icons")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        // Save the composite icon
        val iconFile = File(cacheDir, "calendar_with_date.png")
        FileOutputStream(iconFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
        }

        Log.d("IconGeneration", "Composite icon saved to: ${iconFile.absolutePath}")
    } catch (e: Exception) {
        Log.e("IconGeneration", "Failed to save composite icon", e)
    }
}

fun loadCompositeIconBitmap(context: Context): Bitmap? {
    return try {
        val iconFile = File(context.cacheDir, "calendar_icons/calendar_with_date.png")
        if (iconFile.exists()) {
            BitmapFactory.decodeFile(iconFile.absolutePath)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("IconGeneration", "Failed to load composite icon from cache", e)
        null
    }
}
