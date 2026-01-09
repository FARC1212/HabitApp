package com.habitapp3

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager // Necesario para permisos
import android.os.Build // Necesario para verificar versión Android
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import android.Manifest // Necesario para Manifest.permission

class RecordatoriosActivity : AppCompatActivity() {

    private lateinit var layoutPregunta: LinearLayout
    private lateinit var layoutConfiguracion: LinearLayout
    private lateinit var layoutTimePicker: LinearLayout
    private lateinit var tvHoraSeleccionada: TextView
    private var horaElegida = "09:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recordatorios)

        // --- NUEVO: PEDIR PERMISO AL ENTRAR (Para Android 13+) ---
        solicitarPermisoNotificaciones()

        // Referencias
        layoutPregunta = findViewById(R.id.layoutPregunta)
        layoutConfiguracion = findViewById(R.id.layoutConfiguracion)
        layoutTimePicker = findViewById(R.id.layoutTimePicker)
        tvHoraSeleccionada = findViewById(R.id.tvHoraSeleccionada)

        val btnSi = findViewById<View>(R.id.btnSiQuiero)
        val btnNo = findViewById<View>(R.id.btnNoGracias)
        val btnFinalizar = findViewById<View>(R.id.btnFinalizar)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupFrecuencia)
        // val rbHoraFija = findViewById<RadioButton>(R.id.rbHoraFija) // No es estrictamente necesario instanciarlo si usamos el ID en el when

        // 1. SI QUIERE NOTIFICACIONES
        btnSi.setOnClickListener {
            layoutPregunta.visibility = View.GONE
            layoutConfiguracion.visibility = View.VISIBLE
        }

        // 2. NO QUIERE (Saltar al inicio)
        btnNo.setOnClickListener {
            guardarPreferencias(false, "nunca")
            irAlMain()
        }

        // 3. Lógica del RadioGroup (Mostrar selector de hora)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbHoraFija) {
                layoutTimePicker.visibility = View.VISIBLE
            } else {
                layoutTimePicker.visibility = View.GONE
            }
        }

        // 4. Selector de Hora (Click en el texto de la hora)
        tvHoraSeleccionada.setOnClickListener {
            mostrarReloj()
        }

        // 5. FINALIZAR
        btnFinalizar.setOnClickListener {
            val frecuencia = when (radioGroup.checkedRadioButtonId) {
                R.id.rb4Horas -> "4_horas"
                R.id.rb12Horas -> "12_horas"
                R.id.rbHoraFija -> "fija_$horaElegida"
                else -> "12_horas" // Default
            }

            // A) Guardamos que el usuario dijo "SÍ"
            guardarPreferencias(true, frecuencia)

            // B) --- NUEVO: PROGRAMAMOS LA ALARMA REAL ---
            // Esto llama al archivo NotificacionesHelper.kt que creamos antes
            NotificacionesHelper.programarNotificacion(this, frecuencia)

            irAlMain()
        }
    }

    // --- NUEVO: FUNCIÓN PARA PEDIR PERMISO EN ANDROID 13 ---
    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun mostrarReloj() {
        val calendario = Calendar.getInstance()
        val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
        val minutoActual = calendario.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, hour, minute ->
            // Formatear hora (Ej: 09:05)
            val horaFormateada = String.format("%02d:%02d", hour, minute)
            horaElegida = horaFormateada
            tvHoraSeleccionada.text = horaFormateada
        }, horaActual, minutoActual, false) // false = formato 12h (AM/PM), true = 24h

        timePicker.show()
    }

    private fun guardarPreferencias(activado: Boolean, frecuencia: String) {
        val prefs = getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("NOTIFICACIONES_ACTIVAS", activado)
            putString("FRECUENCIA_NOTIFICACION", frecuencia)
            putBoolean("USER_ONBOARDING_COMPLETE", true) // ¡Importante! Aquí marcamos que terminó todo
            apply()
        }
    }

    private fun irAlMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}