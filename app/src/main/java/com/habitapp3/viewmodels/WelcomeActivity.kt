package com.habitapp3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)

        // 1. ¿YA COMPLETÓ TODO EL PROCESO? (Nombre + Selección de hábitos)
        val onboardingCompleto = prefs.getBoolean("USER_ONBOARDING_COMPLETE", false)

        if (onboardingCompleto) {
            // Si ya terminó todo, lo mandamos directo a la pantalla principal (Home)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return // ¡Importante! Detenemos aquí.
        }

        // 2. ¿YA TIENE NOMBRE PERO LE FALTA ELEGIR HÁBITOS?
        val nombreGuardado = prefs.getString("NOMBRE_USUARIO", "")
        if (!nombreGuardado.isNullOrEmpty()) {
            // Si ya sabemos cómo se llama, pasamos directo a la selección
            irASeleccionHabitos()
            return
        }

        // 3. SI ES UN USUARIO NUEVO (Ni nombre, ni hábitos): Mostramos el diseño
        setContentView(R.layout.activity_welcome)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreBienvenida)
        val btnComenzar = findViewById<Button>(R.id.btnComenzar)

        btnComenzar.setOnClickListener {
            val nombreIngresado = etNombre.text.toString().trim()

            if (nombreIngresado.isNotEmpty()) {
                // Guardamos el nombre
                prefs.edit().putString("NOMBRE_USUARIO", nombreIngresado).apply()

                // Ahora sí, vamos a elegir los hábitos
                irASeleccionHabitos()
            } else {
                Toast.makeText(this, "Por favor, dinos cómo te llamas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irASeleccionHabitos() {
        val intent = Intent(this, SeleccionHabitosActivity::class.java)
        startActivity(intent)
        finish()
    }
}