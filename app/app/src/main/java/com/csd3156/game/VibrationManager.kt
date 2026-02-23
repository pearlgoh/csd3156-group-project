package com.csd3156.game

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.ContextualFlowRow

class VibrationManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
    }
    private val audioManager: AudioManager by lazy{
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(PREF_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(PREF_VIBRATION, value).apply()

    private fun canVibrate() : Boolean{
        if(!isVibrationEnabled) return false
        val ringerMode = audioManager.ringerMode
        //skip on RINGER_MODE_SILENT
        return ringerMode != AudioManager.RINGER_MODE_SILENT
    }

    fun vibrateOnTap(){
        if(!canVibrate()) return
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE))
        }else  {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30L)
        }
    }

    fun vibrateOnFail(){
        if (!canVibrate()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Two-pulse pattern: buzz, pause, buzz
            val timings = longArrayOf(0L, 120L, 80L, 200L)
            val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0L, 120L, 80L, 200L), -1)
        }
    }

    companion object{
        private const val PREF_VIBRATION = "vibration_enabled"
    }
}

