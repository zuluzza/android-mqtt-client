package com.zuluzza.androidmqttclient

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

val TAG = "MqttClient"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serviceIntent = Intent(this, DataSendingService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}