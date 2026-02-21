package com.csd3156.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class SoundManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val soundPool: SoundPool
    private var tapSoundId: Int = -1

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load tap sound
        // Note: Need to add tap_sound.mp3 to res/raw
        val tapResId = context.resources.getIdentifier("tap_sound", "raw", context.packageName)
        if (tapResId != 0) {
            tapSoundId = soundPool.load(context, tapResId, 1)
        }
    }

    fun playBGM() {
        // Note: Need to add bgm.mp3 to res/raw
        val bgmResId = context.resources.getIdentifier("bgm", "raw", context.packageName)
        if (bgmResId == 0) return

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, bgmResId)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(0.3f, 0.3f)
            mediaPlayer?.start()
        } else if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun pauseBGM() {
        mediaPlayer?.pause()
    }

    fun stopBGM() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun playTapSound() {
        if (tapSoundId != -1) {
            soundPool.play(tapSoundId, 0.7f, 0.7f, 0, 0, 1f)
        }
    }
}
