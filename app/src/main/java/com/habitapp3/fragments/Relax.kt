package com.habitapp3.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.habitapp3.R

class Relax : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_relax, container, false)
    }

    override fun onResume() {
        super.onResume()
        // Verificamos si debemos preguntar por el hábito cada vez que el usuario entra a esta pantalla
        verificarSiDeseaActivar()
    }

    private fun verificarSiDeseaActivar() {
        val context = context ?: return
        val prefs = context.getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)

        // Verificamos si el hábito de RELAX está oculto (false)
        val estaVisible = prefs.getBoolean("VER_RELAX", true)

        // Si NO está visible (es false), lanzamos la pregunta
        if (!estaVisible) {
            AlertDialog.Builder(context)
                .setTitle("¿Activar hábito de Relax?")
                .setMessage("El seguimiento de estrés está desactivado en tu inicio. ¿Quieres añadir este hábito a tu tablero?")
                .setPositiveButton("Sí, lo necesito") { _, _ ->
                    // Guardamos que AHORA ES VISIBLE (true)
                    prefs.edit().putBoolean("VER_RELAX", true).apply()
                    Toast.makeText(context, "¡Relax agregado al Inicio!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No por ahora") { dialog, _ ->
                    // No hacemos nada, solo cerramos el diálogo
                    dialog.dismiss()
                }
                .show()
        }
    }
}