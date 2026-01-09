package com.habitapp3.fragments

import android.content.Context
import android.content.Intent // IMPORTANTE: Necesario para cambiar de pantalla
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
import com.habitapp3.RecordatoriosActivity // Asegúrate de que este nombre coincida con tu Activity
import java.io.File

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
        val esOscuro = prefs.getBoolean("MODO_OSCURO", false)
        switchOscuro.isChecked = esOscuro

        switchOscuro.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("MODO_OSCURO", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // --- 3. LÓGICA DE NOTIFICACIONES (NUEVO) ---
        // Buscamos el botón que pusimos en el XML nuevo
        val btnNotis = view.findViewById<View>(R.id.btnConfigurarNotis)

        btnNotis.setOnClickListener {
            // Abrimos la actividad de Recordatorios
            val intent = Intent(requireContext(), RecordatoriosActivity::class.java)
            startActivity(intent)
        }

        // --- 4. LÓGICA DE REINICIAR DATOS ---
        val btnBorrar = view.findViewById<Button>(R.id.btnReiniciarDatos)

        btnBorrar.setOnClickListener {
            mostrarAlertaBorrado()
        }
    }

    private fun mostrarAlertaBorrado() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Estás seguro?")
            .setMessage("Se perderán todos tus registros de sueño, historial de relax y progreso de ejercicios. Esta acción no se puede deshacer.")
            .setPositiveButton("Sí, borrar todo") { _, _ ->

                // 1. Borrar Preferencias Generales
                prefs.edit().clear().apply()

                // 2. Borrar Historial de Relax
                requireContext().getSharedPreferences("RelaxHistory", Context.MODE_PRIVATE).edit().clear().apply()

                // 3. Borrar Archivos de Sueño
                borrarArchivosDeSueno()

                // 4. Restablecer modo claro por defecto
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                Toast.makeText(context, "Aplicación reiniciada por completo", Toast.LENGTH_LONG).show()

                // Reiniciar la actividad para limpiar la pantalla y volver al inicio (probablemente Login)
                // Ojo: Al borrar prefs, la app volverá a pedir nombre al reiniciar
                val intent = requireActivity().intent
                requireActivity().finish()
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun borrarArchivosDeSueno() {
        try {
            val directory = requireContext().filesDir
            val archivos = directory.listFiles { file ->
                file.name.startsWith("session_") && file.name.endsWith(".json")
            }
            archivos?.forEach { archivo ->
                archivo.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}