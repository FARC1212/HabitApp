package com.habitapp3.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This is a static action name we'll use for communication.
        val stopIntent = Intent("com.habitapp3.STOP_MONITORING")
        LocalBroadcastManager.getInstance(context).sendBroadcast(stopIntent)
    }
}