package com.zuluzza.androidmqttclient

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.MqttClient

class MqttService(context: Context?) {
    val topic = "android/mqtt/topic/1"

    var mqttAndroidClient: MqttAndroidClient
    private val clientId: String = MqttClient.generateClientId()
    private val serverURI = "tcp://broker.hivemq.com:1883"

    init {
        mqttAndroidClient = MqttAndroidClient(context, serverURI , clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, server: String) {
                Log.d(TAG, "Connection established to $server")
            }

            override fun connectionLost(throwable: Throwable?) {
                Log.d(TAG, "Connection was lost")
            }

            @Throws(Exception::class)
            override fun messageArrived(
                    topic: String,
                    mqttMessage: MqttMessage
            ) {
                Log.d(TAG, "Received message on $topic: $mqttMessage")
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.d(TAG,"Publish ack received for ${iMqttDeliveryToken.message}")
            }
        })
    }

    fun connect() {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = false
        // client and server maintains state over disconnection (meets specified QoS level if disconnected)
        mqttConnectOptions.isCleanSession = false
        mqttConnectOptions.connectionTimeout = 30 // default...
        mqttConnectOptions.keepAliveInterval = 60 // default...
        mqttConnectOptions.userName = "username"
        mqttConnectOptions.password = "password".toCharArray()

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(TAG, "Connection established")
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 2* 60 * 60 //up to 2 hours
                    disconnectedBufferOptions.isPersistBuffer = true
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                }

                override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                ) {
                    Log.e(TAG, "Failed to connect: $exception")
                }
            })

        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    fun subscribe(subscriptionTopic: String, qos: Int = 0) {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.d(TAG, "Subscribed to topic '$subscriptionTopic'")
                }

                override fun onFailure(
                        asyncActionToken: IMqttToken,
                        exception: Throwable
                ) {
                    Log.e(TAG, "Subscription to topic '$subscriptionTopic' failed!")
                }
            })
        } catch (ex: MqttException) {
            System.err.println("Exception while attempted to subscribe topic '$subscriptionTopic'")
            ex.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 1) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            val token = mqttAndroidClient.publish(topic, message.payload, qos, false)
            Log.d(TAG, "Message published to topic `$topic` $message")
        } catch (e: MqttException) {
            Log.e(TAG, "Error Publishing to $topic: " + e.message)
            e.printStackTrace()
        }
    }

    fun isConnected() : Boolean {
        return mqttAndroidClient.isConnected
    }
}
