package com.example.calculator

import java.io.Serializable
import java.util.Date

data class CalendarEvent(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val date: Long, // Timestamp representing the date
    val color: String = "#1976D2", // HEX color code
    val description: String = "",
    val category: String = "general" // general, holiday, personal, work, birthday, etc.
) : Serializable
