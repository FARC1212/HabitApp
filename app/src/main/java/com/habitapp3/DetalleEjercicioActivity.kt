package com.habitapp3

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleEjercicioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_ejercicio)

        val prefs = getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)

        // 1. RECUPERAR SELECCIÓN DE RUTINA
        val splitSeleccionado = prefs.getInt("SPLIT_SELECCIONADO", R.id.rbPPL)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupSplit)
        val layoutPPL = findViewById<LinearLayout>(R.id.layoutPPL)
        val layoutUL = findViewById<LinearLayout>(R.id.layoutUpperLower)

        // Configurar estado inicial visual
        radioGroup.check(splitSeleccionado)
        if (splitSeleccionado == R.id.rbPPL) {
            layoutPPL.visibility = View.VISIBLE
            layoutUL.visibility = View.GONE
        } else {
            layoutPPL.visibility = View.GONE
            layoutUL.visibility = View.VISIBLE
        }

        // 2. GUARDAR CUANDO CAMBIA DE RUTINA
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val editor = prefs.edit()
            editor.putInt("SPLIT_SELECCIONADO", checkedId)

            // Guardamos el nombre en texto para que el INICIO lo lea
            val nombreRutina = if (checkedId == R.id.rbPPL) "Push - Pull - Leg" else "Upper - Lower"
            editor.putString("NOMBRE_RUTINA_TEXTO", nombreRutina)
            editor.apply()

            // Actualizar visibilidad
            if (checkedId == R.id.rbPPL) {
                layoutPPL.visibility = View.VISIBLE
                layoutUL.visibility = View.GONE
            } else {
                layoutPPL.visibility = View.GONE
                layoutUL.visibility = View.VISIBLE
            }
        }

        // 3. CONFIGURAR LOS DÍAS (Aquí sucede la magia de la gráfica)
        // PPL
        configurarDia(R.id.ppl_dia1, "Día 1: Push", "• Press Banca\n• Press Militar...")
        configurarDia(R.id.ppl_dia2, "Día 2: Pull", "• Dominadas\n• Remo Barra...")
        configurarDia(R.id.ppl_dia3, "Día 3: Leg", "• Sentadilla\n• Prensa...")
        configurarDia(R.id.ppl_dia4, "Día 4: Push", "• Militar Mancuerna\n• Vuelos...")
        configurarDia(R.id.ppl_dia5, "Día 5: Pull", "• Peso Muerto\n• Curl Bíceps...")

        // Upper Lower
        configurarDia(R.id.ul_dia1, "Día 1: Upper", "• Press Banca\n• Remo...")
        configurarDia(R.id.ul_dia2, "Día 2: Lower", "• Sentadilla\n• Peso Muerto...")
        configurarDia(R.id.ul_dia3, "Día 3: Descanso", "• Caminata\n• Estiramientos...")
        configurarDia(R.id.ul_dia4, "Día 4: Upper", "• Press Inclinado\n• Jalón...")
        configurarDia(R.id.ul_dia5, "Día 5: Lower", "• Zancadas\n• Hip Thrust...")
    }

    private fun configurarDia(includeId: Int, titulo: String, rutina: String) {
        val card = findViewById<CardView>(includeId)
        val tvTitulo = card.findViewById<TextView>(R.id.tvTituloDia)
        val tvEjercicios = card.findViewById<TextView>(R.id.tvEjercicios)
        val layoutDetalle = card.findViewById<LinearLayout>(R.id.layoutDetalle)
        val checkbox = card.findViewById<CheckBox>(R.id.checkboxCompletado)

        tvTitulo.text = titulo
        tvEjercicios.text = rutina

        // --- LÓGICA IMPORTANTE PARA TU GRÁFICA ---
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)

        // Clave única para este checkbox hoy
        val keyCheckbox = "DONE_${includeId}_$fechaHoy"
        checkbox.isChecked = prefs.getBoolean(keyCheckbox, false)

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            val editor = prefs.edit()
            editor.putBoolean(keyCheckbox, isChecked) // Recordar el checkbox

            if (isChecked) {
                // 1. Guardar que HOY se entrenó (Para Inicio y para el color de la tarjeta)
                editor.putString("ULTIMO_ENTRENAMIENTO", fechaHoy)

                // 2. Guardar para la GRÁFICA (Esto lo lee tu fragmento Ejercicio.kt)
                editor.putBoolean("HISTORIAL_$fechaHoy", true)
            } else {
                // Si desmarca, podrías querer borrar el historial, pero por ahora lo dejamos simple
            }
            editor.apply()
        }

        // Expandir / Contraer detalles
        card.setOnClickListener {
            layoutDetalle.visibility = if (layoutDetalle.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }
}