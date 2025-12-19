package com.habitapp3

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetalleEjercicioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_ejercicio)

        // Recuperar preferencia de Split (PPL o UpperLower) guardada
        val prefs = getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val splitSeleccionado = prefs.getInt("SPLIT_SELECCIONADO", R.id.rbPPL)

        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupSplit)
        radioGroup.check(splitSeleccionado) // Marcar el que estaba guardado

        // ... (Tu código de referencias a layouts PPL y UL igual que antes) ...
        val layoutPPL = findViewById<LinearLayout>(R.id.layoutPPL)
        val layoutUL = findViewById<LinearLayout>(R.id.layoutUpperLower)

        // Lógica de visibilidad inicial basada en lo guardado
        if (splitSeleccionado == R.id.rbPPL) {
            layoutPPL.visibility = View.VISIBLE
            layoutUL.visibility = View.GONE
        } else {
            layoutPPL.visibility = View.GONE
            layoutUL.visibility = View.VISIBLE
        }

        // Listener para guardar el cambio de rutina
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            prefs.edit().putInt("SPLIT_SELECCIONADO", checkedId).apply()
            if (checkedId == R.id.rbPPL) {
                layoutPPL.visibility = View.VISIBLE
                layoutUL.visibility = View.GONE
            } else {
                layoutPPL.visibility = View.GONE
                layoutUL.visibility = View.VISIBLE
            }
        }

        // 2. Configurar los datos de PUSH PULL LEG
        configurarDia(R.id.ppl_dia1, "Día 1: Push (Empuje)", "• Press Banca: 4x8\n• Press Militar: 3x10\n• Fondos: 3x12\n• Vuelos Lat: 3x15\n• Tríceps Polea: 3x12")
        configurarDia(R.id.ppl_dia2, "Día 2: Pull (Tracción)", "• Dominadas: 3xFallo\n• Remo Barra: 4x8\n• Jalón Pecho: 3x10\n• Curl Barra: 3x10\n• Curl Martillo: 3x12")
        configurarDia(R.id.ppl_dia3, "Día 3: Leg (Pierna)", "• Sentadilla: 4x6\n• Prensa: 3x10\n• Extensión Cuádriceps: 3x15\n• Curl Femoral: 3x12\n• Pantorrilla: 4x20")
        configurarDia(R.id.ppl_dia4, "Día 4: Push (Enfoque Hombro)", "• Press Militar Mancuernas: 4x8\n• Press Inclinado: 3x10\n• Vuelos Laterales: 4x15\n• Press Francés: 3x10")
        configurarDia(R.id.ppl_dia5, "Día 5: Pull (Enfoque Densidad)", "• Peso Muerto: 3x5\n• Remo Mancuerna: 3x10\n• Facepull: 3x15\n• Curl Predicador: 3x12")

        // 3. Configurar los datos de UPPER LOWER
        configurarDia(R.id.ul_dia1, "Día 1: Upper (Fuerza)", "• Press Banca: 5x5\n• Remo Pendlay: 5x5\n• Press Militar: 3x8\n• Dominadas: 3x8")
        configurarDia(R.id.ul_dia2, "Día 2: Lower (Fuerza)", "• Sentadilla: 5x5\n• Peso Muerto Rumano: 3x8\n• Prensa: 3x10\n• Gemelos: 4x15")
        configurarDia(R.id.ul_dia3, "Día 3: Descanso Activo", "• Caminata suave de 30 min\n• Sesión de estiramientos (20 min)\n• Hidratación")
        configurarDia(R.id.ul_dia4, "Día 4: Upper (Hipertrofia)", "• Press Inclinado Mancuernas: 3x10\n• Jalón al Pecho: 3x12\n• Vuelos Laterales: 3x15\n• Curl Bíceps + Extensión Tríceps: 3x12")
        configurarDia(R.id.ul_dia5, "Día 5: Lower (Hipertrofia)", "• Zancadas: 3x12\n• Hip Thrust: 4x10\n• Extensión de pierna: 3x15\n• Curl Femoral: 3x15")
    }

    // Esta función mágica evita repetir código 10 veces
    private fun configurarDia(includeId: Int, titulo: String, rutina: String) {
        val card = findViewById<CardView>(includeId)
        val tvTitulo = card.findViewById<TextView>(R.id.tvTituloDia)
        val tvEjercicios = card.findViewById<TextView>(R.id.tvEjercicios)
        val layoutDetalle = card.findViewById<LinearLayout>(R.id.layoutDetalle)
        val checkbox = card.findViewById<CheckBox>(R.id.checkboxCompletado) // ¡Ahora sí lo usamos!

        tvTitulo.text = titulo
        tvEjercicios.text = rutina

        // 1. Verificar si este checkbox específico ya estaba marcado hoy
        // Usamos una clave única combinando el ID del día + la fecha de hoy
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val key = "DONE_${includeId}_$fechaHoy"

        checkbox.isChecked = prefs.getBoolean(key, false)

        // 2. Guardar cuando se marca/desmarca
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            val editor = prefs.edit()
            editor.putBoolean(key, isChecked)

            // IMPORTANTE: Guardamos también que "Hoy se entrenó" en general
            if (isChecked) {
                editor.putString("ULTIMO_ENTRENAMIENTO", fechaHoy)

                // Guardamos historial para la gráfica (Ej: "2023-12-18" = true)
                editor.putBoolean("HISTORIAL_$fechaHoy", true)
            }

            editor.apply()
        }

        // Expandir / Contraer
        card.setOnClickListener {
            layoutDetalle.visibility = if (layoutDetalle.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }
}