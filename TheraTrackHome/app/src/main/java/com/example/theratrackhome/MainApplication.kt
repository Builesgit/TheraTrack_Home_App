package com.example.theratrackhome

import android.app.Application
import com.example.theratrackhome.controller.NotificacionesManager

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificacionesManager.crearCanales(this)
    }
}
