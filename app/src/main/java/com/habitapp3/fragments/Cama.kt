package com.habitapp3.fragments

import com.google.gson.Gson
import com.habitapp3.data.SleepSession
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.habitapp3.R
import com.habitapp3.receivers.AlarmReceiver
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class Cama : Fragment() {

    // --- Interface for communicating with MainActivity ---
    interface NavigationBarController {
        fun setNavigationBarVisibility(isVisible: Boolean)
    }
    private var navigationBarController: NavigationBarController? = null

    // --- UI Elements ---
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var sleepChart: LineChart
    private lateinit var alarmButton: Button
    private lateinit var historyButton: Button

    // --- Sound Monitoring & Data ---
    private var soundMonitor: SoundMonitor? = null
    private val rawSoundLevels = mutableListOf<Double>()
    private val chartEntries = mutableListOf<Entry>()
    private val savedSessionEntries = mutableListOf<Entry>()
    private var dataCollectionTimer: Timer? = null
    private var chartUpdateTimer: Timer? = null
    private var sessionStartTime: Date? = null
    @Volatile private var latestSoundLevel: Double = 0.0

    // --- Alarm Receiver ---
    private val stopMonitoringReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.habitapp3.STOP_MONITORING") {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "¡Alarma! Deteniendo monitoreo...", Toast.LENGTH_LONG).show()
                    if (stopButton.isEnabled) { // Ensure monitoring is active
                        stopMonitoring()
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Link the fragment to the activity's implementation of the interface
        if (context is NavigationBarController) {
            navigationBarController = context
        } else {
            throw RuntimeException("$context must implement NavigationBarController")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cama, container, false)
        bindViews(view)
        setupListeners()
        setupChart()
        return view
    }

    // En Cama.kt, dentro de bindViews
    private fun bindViews(view: View) {
        startButton = view.findViewById(R.id.startButton)
        stopButton = view.findViewById(R.id.stopButton)
        statusTextView = view.findViewById(R.id.statusTextView)
        sleepChart = view.findViewById(R.id.sleepChart)
        alarmButton = view.findViewById(R.id.alarmButton)
        historyButton = view.findViewById(R.id.historyButton)
    }


    private fun setupListeners() {
        startButton.setOnClickListener { checkPermissionAndStartMonitoring() }
        stopButton.setOnClickListener { stopMonitoringAndCancelAlarm() }
        alarmButton.setOnClickListener { openTimePicker() }

        // Añade el listener para el botón de historial
        historyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HistorialFragment())
                .addToBackStack(null) // Permite al usuario volver a la pantalla de Cama
                .commit()
        }
    }

    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            val alarmTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) { // If time is in the past, set for tomorrow
                    add(Calendar.DATE, 1)
                }
            }
            setAlarm(alarmTime.timeInMillis)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            alarmButton.text = "Alarma: ${timeFormat.format(alarmTime.time)}"
        }, currentHour, currentMinute, false).show()
    }

    private fun setAlarm(timeInMillis: Long) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Request permission for exact alarms on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().also {
                    it.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(it)
                }
                Toast.makeText(context, "Se necesita permiso para alarmas exactas", Toast.LENGTH_LONG).show()
                return
            }
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        Toast.makeText(context, "Alarma establecida", Toast.LENGTH_SHORT).show()
    }

    private fun cancelAlarm() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Toast.makeText(context, "Alarma cancelada", Toast.LENGTH_SHORT).show()
            alarmButton.text = "Establecer Alarma"
        }
    }

    private fun startMonitoring() {
        navigationBarController?.setNavigationBarVisibility(false) // Hide Nav Bar
        // ... (rest of the startMonitoring logic is the same)
        chartEntries.clear()
        savedSessionEntries.clear()
        rawSoundLevels.clear()
        sessionStartTime = Date()
        updateChart()

        statusTextView.text = "Monitoreando sonido..."
        startButton.isEnabled = false
        stopButton.isEnabled = true
        alarmButton.isEnabled = false

        latestSoundLevel = 0.0
        soundMonitor = SoundMonitor()
        soundMonitor?.startMonitoring { soundLevel ->
            val finiteSoundLevel = if (soundLevel.isFinite()) soundLevel else 0.0
            latestSoundLevel = finiteSoundLevel
            synchronized(rawSoundLevels) {
                rawSoundLevels.add(finiteSoundLevel)
            }
        }

        chartUpdateTimer = timer(period = 1000) {
            val elapsedTimeInSeconds = sessionStartTime?.let { (Date().time - it.time) / 1000f } ?: 0f
            chartEntries.add(Entry(elapsedTimeInSeconds / 60, latestSoundLevel.toFloat()))
            activity?.runOnUiThread {
                if (isAdded) {
                    updateChart()
                    statusTextView.text = "Nivel de ruido actual: ${"%.2f".format(latestSoundLevel)} dB"
                }
            }
        }

        dataCollectionTimer = timer(initialDelay = 30_000, period = 30_000) {
            processAndSaveAverage()
        }

        Toast.makeText(context, "Monitoreo iniciado", Toast.LENGTH_SHORT).show()
    }

    private fun stopMonitoring() {
        navigationBarController?.setNavigationBarVisibility(true) // Show Nav Bar
        chartUpdateTimer?.cancel()
        dataCollectionTimer?.cancel()
        soundMonitor?.stopMonitoring()
        chartUpdateTimer = null
        dataCollectionTimer = null
        soundMonitor = null

        // ¡CAMBIO CLAVE! Llama a la nueva función de análisis y guardado
        // en lugar de la antigua 'saveSessionToFile'
        processAndSaveAverage() // Guarda el último segmento
        analyzeAndSaveSession()

        statusTextView.text = "Monitoreo detenido. Sesión guardada."
        startButton.isEnabled = true
        stopButton.isEnabled = false
        alarmButton.isEnabled = true
    }

    private fun stopMonitoringAndCancelAlarm() {
        stopMonitoring()
        cancelAlarm()
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            stopMonitoringReceiver, IntentFilter("com.habitapp3.STOP_MONITORING")
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(stopMonitoringReceiver)
    }

    override fun onDetach() {
        super.onDetach()
        navigationBarController = null // Clean up the reference
    }

    // ... (The rest of the functions: checkPermissionAndStartMonitoring, processAndSaveAverage, saveSessionToFile, setupChart, updateChart, onRequestPermissionsResult, onDestroyView are the same) ...
    // --- The following functions are copied from your existing code for completeness ---

    private fun checkPermissionAndStartMonitoring() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO), 1 // RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            startMonitoring()
        }
    }

    private fun processAndSaveAverage() {
        val averageSoundLevel: Double
        synchronized(rawSoundLevels) {
            if (rawSoundLevels.isEmpty()) {
                return // Nothing to process
            }
            averageSoundLevel = rawSoundLevels.average()
            rawSoundLevels.clear()
        }

        val elapsedTimeInSeconds = sessionStartTime?.let { (Date().time - it.time) / 1000f } ?: 0f
        savedSessionEntries.add(Entry(elapsedTimeInSeconds / 60, averageSoundLevel.toFloat()))
    }

    private fun analyzeAndSaveSession() {
        val sessionEndTime = Date()
        val startTime = sessionStartTime ?: return
        val sessionData = savedSessionEntries.toList()

        if (sessionData.isEmpty() || !isAdded) return

        // 1. Analizar los datos
        val (lightSleepMinutes, mediumSleepMinutes, deepSleepMinutes) = analyzeSleepStages(sessionData)
        val totalMinutes = (sessionEndTime.time - startTime.time) / 60000

        // Formateamos el texto (Ej: "7h 30m")
        val totalTimeStr = formatMinutesToHoursAndMinutes(totalMinutes)

        // 2. Formatear los datos para el modelo SleepSession (JSON)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val session = SleepSession(
            startDate = dateFormat.format(startTime),
            startTime = timeFormat.format(startTime),
            endTime = timeFormat.format(sessionEndTime),
            totalTime = totalTimeStr,
            lightSleepTime = formatMinutesToHoursAndMinutes(lightSleepMinutes.toLong()),
            mediumSleepTime = formatMinutesToHoursAndMinutes(mediumSleepMinutes.toLong()),
            deepSleepTime = formatMinutesToHoursAndMinutes(deepSleepMinutes.toLong())
        )

        // 3. Guardar JSON (Historial)
        saveSessionAsJson(session, startTime)

        // --- NUEVO: GUARDAR PARA LA PANTALLA DE INICIO ---
        val prefs = requireContext().getSharedPreferences("HabitAppPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            // Guardamos el tiempo total (Ej: "7h 15m")
            putString("ULTIMO_SUENO_TIEMPO", totalTimeStr)
            // Guardamos la fecha para saber si es de hoy
            putString("FECHA_ULTIMO_SUENO", dateFormat.format(sessionEndTime))
            apply()
        }
        // ------------------------------------------------
    }

    // AÑADE esta función para el análisis de las fases de sueño
    private fun analyzeSleepStages(entries: List<Entry>): Triple<Int, Int, Int> {
        val stageDurationMinutes = 20 // Duración de cada bloque a analizar
        var light = 0
        var medium = 0
        var deep = 0

        // Agrupa las entradas de promedios (cada una representa 30s) en bloques de 20 minutos.
        // Como cada entrada es de 30s, necesitamos 40 entradas para hacer 20 minutos.
        val entriesPerChunk = 40
        val chunks = entries.windowed(size = entriesPerChunk, step = entriesPerChunk, partialWindows = true)

        for (chunk in chunks) {
            val averageNoiseInChunk = chunk.map { it.y }.average()
            // Define tus umbrales de ruido aquí
            when {
                averageNoiseInChunk < 35 -> deep++   // Ruido bajo -> Sueño Profundo
                averageNoiseInChunk < 50 -> medium++ // Ruido medio -> Sueño Medio
                else -> light++                      // Ruido alto -> Sueño Ligero o despierto
            }
        }

        // Multiplica el contador de cada fase por la duración de los bloques
        return Triple(light * stageDurationMinutes, medium * stageDurationMinutes, deep * stageDurationMinutes)
    }

    // AÑADE esta función para guardar el archivo en formato JSON
    private fun saveSessionAsJson(session: SleepSession, startTime: Date) {
        val gson = Gson()
        val jsonString = gson.toJson(session)

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val filename = "session_${dateFormat.format(startTime)}.json"

        try {
            val file = File(requireContext().filesDir, filename)
            FileOutputStream(file).use {
                it.write(jsonString.toByteArray())
            }
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Análisis de sesión guardado", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // AÑADE esta función de ayuda para formatear el tiempo
    private fun formatMinutesToHoursAndMinutes(minutes: Long): String {
        if (minutes < 0) return "0m"
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return "${hours}h ${remainingMinutes}m"
    }

    private fun setupChart() {
        sleepChart.description.isEnabled = false
        sleepChart.setTouchEnabled(true)
        sleepChart.isDragEnabled = true
        sleepChart.setScaleEnabled(true)
        sleepChart.setPinchZoom(true)
        sleepChart.legend.isEnabled = false
        sleepChart.axisRight.isEnabled = false
        sleepChart.xAxis.apply {
            setDrawLabels(false)
            setDrawGridLines(false)
            setDrawAxisLine(false)
        }
        sleepChart.axisLeft.apply {
            setDrawLabels(false)
            setDrawGridLines(true)
            setDrawAxisLine(false)
            gridColor = Color.LTGRAY
        }
    }

    private fun updateChart() {
        if (!isAdded) return

        if (chartEntries.isEmpty()) {
            sleepChart.clear()
            sleepChart.invalidate()
            return
        }

        val dataSet = LineDataSet(chartEntries, "Nivel de Ruido")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.purple_500)
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 2.5f

        val lineData = LineData(dataSet)
        sleepChart.data = lineData
        sleepChart.invalidate()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1) { // RECORD_AUDIO_PERMISSION_CODE
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMonitoring()
            } else {
                Toast.makeText(context, "Permiso de audio denegado.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chartUpdateTimer?.cancel()
        dataCollectionTimer?.cancel()
        soundMonitor?.stopMonitoring()
    }
}
