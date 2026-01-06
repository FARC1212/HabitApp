package com.habitapp3.data

// Define los tipos de sueño
enum class SleepStage {
    AWAKE, LIGHT, MEDIUM, DEEP
}

// Clase para representar una sesión de sueño completa
data class SleepSession(
    val startDate: String, // "24/09/2024"
    val startTime: String, // "11:05 PM"
    val endTime: String,   // "07:15 AM"
    val totalTime: String, // "8h 10m"
    val lightSleepTime: String, // "2h 30m"
    val mediumSleepTime: String, // "4h 10m"
    val deepSleepTime: String,   // "1h 30m"
)
    