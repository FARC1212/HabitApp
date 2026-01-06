package com.habitapp3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.habitapp3.data.SleepSession

class SleepSessionAdapter(private val sessions: List<SleepSession>) : RecyclerView.Adapter<SleepSessionAdapter.SessionViewHolder>() {

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val totalTimeTextView: TextView = itemView.findViewById(R.id.totalTimeTextView)
        val deepSleepTextView: TextView = itemView.findViewById(R.id.deepSleepTextView)
        val mediumSleepTextView: TextView = itemView.findViewById(R.id.mediumSleepTextView)
        val lightSleepTextView: TextView = itemView.findViewById(R.id.lightSleepTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sleep_session, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.dateTextView.text = "${session.startDate} (${session.startTime} - ${session.endTime})"
        holder.totalTimeTextView.text = "Tiempo Total: ${session.totalTime}"
        holder.deepSleepTextView.text = "Profundo: ${session.deepSleepTime}"
        holder.mediumSleepTextView.text = "Medio: ${session.mediumSleepTime}"
        holder.lightSleepTextView.text = "Ligero: ${session.lightSleepTime}"
    }

    override fun getItemCount() = sessions.size
}
    