package com.csd3156.game

import android.app.Application

class App: Application() {
    companion object {
        lateinit var soundManager: SoundManager
            private set
        lateinit var vibrationManager: VibrationManager
            private set

    }

    override fun onCreate() {
        super.onCreate()
        soundManager = SoundManager(this)
        vibrationManager = VibrationManager(this)

    }
}