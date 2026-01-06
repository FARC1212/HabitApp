package com.habitapp3.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitapp3.R
import com.habitapp3.SleepSessionAdapter
import com.habitapp3.data.SleepSession
import com.google.gson.Gson
import java.io.File

class HistorialFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sleepSessionAdapter: SleepSessionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial, container, false)

        recyclerView = view.findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sleepSessions = loadSleepSessions()
        sleepSessionAdapter = SleepSessionAdapter(sleepSessions)
        recyclerView.adapter = sleepSessionAdapter

        return view
    }

    private fun loadSleepSessions(): List<SleepSession> {
        val sessions = mutableListOf<SleepSession>()
        val directory = requireContext().filesDir
        val gson = Gson()

        directory.listFiles { file -> file.name.startsWith("session_") && file.name.endsWith(".json") }
            ?.sortedByDescending { it.lastModified() } // Muestra los más recientes primero
            ?.forEach { file ->
                try {
                    val json = file.readText()
                    val session = gson.fromJson(json, SleepSession::class.java)
                    sessions.add(session)
                } catch (e: Exception) {
                    e.printStackTrace() // Manejar error de lectura o parseo
                }
            }
        return sessions
    }
}
