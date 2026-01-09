package com.habitapp3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Este método se ejecuta automáticamente cuando llega la hora de la alarma
        mostrarNotificacion(context)
    }

    private fun mostrarNotificacion(context: Context) {
        val channelId = "habit_channel_id"
        val notificationId = 101

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Crear el Canal de Notificaciones (Obligatorio para Android 8 en adelante)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Hábitos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para recordatorios de rutina"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Configurar qué pasa al tocar la notificación (Abrir MainActivity)
        val intentApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // FLAG_IMMUTABLE es obligatorio en versiones nuevas de Android
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intentApp, PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Construir el diseño de la notificación
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.noti) // Asegúrate de tener un icono aquí (o usa R.drawable.ic_check si lo creaste)
            .setContentTitle("¡Es hora de tu habito!")
            .setContentText("Recuerda registrar tu progreso de hoy.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 4. Mostrarla en pantalla
        notificationManager.notify(notificationId, builder.build())
    }
}