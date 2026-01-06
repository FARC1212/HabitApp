package com.habitapp3.adapters // Ajusta el paquete si lo tienes en otra carpeta

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView // O MaterialCardView según tu import
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitapp3.R
import com.habitapp3.models.CalendarDay
import com.google.android.material.card.MaterialCardView

class CalendarAdapter(private val days: List<CalendarDay>) :
    RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayName: TextView = view.findViewById(R.id.tv_day_name)
        val tvDayNumber: TextView = view.findViewById(R.id.tv_day_number)
        val indicator: View = view.findViewById(R.id.view_indicator)
        val card: MaterialCardView = view.findViewById(R.id.card_day)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]

        // 1. Poner textos
        holder.tvDayName.text = day.dayName
        holder.tvDayNumber.text = day.dayNumber

        // 2. Lógica del puntito verde (Si completó el hábito)
        if (day.isCompleted) {
            holder.indicator.visibility = View.VISIBLE
        } else {
            holder.indicator.visibility = View.INVISIBLE
        }

        // 3. Lógica visual si el día está seleccionado (El día de "Hoy")
        if (day.isSelected) {
            // Fondo oscuro, texto blanco (Estilo "Activo")
            holder.card.setCardBackgroundColor(Color.parseColor("#333333"))
            holder.tvDayName.setTextColor(Color.WHITE)
            holder.tvDayNumber.setTextColor(Color.WHITE)
        } else {
            // Fondo blanco, texto normal (Estilo "Inactivo")
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.tvDayName.setTextColor(Color.parseColor("#757575"))
            holder.tvDayNumber.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount() = days.size
}