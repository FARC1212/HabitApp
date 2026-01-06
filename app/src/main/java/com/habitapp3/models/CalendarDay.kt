package com.habitapp3.models // Asegúrate de que el paquete coincida con tu carpeta

data class CalendarDay(
    val dayName: String,      // Ej: "LUN"
    val dayNumber: String,    // Ej: "06"
    var isCompleted: Boolean = false, // ¿Cumplió la meta ese día?
    var isSelected: Boolean = false   // ¿Es el día que estamos viendo?
)