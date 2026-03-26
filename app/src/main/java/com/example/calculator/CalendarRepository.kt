package com.example.calculator

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

class CalendarRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("calendar_events", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val TAG = "CalendarRepository"
    private val EVENTS_KEY = "events"

    fun addEvent(event: CalendarEvent): Boolean {
        return try {
            val events = getEvents().toMutableList()
            events.add(event)
            saveEvents(events)
            Log.d(TAG, "Event added: ${event.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding event", e)
            false
        }
    }

    fun updateEvent(event: CalendarEvent): Boolean {
        return try {
            val events = getEvents().toMutableList()
            val index = events.indexOfFirst { it.id == event.id }
            if (index != -1) {
                events[index] = event
                saveEvents(events)
                Log.d(TAG, "Event updated: ${event.title}")
                return true
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event", e)
            false
        }
    }

    fun deleteEvent(eventId: String): Boolean {
        return try {
            val events = getEvents().toMutableList()
            val removed = events.removeIf { it.id == eventId }
            if (removed) {
                saveEvents(events)
                Log.d(TAG, "Event deleted: $eventId")
            }
            removed
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting event", e)
            false
        }
    }

    fun getEvents(): List<CalendarEvent> {
        return try {
            val json = prefs.getString(EVENTS_KEY, "[]") ?: "[]"
            val type = object : TypeToken<List<CalendarEvent>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving events", e)
            emptyList()
        }
    }

    fun getEventsByDate(dateInMillis: Long): List<CalendarEvent> {
        return getEvents().filter { event ->
            val eventCal = Calendar.getInstance().apply { timeInMillis = event.date }
            val dateCal = Calendar.getInstance().apply { timeInMillis = dateInMillis }
            eventCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
                    eventCal.get(Calendar.MONTH) == dateCal.get(Calendar.MONTH) &&
                    eventCal.get(Calendar.DAY_OF_MONTH) == dateCal.get(Calendar.DAY_OF_MONTH)
        }
    }

    fun getEventsByMonth(year: Int, month: Int): List<CalendarEvent> {
        return getEvents().filter { event ->
            val eventCal = Calendar.getInstance().apply { timeInMillis = event.date }
            eventCal.get(Calendar.YEAR) == year && eventCal.get(Calendar.MONTH) == month
        }
    }

    fun getEventById(eventId: String): CalendarEvent? {
        return getEvents().find { it.id == eventId }
    }

    fun clearAllEvents() {
        try {
            prefs.edit().remove(EVENTS_KEY).apply()
            Log.d(TAG, "All events cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing events", e)
        }
    }

    fun importDefaultEvents() {
        try {
            // Add some default events if none exist
            if (getEvents().isEmpty()) {
                val defaultEvents = listOf(
                    CalendarEvent(
                        title = "Happy birthday!",
                        date = getDateInCurrentYear(3, 1),
                        color = "#1976D2",
                        category = "birthday"
                    ),
                    CalendarEvent(
                        title = "Holika Dahanam",
                        date = getDateInCurrentYear(3, 4),
                        color = "#2E7D32",
                        category = "holiday"
                    ),
                    CalendarEvent(
                        title = "Holi",
                        date = getDateInCurrentYear(3, 5),
                        color = "#2E7D32",
                        category = "holiday"
                    )
                )
                defaultEvents.forEach { addEvent(it) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error importing default events", e)
        }
    }

    private fun saveEvents(events: List<CalendarEvent>) {
        try {
            val json = gson.toJson(events)
            prefs.edit().putString(EVENTS_KEY, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving events", e)
        }
    }

    fun searchEvents(query: String): List<CalendarEvent> {
        return try {
            val searchQuery = query.lowercase().trim()
            if (searchQuery.isEmpty()) {
                emptyList()
            } else {
                getEvents().filter { event ->
                    event.title.lowercase().contains(searchQuery) ||
                    event.description.lowercase().contains(searchQuery) ||
                    event.category.lowercase().contains(searchQuery)
                }.sortedBy { it.date }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching events", e)
            emptyList()
        }
    }

    private fun getDateInCurrentYear(month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
