package com.csd3156.game

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.edit

/**
 * Manages haptic feedback for the application.
 *
 * Vibration is suppressed when the device is in silent mode and can be toggled
 * globally through [isVibrationEnabled], which is persisted across sessions via
 * [SharedPreferences].
 */
class VibrationManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Obtain the Vibrator through VibratorManager on API 31+, directly on older versions.
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        }
    }

    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * Whether vibration feedback is enabled.
     *
     * Reads from and writes to [SharedPreferences] so the setting persists across
     * app launches. Defaults to `true`.
     */
    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(PREF_VIBRATION, true)
        set(value) { prefs.edit { putBoolean(PREF_VIBRATION, value) } }

    /** Returns `true` if the device can currently vibrate (enabled and not in silent mode). */
    private fun canVibrate(): Boolean {
        if (!isVibrationEnabled) return false
        return audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT
    }

    /** Plays a short vibration pulse to confirm a successful tile tap. */
    fun vibrateOnTap() {
        if (!canVibrate()) return
        vibrator.vibrate(VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    /** Plays a two-pulse vibration pattern to signal a game-over event. */
    fun vibrateOnFail() {
        if (!canVibrate()) return
        val timings = longArrayOf(0L, 120L, 80L, 200L)
        val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    companion object {
        private const val PREFS_NAME = "game_prefs"
        private const val PREF_VIBRATION = "vibration_enabled"
    }
}
