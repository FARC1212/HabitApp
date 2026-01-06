package com.habitapp3.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial
import com.habitapp3.R

class Opciones : Fragment() {

    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_opciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar SharedPreferences
        prefs = requireContext().getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)

        // --- 1. LÓGICA DEL NOMBRE ---
        val etNombre = view.findViewById<EditText>(R.id.etNombreUsuario)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarNombre)

        // Cargar nombre actual si existe
        val nombreActual = prefs.getString("NOMBRE_USUARIO", "")
        etNombre.setText(nombreActual)

        btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString()
            if (nuevoNombre.isNotEmpty()) {
                prefs.edit().putString("NOMBRE_USUARIO", nuevoNombre).apply()
                Toast.makeText(context, "¡Nombre guardado!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Escribe un nombre válido", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 2. LÓGICA DEL MODO OSCURO ---
        val switchOscuro = view.findViewById<SwitchMaterial>(R.id.switchModoOscuro)

        // Detectar estado actual
        val esOscuro = prefs.getBoolean("MODO_OSCURO", false)
        switchOscuro.isChecked = esOscuro

        switchOscuro.setOnCheckedChangeListener { _, isChecked ->
            // Guardar preferencia
            prefs.edit().putBoolean("MODO_OSCURO", isChecked).apply()

            // Aplicar cambio (Esto reiniciará la actividad brevemente)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // --- 3. LÓGICA DE REINICIAR DATOS ---
        val btnBorrar = view.findViewById<Button>(R.id.btnReiniciarDatos)

        btnBorrar.setOnClickListener {
            mostrarAlertaBorrado()
        }
    }

    private fun mostrarAlertaBorrado() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Estás seguro?")
            .setMessage("Se perderán todos tus registros de sueño y configuración de ejercicios. Esta acción no se puede deshacer.")
            .setPositiveButton("Sí, borrar todo") { _, _ ->
                // Borrar todo el SharedPreferences
                prefs.edit().clear().apply()

                // Opcional: Restablecer modo claro por defecto
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                Toast.makeText(context, "Aplicación reiniciada", Toast.LENGTH_LONG).show()

                // Reiniciar la actividad para que se limpien las vistas
                activity?.recreate()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}