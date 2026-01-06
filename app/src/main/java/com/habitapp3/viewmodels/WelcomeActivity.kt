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

        // 1. VERIFICAR SI YA EXISTE UN USUARIO
        val prefs = getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        val nombreGuardado = prefs.getString("NOMBRE_USUARIO", "")

        if (!nombreGuardado.isNullOrEmpty()) {
            // Si ya tiene nombre, saltamos directo a la App principal
            irAlMenuPrincipal()
            return // Detenemos la ejecución de esta pantalla
        }

        // 2. SI NO EXISTE, MOSTRAMOS EL DISEÑO DE BIENVENIDA
        setContentView(R.layout.activity_welcome)

        val etNombre = findViewById<TextInputEditText>(R.id.etNombreBienvenida)
        val btnComenzar = findViewById<Button>(R.id.btnComenzar)

        btnComenzar.setOnClickListener {
            val nombreIngresado = etNombre.text.toString().trim()

            if (nombreIngresado.isNotEmpty()) {
                // Guardamos el nombre
                prefs.edit().putString("NOMBRE_USUARIO", nombreIngresado).apply()

                // Entramos a la app
                irAlMenuPrincipal()
            } else {
                Toast.makeText(this, "Por favor, dinos cómo te llamas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irAlMenuPrincipal() {
        val intent = Intent(this, SeleccionHabitosActivity::class.java)
        startActivity(intent)
        finish()
    }
}