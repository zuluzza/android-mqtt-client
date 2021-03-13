package com.zuluzza.androidmqttclient

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AutoStart : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "starting")
            val serviceIntent = Intent(context, DataSendingService::class.java)
            ContextCompat.startForegroundService(context!!, serviceIntent)
        }
    }
}