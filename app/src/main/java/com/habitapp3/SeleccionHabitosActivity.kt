package com.habitapp3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SeleccionHabitosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_habitos)

        val checkSueno = findViewById<CheckBox>(R.id.checkSueno)
        val checkEjercicio = findViewById<CheckBox>(R.id.checkEjercicio)
        val checkRelax = findViewById<CheckBox>(R.id.checkRelax)
        val btnContinuar = findViewById<Button>(R.id.btnContinuarHome)

        btnContinuar.setOnClickListener {
            // Validamos que elija al menos uno
            if (!checkSueno.isChecked && !checkEjercicio.isChecked && !checkRelax.isChecked) {
                Toast.makeText(this, "Por favor, elige al menos un hábito", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardamos las preferencias de los hábitos
            val prefs = getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
            val editor = prefs.edit()

            editor.putBoolean("VER_SUENO", checkSueno.isChecked)
            editor.putBoolean("VER_EJERCICIO", checkEjercicio.isChecked)
            editor.putBoolean("VER_RELAX", checkRelax.isChecked)

            // IMPORTANTE: Aquí borré la línea de "USER_ONBOARDING_COMPLETE".
            // Esa se guardará recién en la siguiente pantalla (Recordatorios).

            editor.apply()

            // CAMBIO: Ahora vamos a la pantalla de Recordatorios, no al Main
            val intent = Intent(this, RecordatoriosActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}