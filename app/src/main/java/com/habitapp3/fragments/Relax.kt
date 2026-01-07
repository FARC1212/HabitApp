package com.habitapp3.fragments

import android.app.AlertDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.habitapp3.R
import java.text.SimpleDateFormat
import java.util.*

class Relax : Fragment() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentSoundId: Int? = null

    // Variables para el Player UI
    private var cardPlayer: MaterialCardView? = null
    private var btnPlayPause: ImageButton? = null
    private var seekBar: SeekBar? = null
    private var txtPlayerTitle: TextView? = null
    private var txtDuration: TextView? = null
    private var isUserSeeking = false // Para saber si el usuario está arrastrando la barra

    // Handler para actualizar la barra de progreso
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBar = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                if (player.isPlaying && !isUserSeeking) {
                    seekBar?.progress = player.currentPosition
                    txtDuration?.text = formatTime(player.currentPosition) + " / " + formatTime(player.duration)
                }
                // Repetir cada segundo
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_relax, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas del Player
        cardPlayer = view.findViewById(R.id.cardPlayer)
        btnPlayPause = view.findViewById(R.id.btnPlayPause)
        seekBar = view.findViewById(R.id.seekBar)
        txtPlayerTitle = view.findViewById(R.id.txtPlayerTitle)
        txtDuration = view.findViewById(R.id.txtDuration)
        val btnClosePlayer = view.findViewById<View>(R.id.btnClosePlayer)

        // Botón Historial (esquina superior derecha)
        view.findViewById<View>(R.id.btnHistory).setOnClickListener {
            mostrarHistorial()
        }

        // Eventos del Player UI
        btnClosePlayer.setOnClickListener {
            detenerAudio()
        }

        btnPlayPause?.setOnClickListener {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    btnPlayPause?.setImageResource(android.R.drawable.ic_media_play)
                } else {
                    player.start()
                    btnPlayPause?.setImageResource(android.R.drawable.ic_media_pause)
                }
            }
        }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) txtDuration?.text = formatTime(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                mediaPlayer?.seekTo(seekBar?.progress ?: 0)
            }
        })

        // Configuración de tarjetas (igual que antes)
        view.findViewById<View>(R.id.cardRain).setOnClickListener { reproducirAudio(R.raw.lluvia, true, "Lluvia") }
        view.findViewById<View>(R.id.cardForest).setOnClickListener { reproducirAudio(R.raw.bosque, true, "Bosque") }
        view.findViewById<View>(R.id.cardOcean).setOnClickListener { reproducirAudio(R.raw.oceano, true, "Océano") }
        view.findViewById<View>(R.id.cardFire).setOnClickListener { reproducirAudio(R.raw.hoguera, true, "Hoguera") }

        view.findViewById<View>(R.id.cardVideo1).setOnClickListener { reproducirAudio(R.raw.mindfulness_basico, false, "Mindfulness Básico") }
        view.findViewById<View>(R.id.cardVideo2).setOnClickListener { reproducirAudio(R.raw.alivio_estres, false, "Alivio del Estrés") }
    }

    private fun reproducirAudio(rawResId: Int, loop: Boolean, nombre: String) {
        if (currentSoundId == rawResId && mediaPlayer?.isPlaying == true) {
            detenerAudio()
            return
        }

        detenerAudio() // Limpiar anterior

        try {
            mediaPlayer = MediaPlayer.create(context, rawResId)
            mediaPlayer?.isLooping = loop
            mediaPlayer?.start()
            currentSoundId = rawResId

            // Configurar UI según si es loop o sesión
            if (loop) {
                // Para sonidos ambiente, ocultamos el player complejo (opcional, o podrías mostrarlo sin barra)
                cardPlayer?.visibility = View.GONE
                Toast.makeText(context, "Ambiente: $nombre", Toast.LENGTH_SHORT).show()
            } else {
                // Para sesiones guiadas, mostramos el control
                mostrarPlayer(nombre)
            }

            // Listener de fin de reproducción
            if (!loop) {
                mediaPlayer?.setOnCompletionListener {
                    // AQUÍ GUARDAMOS EL HÁBITO
                    guardarSesionEnHistorial(nombre)
                    detenerAudio()
                    Toast.makeText(context, "¡Sesión completada!", Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mostrarPlayer(nombre: String) {
        cardPlayer?.visibility = View.VISIBLE
        txtPlayerTitle?.text = nombre
        btnPlayPause?.setImageResource(android.R.drawable.ic_media_pause)

        val totalDuration = mediaPlayer?.duration ?: 0
        seekBar?.max = totalDuration
        seekBar?.progress = 0

        // Iniciar el loop de actualización de la barra
        handler.post(updateSeekBar)
    }

    private fun detenerAudio() {
        handler.removeCallbacks(updateSeekBar) // Parar actualización de UI
        cardPlayer?.visibility = View.GONE // Ocultar panel

        if (mediaPlayer != null) {
            try {
                if (mediaPlayer!!.isPlaying) mediaPlayer!!.stop()
                mediaPlayer!!.release()
            } catch (e: Exception) { e.printStackTrace() }
            mediaPlayer = null
        }
        currentSoundId = null
    }

    // --- LÓGICA DE HISTORIAL ---

    private fun guardarSesionEnHistorial(nombreSesion: String) {
        val context = context ?: return
        // Nota: Usamos "HabitAppPrefs" para el calendario (para tener todo junto)
        // aunque el historial de texto detallado se guarde en "RelaxHistory"
        val prefs = context.getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val prefsDetalle = context.getSharedPreferences("RelaxHistory", Context.MODE_PRIVATE)

        // 1. Guardar historial detallado (Tu lógica existente)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaActual = sdf.format(Date())
        val nuevoRegistro = "$fechaActual - $nombreSesion"
        val historialActual = prefsDetalle.getString("LISTA_SESIONES", "") ?: ""
        val nuevoHistorial = if (historialActual.isEmpty()) nuevoRegistro else "$nuevoRegistro|$historialActual"
        prefsDetalle.edit().putString("LISTA_SESIONES", nuevoHistorial).apply()

        // 2. NUEVO: Guardar fecha simple para el CALENDARIO (Inicio)
        val sdfCal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaHoy = sdfCal.format(Date())

        val historialSet = prefs.getStringSet("HISTORIAL_RELAX", HashSet())?.toMutableSet() ?: mutableSetOf()
        historialSet.add(fechaHoy)

        prefs.edit().putStringSet("HISTORIAL_RELAX", historialSet).apply()
    }

    private fun mostrarHistorial() {
        val context = context ?: return
        val prefs = context.getSharedPreferences("RelaxHistory", Context.MODE_PRIVATE)
        val rawData = prefs.getString("LISTA_SESIONES", "") ?: ""

        if (rawData.isEmpty()) {
            Toast.makeText(context, "Aún no has completado ninguna sesión.", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertimos el string guardado en una lista
        val listaSesiones = rawData.split("|").toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Tu Historial de Relax")
            .setItems(listaSesiones, null) // Muestra la lista simple
            .setPositiveButton("Cerrar") { d, _ -> d.dismiss() }
            .setNeutralButton("Borrar Historial") { _, _ ->
                prefs.edit().clear().apply()
                Toast.makeText(context, "Historial borrado", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun formatTime(millis: Int): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        verificarSiDeseaActivar()
    }

    override fun onPause() {
        super.onPause()
        detenerAudio()
    }

    // ... Tu función verificarSiDeseaActivar() original sigue aquí abajo ...
    private fun verificarSiDeseaActivar() {
        val context = context ?: return
        val prefs = context.getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val estaVisible = prefs.getBoolean("VER_RELAX", true)

        if (!estaVisible) {
            AlertDialog.Builder(context)
                .setTitle("¿Activar hábito de Relax?")
                .setMessage("El seguimiento de estrés está desactivado en tu inicio. ¿Quieres añadir este hábito a tu tablero?")
                .setPositiveButton("Sí, lo necesito") { _, _ ->
                    prefs.edit().putBoolean("VER_RELAX", true).apply()
                    Toast.makeText(context, "¡Relax agregado al Inicio!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No por ahora") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}