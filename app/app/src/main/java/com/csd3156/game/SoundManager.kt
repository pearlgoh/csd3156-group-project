package com.csd3156.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * Manages all audio playback for the application, including Background Music (BGM)
 * and Sound Effects (SFX).
 */
class SoundManager(private val context: Context) {
    // MediaPlayer is used for long-running audio like background music
    private var mediaPlayer: MediaPlayer? = null
    
    // SoundPool is optimized for short, low-latency sounds like button taps
    private val soundPool: SoundPool
    private var tapSoundId: Int = -1
    
    // Tracks if BGM is intended to be playing (e.g., active in a menu or game)
    // Helps prevent music from auto-starting when it should be silent (like Game Over screen)
    private var shouldPlayBGM: Boolean = false

    init {
        // Configure audio attributes for game usage
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load tap sound from res/raw/tap_sound.mp3
        val tapResId = context.resources.getIdentifier("tap_sound", "raw", context.packageName)
        if (tapResId != 0) {
            tapSoundId = soundPool.load(context, tapResId, 1)
        }
    }

    /**
     * Explicitly starts BGM and sets the intent to "playing".
     * Call this when entering screens where music should play.
     */
    fun playBGM() {
        shouldPlayBGM = true
        resumeBGM()
    }

    /**
     * Resumes BGM only if it was intended to be playing.
     * Used primarily during Activity lifecycle transitions (e.g., onStart).
     */
    fun resumeBGM() {
        if (!shouldPlayBGM) return

        val bgmResId = context.resources.getIdentifier("bgm", "raw", context.packageName)
        if (bgmResId == 0) return

        try {
            if (mediaPlayer == null) {
                // Initialize and start if not exists
                mediaPlayer = MediaPlayer.create(context, bgmResId)
                mediaPlayer?.apply {
                    isLooping = true
                    setVolume(0.2f, 0.2f)
                    setOnErrorListener { _, _, _ ->
                        stopBGM()
                        true
                    }
                    start()
                }
            } else {
                // Resume if already exists but paused
                if (!mediaPlayer!!.isPlaying) {
                    mediaPlayer?.start()
                }
            }
        } catch (e: Exception) {
            stopBGM()
        }
    }

    /**
     * Pauses the BGM without clearing the intent to play.
     * Used when the app is backgrounded.
     */
    fun pauseBGM() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            // Ignore errors on pause to prevent crashes
        }
    }

    /**
     * Stops the BGM completely and sets the intent to "not playing".
     * Call this when entering screens where music should be silent (e.g., Game Over).
     */
    fun stopBGM() {
        shouldPlayBGM = false
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            // Ignore errors on stop/release
        } finally {
            mediaPlayer = null
        }
    }

    /**
     * Plays the tap sound effect with a short delay/low latency.
     * Pitch can be adjusted by changing the last 'rate' parameter (1.0f = normal).
     */
    fun playTapSound() {
        if (tapSoundId != -1) {
            // Parameters: soundID, leftVol, rightVol, priority, loop, rate
            soundPool.play(tapSoundId, 1f, 1f, 0, 0, 1f)
        }
    }
}
