package com.habitapp3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.habitapp3.data.SleepSession

class SleepSessionAdapter(private val sessions: List<SleepSession>) : RecyclerView.Adapter<SleepSessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Buscamos los componentes. Usamos '?' para que si no encuentra uno, no cierre la app.
        val dateTextView: TextView? = itemView.findViewById(R.id.dateTextView)
        val timeRangeTextView: TextView? = itemView.findViewById(R.id.timeRangeTextView)
        val totalTimeTextView: TextView? = itemView.findViewById(R.id.totalTimeTextView)

        val deepSleepTextView: TextView? = itemView.findViewById(R.id.deepSleepTextView)
        val mediumSleepTextView: TextView? = itemView.findViewById(R.id.mediumSleepTextView)
        val lightSleepTextView: TextView? = itemView.findViewById(R.id.lightSleepTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        // AQUÍ estaba la clave: Usamos exactamente tu nombre de archivo
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sleep_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]

        // Asignamos los textos con seguridad (?.let o ?.text)

        // 1. FECHA
        holder.dateTextView?.text = session.startDate

        // 2. RANGO DE HORA
        // Si el XML tiene el campo de rango (tu nuevo diseño lo tiene), lo usamos.
        if (holder.timeRangeTextView != null) {
            holder.timeRangeTextView.text = "${session.startTime} - ${session.endTime}"
        } else {
            // Si por alguna razón fallara, lo pegamos a la fecha
            holder.dateTextView?.text = "${session.startDate} (${session.startTime} - ${session.endTime})"
        }

        // 3. TIEMPO TOTAL
        holder.totalTimeTextView?.text = session.totalTime

        // 4. FASES DEL SUEÑO
        holder.deepSleepTextView?.text = session.deepSleepTime
        holder.mediumSleepTextView?.text = session.mediumSleepTime
        holder.lightSleepTextView?.text = session.lightSleepTime
    }

    override fun getItemCount() = sessions.size
}