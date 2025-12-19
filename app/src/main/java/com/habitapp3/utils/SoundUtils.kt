package com.habitapp3.fragments // O el paquete que hayas elegido

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.log10
import kotlin.math.sqrt

// --- La clase que te dio la IA para manejar el micrófono ---
class SoundMonitor {

    private val sampleRate = 44100
    private var bufferSize: Int
    private var recorder: AudioRecord? = null

    init {
        // Asegúrate de que los permisos ya están concedidos antes de crear el recorder
        bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
    }

    // @SuppressLint es para decirle a Android Studio que estamos seguros de que el permiso
    // fue verificado antes de llamar a este método.
    @SuppressLint("MissingPermission")
    fun startMonitoring(onSoundLevel: (Double) -> Unit) {
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        recorder?.startRecording()
        val buffer = ShortArray(bufferSize)

        // Ejecuta la lectura en un hilo separado para no bloquear la UI
        Thread {
            // Usamos una propiedad del grabador para controlar el bucle
            while (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = recorder?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    // Calcula el RMS (Root Mean Square), una medida de la potencia de la señal
                    val rms = sqrt(buffer.take(read).map { it.toDouble() * it.toDouble() }.average())
                    // Convierte RMS a decibelios (dB)
                    val db = 20 * log10(rms)
                    onSoundLevel(db)
                }
                Thread.sleep(500) // Pausa de medio segundo entre lecturas
            }
        }.start()
    }

    fun stopMonitoring() {
        if (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            recorder?.stop()
        }
        recorder?.release()
        recorder = null
    }
}

// --- La función que traduce sonido a "movimiento" ---
fun sonidoADesplazamiento(db: Double): Int {
    return when {
        db < 25 -> (0..2).random()   // Sueño profundo (ajusté los valores, ya que el cálculo da valores positivos)
        db < 35 -> (3..5).random()   // Fase REM
        db < 45 -> (6..8).random()   // Sueño ligero
        else -> (9..10).random()    // Despierto o ruido fuerte
    }
}
