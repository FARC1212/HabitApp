package com.habitapp3.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitapp3.R
import com.habitapp3.adapters.CalendarAdapter
import com.habitapp3.models.CalendarDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Inicio : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar RecyclerView
        val rvCalendar = view.findViewById<RecyclerView>(R.id.rv_calendar_week)
        rvCalendar.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // 2. Generar los días REALES basados en la fecha del celular
        val daysList = generarDiasSemana()

        // 3. Asignar el adaptador
        val adapter = CalendarAdapter(daysList)
        rvCalendar.adapter = adapter

        // (Opcional) Hacer que el calendario scrollee automáticamente al día de hoy
        val hoyIndex = daysList.indexOfFirst { it.isSelected }
        if (hoyIndex != -1) {
            rvCalendar.scrollToPosition(hoyIndex)
        }
    }

    // -------------------------------------------------------------
    // NUEVA FUNCIÓN: Genera la semana actual automáticamente
    // -------------------------------------------------------------
    private fun generarDiasSemana(): List<CalendarDay> {
        val listaDias = mutableListOf<CalendarDay>()
        val fechaHoy = Calendar.getInstance()
        val calendario = Calendar.getInstance()

        // Ajustar al Lunes de la semana actual
        calendario.firstDayOfWeek = Calendar.MONDAY
        calendario.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val formatoNombre = SimpleDateFormat("EEE", Locale("es", "ES"))
        val formatoNumero = SimpleDateFormat("dd", Locale.getDefault())

        // --- NUEVO: PREPARAMOS LA LECTURA DEL HISTORIAL ---
        // Formato para comparar con lo guardado en base de datos (Ej: "2024-05-20")
        val formatoGuardado = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val context = context ?: return emptyList() // Seguridad por si el contexto es nulo
        val prefs = context.getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)

        // Obtenemos la lista de días cumplidos (Si no hay nada, devuelve lista vacía)
        val historialSet = prefs.getStringSet("HISTORIAL_RUTINAS", emptySet()) ?: emptySet()

        // ---------------------------------------------------

        for (i in 0..6) {
            val nombre = formatoNombre.format(calendario.time).uppercase().replace(".", "")
            val numero = formatoNumero.format(calendario.time)

            // Obtenemos la fecha actual del bucle en formato texto (ej: "2024-05-20")
            val fechaBucleString = formatoGuardado.format(calendario.time)

            // 1. ¿Es hoy?
            val esHoy = (calendario.get(Calendar.DAY_OF_YEAR) == fechaHoy.get(Calendar.DAY_OF_YEAR)) &&
                    (calendario.get(Calendar.YEAR) == fechaHoy.get(Calendar.YEAR))

            // 2. ¿Está completado? (Buscamos si la fecha existe en el historial)
            val estaCompletado = historialSet.contains(fechaBucleString)

            listaDias.add(CalendarDay(nombre, numero, isCompleted = estaCompletado, isSelected = esHoy))

            calendario.add(Calendar.DAY_OF_MONTH, 1)
        }

        return listaDias
    }

    override fun onResume() {
        super.onResume()
        actualizarTablero()
    }

    private fun actualizarTablero() {
        val view = view ?: return
        val context = requireContext()
        val prefs = context.getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val fechaHoySistema = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // ----------------------------------------------------
        // 1. NUEVO: CONTROL DE VISIBILIDAD (OCULTAR TARJETAS)
        // ----------------------------------------------------
        // Leemos si el usuario activó estos hábitos (Por defecto 'true' para que se vean si no hay datos)
        val verSueno = prefs.getBoolean("VER_SUENO", true)
        val verEjercicio = prefs.getBoolean("VER_EJERCICIO", true)
        val verRelax = prefs.getBoolean("VER_RELAX", true)

        // Buscamos las tarjetas por el ID que acabamos de poner en el XML
        val cardSueno = view.findViewById<View>(R.id.cardSuenoContainer)
        val cardEjercicio = view.findViewById<View>(R.id.cardEjercicioContainer)
        val cardRelax = view.findViewById<View>(R.id.cardRelaxContainer)

        // Si el usuario dijo que NO quería ver sueño, usamos View.GONE (Desaparece y no ocupa espacio)
        if (cardSueno != null) {
            cardSueno.visibility = if (verSueno) View.VISIBLE else View.GONE
        }

        if (cardEjercicio != null) {
            cardEjercicio.visibility = if (verEjercicio) View.VISIBLE else View.GONE
        }

        if (cardRelax != null) {
            cardRelax.visibility = if (verRelax) View.VISIBLE else View.GONE
        }

        // ----------------------------------------------------
        // 2. FECHA Y SALUDO (Lógica anterior)
        // ----------------------------------------------------
        val tvSaludo = view.findViewById<TextView>(R.id.tvWelcome)
        val fechaTexto = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(Date())
        val nombreUsuario = prefs.getString("NOMBRE_USUARIO", "Usuario")

        if (nombreUsuario == "Usuario" || nombreUsuario!!.isEmpty()) {
            tvSaludo.text = "¡Hola! Hoy es ${fechaTexto.replaceFirstChar { it.uppercase() }}"
        } else {
            tvSaludo.text = "¡Hola, $nombreUsuario!\nHoy es ${fechaTexto.replaceFirstChar { it.uppercase() }}"
        }

        // ----------------------------------------------------
        // 3. DATOS DE SUEÑO
        // ----------------------------------------------------
        if (verSueno) { // Solo actualizamos el texto si la tarjeta es visible
            val tvSuenoHoras = view.findViewById<TextView>(R.id.tvSuenoHoras)
            val ultimoSueno = prefs.getString("ULTIMO_SUENO_TIEMPO", "--h --m")
            if (tvSuenoHoras != null) tvSuenoHoras.text = ultimoSueno
        }

        // ----------------------------------------------------
        // 4. DATOS DE EJERCICIO
        // ----------------------------------------------------
        if (verEjercicio) { // Solo actualizamos texto si es visible
            val nombreRutina = prefs.getString("NOMBRE_RUTINA_TEXTO", "Sin rutina")
            val ultimoEntreno = prefs.getString("ULTIMO_ENTRENAMIENTO", "")

            val tvRutinaTitulo = view.findViewById<TextView>(R.id.tvRutinaActual)
            val tvEstado = view.findViewById<TextView>(R.id.tvEstadoEjercicio)

            if (tvRutinaTitulo != null) {
                tvRutinaTitulo.text = nombreRutina
                tvRutinaTitulo.textSize = 20f
            }

            if (tvEstado != null) {
                if (ultimoEntreno == fechaHoySistema) {
                    tvEstado.text = "✅ ¡Rutina completada hoy!"
                    tvEstado.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                } else {
                    tvEstado.text = "⏳ Pendiente por hacer"
                    tvEstado.setTextColor(android.graphics.Color.GRAY)
                }
            }
        }

        // (La sección de Relax no tenía lógica de texto todavía, así que solo ocultamos la tarjeta arriba)
    }
}