package com.habitapp3.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // Importante para las imágenes
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitapp3.adapters.CalendarAdapter
import com.habitapp3.R
import java.text.SimpleDateFormat
import java.util.*

class Inicio : Fragment() {

    private lateinit var prefs: SharedPreferences

    // Variables para los textos
    private lateinit var tvWelcome: TextView
    private lateinit var tvSuenoHoras: TextView
    private lateinit var tvRutinaActual: TextView
    private lateinit var tvEstadoEjercicio: TextView
    private lateinit var tvEstadoRelax: TextView

    // Variables para los CHECKS (NUEVO)
    private lateinit var ivCheckSueno: ImageView
    private lateinit var ivCheckEjercicio: ImageView
    private lateinit var ivCheckRelax: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = requireContext().getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)

        // 1. Vincular las vistas
        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvSuenoHoras = view.findViewById(R.id.tvSuenoHoras)
        tvRutinaActual = view.findViewById(R.id.tvRutinaActual)
        tvEstadoEjercicio = view.findViewById(R.id.tvEstadoEjercicio)
        tvEstadoRelax = view.findViewById(R.id.tvEstadoRelax)

        // Vincular los Iconos de Check
        ivCheckSueno = view.findViewById(R.id.ivCheckSueno)
        ivCheckEjercicio = view.findViewById(R.id.ivCheckEjercicio)
        ivCheckRelax = view.findViewById(R.id.ivCheckRelax)

        // Configurar fecha y calendario
        setupDateAndCalendar(view)
    }

    // Usamos onResume para que se actualice cada vez que vuelves a esta pantalla
    override fun onResume() {
        super.onResume()
        actualizarEstadoHabitos()
    }

    private fun actualizarEstadoHabitos() {
        // --- A) SUEÑO ---
        // Aquí asumimos que guardas las horas de sueño. Si hay registro, mostramos check.
        // Ajusta la lógica según cómo guardes el sueño realmente.
        val horasSueno = prefs.getString("ULTIMO_SUENO_REGISTRADO", "--h --m") ?: "--h --m"
        tvSuenoHoras.text = horasSueno

        if (horasSueno != "--h --m") {
            ivCheckSueno.visibility = View.VISIBLE // ¡Check visible!
        } else {
            ivCheckSueno.visibility = View.GONE
        }

        // --- B) EJERCICIO ---
        // Revisamos si hoy se marcó como completado (desde el botón que hicimos antes)
        val rutinaCompletada = prefs.getBoolean("EJERCICIO_COMPLETADO_HOY", false)
        val nombreRutina = prefs.getString("RUTINA_SELECCIONADA", "Sin rutina")

        tvRutinaActual.text = nombreRutina

        if (rutinaCompletada) {
            tvEstadoEjercicio.text = "¡Rutina completada hoy!"
            tvEstadoEjercicio.setTextColor(resources.getColor(R.color.verde_agua, null)) // Texto verde opcional
            ivCheckEjercicio.visibility = View.VISIBLE // ¡Check visible!
        } else {
            tvEstadoEjercicio.text = "Estado pendiente"
            tvEstadoEjercicio.setTextColor(resources.getColor(R.color.texto_secundario, null))
            ivCheckEjercicio.visibility = View.GONE
        }

        // --- C) RELAX ---
        // Revisamos si hay minutos registrados hoy
        val minutosRelax = prefs.getInt("RELAX_MINUTOS_HOY", 0)

        if (minutosRelax > 0) {
            tvEstadoRelax.text = "$minutosRelax min de mindfulness hoy."
            ivCheckRelax.visibility = View.VISIBLE // ¡Check visible!
        } else {
            tvEstadoRelax.text = "Hoy no has registrado sesiones."
            ivCheckRelax.visibility = View.GONE
        }
    }

    private fun setupDateAndCalendar(view: View) {
        // Lógica de fecha (Tu código actual de calendario va aquí)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        val fechaStr = dateFormat.format(calendar.time).capitalize()

        // Obtener nombre usuario
        val nombre = prefs.getString("NOMBRE_USUARIO", "Usuario")
        tvWelcome.text = "¡Hola, $nombre!\nHoy es $fechaStr"

        // Configuración del RecyclerView del calendario (simplificado)
        val rvCalendar = view.findViewById<RecyclerView>(R.id.rv_calendar_week)
        rvCalendar.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        // Aquí iría tu adaptador: rvCalendar.adapter = CalendarAdapter(...)
    }
}