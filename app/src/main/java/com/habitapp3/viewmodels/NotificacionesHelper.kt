package com.habitapp3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object NotificacionesHelper {

    fun programarNotificacion(context: Context, tipoFrecuencia: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)

        // FLAG_IMMUTABLE es obligatorio en Android 12+
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Cancelamos cualquier alarma previa para no duplicar
        alarmManager.cancel(pendingIntent)

        // Si el usuario eligió "nunca", terminamos aquí (ya cancelamos la anterior)
        if (tipoFrecuencia == "nunca" || !tipoFrecuencia.isNotEmpty()) return

        // Calculamos cuándo debe sonar
        var triggerTime: Long = 0
        var interval: Long = 0

        when {
            tipoFrecuencia == "4_horas" -> {
                triggerTime = System.currentTimeMillis() + (4 * 60 * 60 * 1000) // 4 horas
                interval = 4 * 60 * 60 * 1000
            }
            tipoFrecuencia == "12_horas" -> {
                triggerTime = System.currentTimeMillis() + (12 * 60 * 60 * 1000) // 12 horas
                interval = 12 * 60 * 60 * 1000
            }
            tipoFrecuencia.startsWith("fija_") -> {
                // Formato esperado: "fija_09:00"
                try {
                    val horaStr = tipoFrecuencia.substringAfter("_") // "09:00"
                    val partes = horaStr.split(":")
                    val hora = partes[0].toInt()
                    val minuto = partes[1].toInt()

                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hora)
                        set(Calendar.MINUTE, minuto)
                        set(Calendar.SECOND, 0)
                    }

                    // Si la hora ya pasó hoy, programar para mañana
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }

                    triggerTime = calendar.timeInMillis
                    interval = AlarmManager.INTERVAL_DAY // Repetir cada 24 horas
                } catch (e: Exception) {
                    // Si falla algo, poner 12 horas por defecto
                    triggerTime = System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_DAY
                    interval = AlarmManager.INTERVAL_HALF_DAY
                }
            }
            else -> return
        }

        // Verificamos permisos de alarmas exactas (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Si no tiene permiso exacto, usamos setWindow o set inexacto,
                // pero por simplicidad aquí intentaremos setRepeating normal
                // (En una app real deberías pedir permiso ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            }
        }

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                interval,
                pendingIntent
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}