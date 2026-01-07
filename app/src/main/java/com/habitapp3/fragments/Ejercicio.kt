package com.habitapp3.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
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
    private lateinit var layoutDias: LinearLayout // Nuevo contenedor para los textos

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ejercicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardNotificacion = view.findViewById(R.id.cardNotificacion)
        tvNotificacion = view.findViewById(R.id.tvMensajeEstado)
        btnIr = view.findViewById(R.id.btnIrEjercicio)
        layoutGrafica = view.findViewById(R.id.layoutGrafica)
        layoutDias = view.findViewById(R.id.layoutDias) // Vinculamos

        btnIr.setOnClickListener {
            val intent = Intent(requireContext(), DetalleEjercicioActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        verificarEstadoDiario()
        dibujarGraficaSemanal()
        verificarSiDeseaActivar()
    }

    private fun dibujarGraficaSemanal() {
        // Limpiamos ambos contenedores antes de dibujar
        layoutGrafica.removeAllViews()
        layoutDias.removeAllViews()

        val prefs = requireContext().getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val historialSet = prefs.getStringSet("HISTORIAL_RUTINAS", emptySet()) ?: emptySet()

        // Configuración de fechas (Lunes de esta semana)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Configuración visual
        val alturaMinimaPx = (20 * resources.displayMetrics.density).toInt() // 20dp en pixeles
        val colorVerde = ContextCompat.getColor(requireContext(), R.color.verde_agua)
        val colorGris = Color.parseColor("#E0E0E0") // Un gris más suave y moderno
        val diasLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

        for (i in 0 until 7) {
            val fechaCheck = format.format(calendar.time)
            val entrenoEseDia = historialSet.contains(fechaCheck)

            // 1. CREAR LA BARRA
            val barra = View(requireContext())
            val paramsBarra = LinearLayout.LayoutParams(
                0, // Ancho 0 porque usamos weight
                if (entrenoEseDia) ViewGroup.LayoutParams.MATCH_PARENT else alturaMinimaPx
            )
            paramsBarra.weight = 1f
            // Márgenes laterales para que las barras no se peguen (ajustado para que se vea elegante)
            paramsBarra.setMargins(12, 0, 12, 0)
            barra.layoutParams = paramsBarra

            // Bordes redondeados para la barra (Opcional, pero se ve mejor)
            // Si no tienes un drawable específico, usamos color plano:
            barra.setBackgroundColor(if (entrenoEseDia) colorVerde else colorGris)


            // 2. CREAR LA ETIQUETA DEL DÍA
            val tvDia = TextView(requireContext())
            val paramsTexto = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            paramsTexto.weight = 1f
            // Importante: Sin márgenes en el texto para que ocupe todo el ancho de la columna imaginaria y se centre bien
            tvDia.layoutParams = paramsTexto

            tvDia.text = diasLabels[i]
            tvDia.gravity = Gravity.CENTER
            tvDia.textSize = 12f

            // Resaltar el día de hoy
            val esHoy = (calendar.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
            if (esHoy) {
                tvDia.setTextColor(colorVerde)
                tvDia.typeface = android.graphics.Typeface.DEFAULT_BOLD
            } else {
                tvDia.setTextColor(Color.GRAY)
            }

            // 3. AÑADIR A SUS RESPECTIVOS LAYOUTS
            layoutGrafica.addView(barra)
            layoutDias.addView(tvDia)

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun verificarEstadoDiario() {
        val prefs = requireContext().getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val ultimoEntreno = prefs.getString("ULTIMO_ENTRENAMIENTO", "")
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (ultimoEntreno == fechaHoy) {
            cardNotificacion.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.verde_agua))
            tvNotificacion.text = "¡Excelente trabajo hoy!"
            btnIr.text = "Ver Rutina"
            btnIr.setTextColor(ContextCompat.getColor(requireContext(), R.color.verde_agua))
        } else {
            // Un amarillo suave o gris oscuro se ve mejor que el amarillo chillón por defecto
            cardNotificacion.setCardBackgroundColor(Color.parseColor("#FFC107")) // Amber 500
            tvNotificacion.text = "¡No has hecho tu ejercicio hoy!"
            btnIr.text = "Hacerlo ahora"
            btnIr.setTextColor(Color.parseColor("#FFC107"))
        }
    }

    private fun verificarSiDeseaActivar() {
        val context = context ?: return
        val prefs = context.getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val estaVisible = prefs.getBoolean("VER_EJERCICIO", true)

        if (!estaVisible) {
            AlertDialog.Builder(context)
                .setTitle("¿Activar hábito de Ejercicio?")
                .setMessage("Actualmente no estás siguiendo este hábito en el inicio. ¿Quieres agregarlo ahora?")
                .setPositiveButton("Sí, agregar") { _, _ ->
                    prefs.edit().putBoolean("VER_EJERCICIO", true).apply()
                    Toast.makeText(context, "¡A darle duro! Agregado al Inicio.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No, gracias") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}