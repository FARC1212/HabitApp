package com.habitapp3.fragments

import android.Manifest // Importación necesaria para el permiso
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // Importación para el botón
import android.widget.TextView // Importación para el texto
import android.widget.Toast // Para mostrar mensajes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.habitapp3.R // Tus recursos de la app

class Cama : Fragment() {

    // 1. Constante para el código de solicitud de permiso
    private val RECORD_AUDIO_PERMISSION_CODE = 1

    // 2. Referencias a los elementos de la UI (vistas)
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var statusTextView: TextView

    // 3. Instancia de nuestro monitor de sonido
    private var soundMonitor: SoundMonitor? = null

    // Este método se ejecuta para crear la vista del fragmento
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // "Infla" o crea la vista desde tu archivo XML
        val view = inflater.inflate(R.layout.fragment_cama, container, false)

        // 4. Conecta las variables con los elementos del layout
        startButton = view.findViewById(R.id.startButton) // Asume que tienes un botón con id "startButton"
        stopButton = view.findViewById(R.id.stopButton)   // Asume que tienes un botón con id "stopButton"
        statusTextView = view.findViewById(R.id.statusTextView) // Asume que tienes un TextView con id "statusTextView"

        // 5. Configura los listeners de los botones
        startButton.setOnClickListener {
            checkPermissionAndStartMonitoring()
        }

        stopButton.setOnClickListener {
            stopMonitoring()
        }

        return view
    }

    // 6. Función para verificar permiso y empezar a monitorear
    private fun checkPermissionAndStartMonitoring() {
        // Verifica si el permiso NO está concedido
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // Si no lo está, pide el permiso al usuario
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            // Si ya tienes el permiso, empieza a monitorear
            startMonitoring()
        }
    }

    // 7. Función que inicia el monitoreo
    private fun startMonitoring() {
        statusTextView.text = "Monitoreando sonido..."
        startButton.isEnabled = false // Deshabilita el botón de inicio
        stopButton.isEnabled = true  // Habilita el botón de parar

        soundMonitor = SoundMonitor()
        // El lambda (código entre llaves) se ejecutará cada vez que haya una nueva lectura de sonido
        soundMonitor?.startMonitoring { soundLevel ->
            val movement = sonidoADesplazamiento(soundLevel)

            // Importante: Debes actualizar la UI desde el hilo principal
            activity?.runOnUiThread {
                statusTextView.text = "Nivel de sonido: ${"%.2f".format(soundLevel)} dB\nMovimiento estimado: $movement"
            }
        }
        Toast.makeText(context, "Monitoreo iniciado", Toast.LENGTH_SHORT).show()
    }

    // 8. Función para detener el monitoreo
    private fun stopMonitoring() {
        soundMonitor?.stopMonitoring()
        soundMonitor = null // Libera la instancia

        statusTextView.text = "Toca 'Iniciar' para comenzar"
        startButton.isEnabled = true
        stopButton.isEnabled = false
        Toast.makeText(context, "Monitoreo detenido", Toast.LENGTH_SHORT).show()
    }

    // 9. Este método se llama después de que el usuario responde a la solicitud de permiso
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, ahora sí empieza a monitorear
                startMonitoring()
            } else {
                // Permiso denegado
                Toast.makeText(context, "Permiso de audio denegado. No se puede iniciar el monitoreo.", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Asegúrate de detener el monitoreo si el fragmento se destruye
    override fun onDestroyView() {
        super.onDestroyView()
        soundMonitor?.stopMonitoring() // Limpia recursos para evitar fugas de memoria
    }
}
