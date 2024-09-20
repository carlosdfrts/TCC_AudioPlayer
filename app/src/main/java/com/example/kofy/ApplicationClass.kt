package com.example.kofy

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

class ApplicationClass: Application() {

    companion object {
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Tocando agora", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "Canal importante para exibição"
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}