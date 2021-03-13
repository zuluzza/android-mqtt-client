package com.zuluzza.androidmqttclient

import android.R
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*


class DataSendingService: Service() {
    private var id: Int = 0
    private val channelId = "MqttServiceChannel"
    private var running = false
    private var undeliveredMessageBuffer: MutableList<String> = mutableListOf()
    private lateinit var mqttService: MqttService

    private fun publishAt1Hz() {
        // block running more than one publishing coroutine
        if (running) return

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                running = true
                while (true) {
                    // This is not perfect way to create 1Hz data, but good enough for the purpose
                    // of showcasing communication. More exact way would be e.g. having repeating
                    // alarm going off at 1Hz
                    delay(1000)
                    if (mqttService.isConnected()) {
                        // Send undelivered messages in a separate coroutine in order to keep on
                        // publishing new messages at 1Hz if there are a lot of undelivered messages
                        launch {
                            undeliveredMessageBuffer.forEach({mqttService.publish(mqttService.topic, it)})
                            undeliveredMessageBuffer.clear()
                        }

                        mqttService.publish(mqttService.topic, "Message payload id ${id++}")
                    } else {
                        undeliveredMessageBuffer.add(undeliveredMessageBuffer.size,"Message payload id ${id++}")
                        Log.d(TAG, "Added id=$id to undelivered buffer")
                    }
                }
                running = false
            }
        }
    }

    // Note: this is to show that data flows, not part of the required functionality
    private fun subscribeToReceiveData() {
        if (running) return

        GlobalScope.launch {
            // wait until connection is established ...
            while (!mqttService.isConnected()) { delay(50) }
            // ... then subscribe the topic in order to receive all send data
            mqttService.subscribe(mqttService.topic)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "DataSendingService on start")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("MQTT Publishing Service")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        mqttService = MqttService(applicationContext)
        mqttService.connect()

        subscribeToReceiveData()
        publishAt1Hz()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "MQTT Publishing Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}