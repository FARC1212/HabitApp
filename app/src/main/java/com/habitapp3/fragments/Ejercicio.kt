package com.habitapp3.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.habitapp3.R
import com.habitapp3.DetalleEjercicioActivity
import java.text.SimpleDateFormat
import java.util.*

class Ejercicio : Fragment() {

    private lateinit var cardNotificacion: CardView
    private lateinit var tvNotificacion: TextView
    private lateinit var btnIr: Button
    private lateinit var layoutGrafica: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ejercicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Vinculamos las vistas
        cardNotificacion = view.findViewById(R.id.cardNotificacion)
        tvNotificacion = view.findViewById(R.id.tvMensajeEstado)
        btnIr = view.findViewById(R.id.btnIrEjercicio)
        layoutGrafica = view.findViewById(R.id.layoutGrafica)

        // 2. Acción del botón
        btnIr.setOnClickListener {
            val intent = Intent(requireContext(), DetalleEjercicioActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizamos la pantalla cada vez que el usuario entra
        verificarEstadoDiario()
        dibujarGraficaSemanal()

        // --- NUEVO: Verificamos si quiere activar el hábito en el Inicio ---
        verificarSiDeseaActivar()
    }

    // --- NUEVA FUNCIÓN: Pregunta si quiere mostrar la tarjeta en Inicio ---
    private fun verificarSiDeseaActivar() {
        val context = context ?: return
        val prefs = context.getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)

        // OJO: Aquí la clave es VER_EJERCICIO
        val estaVisible = prefs.getBoolean("VER_EJERCICIO", true)

        if (!estaVisible) {
            AlertDialog.Builder(context)
                .setTitle("¿Activar hábito de Ejercicio?")
                .setMessage("Actualmente no estás siguiendo este hábito en el inicio. ¿Quieres agregarlo ahora?")
                .setPositiveButton("Sí, agregar") { _, _ ->
                    // Guardamos el cambio
                    prefs.edit().putBoolean("VER_EJERCICIO", true).apply()
                    Toast.makeText(context, "¡A darle duro! Agregado al Inicio.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No, gracias") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun verificarEstadoDiario() {
        val prefs = requireContext().getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val ultimoEntreno = prefs.getString("ULTIMO_ENTRENAMIENTO", "")
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (ultimoEntreno == fechaHoy) {
            // Si ya entrenó hoy: VERDE
            cardNotificacion.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.verde_agua))
            tvNotificacion.text = "¡Excelente trabajo hoy!"
            btnIr.text = "Ver Rutina"
            btnIr.setTextColor(ContextCompat.getColor(requireContext(), R.color.verde_agua))
            btnIr.background.setTint(Color.WHITE)
        } else {
            // Si no ha entrenado: AMARILLO
            cardNotificacion.setCardBackgroundColor(Color.parseColor("#FFEB3B"))
            tvNotificacion.text = "¡No has hecho tu ejercicio diario!"
            btnIr.text = "Hacerlo ahora"
            btnIr.setTextColor(Color.parseColor("#FFEB3B"))
            btnIr.background.setTint(Color.BLACK)
        }
    }

    private fun dibujarGraficaSemanal() {
        layoutGrafica.removeAllViews()
        val prefs = requireContext().getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)

        // Configuramos calendario al Lunes de esta semana
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // --- IMPORTANTE: Usamos el nuevo sistema de historial (Set) para ser consistentes con el calendario ---
        val historialSet = prefs.getStringSet("HISTORIAL_RUTINAS", emptySet()) ?: emptySet()

        for (i in 0 until 7) {
            val fechaCheck = format.format(calendar.time)

            // Verificamos si la fecha está en la lista de completados
            val entrenoEseDia = historialSet.contains(fechaCheck)

            // Crear barra
            val barra = View(requireContext())
            val params = LinearLayout.LayoutParams(
                0,
                if (entrenoEseDia) ViewGroup.LayoutParams.MATCH_PARENT else 20
            )
            params.weight = 1f
            params.setMargins(8, 0, 8, 0)
            barra.layoutParams = params

            // Color
            if (entrenoEseDia) {
                barra.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.verde_agua))
            } else {
                barra.setBackgroundColor(Color.LTGRAY)
            }

            layoutGrafica.addView(barra)

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }
}