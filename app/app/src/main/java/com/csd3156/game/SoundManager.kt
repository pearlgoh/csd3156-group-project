package com.csd3156.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.util.Log

/**
 * Manages all audio playback for the application, including Background Music (BGM)
 * and Sound Effects (SFX).
 *
 * BGM is handled by a [MediaPlayer] that is created once and reused across play/stop
 * cycles to avoid audio hardware re-initialization noise. SFX are handled by a
 * [SoundPool] for low-latency playback.
 */
class SoundManager(context: Context) {

    // Application context is safe to store — it lives as long as the process.
    // Required for setDataSource(Context, Uri) which does not work with a plain String.
    private val appContext: Context = context.applicationContext

    // MediaPlayer is used for long-running audio like background music.
    private var mediaPlayer: MediaPlayer? = null

    // Guards against calling player methods while prepareAsync() is still running.
    private var isPrepared = false

    // SoundPool is optimized for short, low-latency sounds like button taps.
    private val soundPool: SoundPool
    private var tapSoundId = -1

    private val bgmUri: Uri =
        Uri.parse("android.resource://${context.packageName}/${R.raw.bgm}")

    // Tracks whether BGM is intended to be playing. Prevents music from auto-starting
    // on screens where it should be silent (e.g. game over).
    private var shouldPlayBgm = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        tapSoundId = soundPool.load(context, R.raw.tap_sound, 1)
        // Silent warm-up play once loading completes, so the first audible tap
        // doesn't trigger audio hardware initialization and produce a static pop.
        soundPool.setOnLoadCompleteListener { pool, sampleId, _ ->
            pool.play(sampleId, 0f, 0f, 0, 0, 1f)
        }
    }

    /**
     * Explicitly starts the BGM and sets the intent to "playing".
     *
     * Call this when entering a screen where music should play (e.g. main menu, game).
     */
    fun playBGM() {
        shouldPlayBgm = true
        resumeBGM()
    }

    /**
     * Resumes the BGM only if it was previously intended to be playing.
     *
     * Used during Activity lifecycle transitions (e.g. [android.app.Activity.onStart])
     * to restore playback after the app returns to the foreground.
     */
    fun resumeBGM() {
        if (!shouldPlayBgm) return

        try {
            if (mediaPlayer == null) {
                // First-time creation: audio attributes are set BEFORE prepare so Android
                // can route audio correctly from the start, avoiding startup static.
                // prepareAsync() + onPreparedListener ensures playback begins only once
                // the audio system is fully ready.
                isPrepared = false
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(appContext, bgmUri)
                    isLooping = true
                    setVolume(0.2f, 0.2f)
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                        true
                    }
                    setOnPreparedListener { mp ->
                        isPrepared = true
                        // Re-check intent in case stopBGM() was called during preparation.
                        if (shouldPlayBgm) mp.start()
                    }
                    prepareAsync()
                }
            } else if (isPrepared) {
                // Reuse the existing player — no new stream creation, no static.
                mediaPlayer?.let { if (!it.isPlaying) it.start() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in resumeBGM", e)
        }
    }

    /**
     * Pauses the BGM without clearing the intent to play.
     *
     * Used when the app is sent to the background so playback can be restored
     * seamlessly when the app returns to the foreground.
     */
    fun pauseBGM() {
        if (!isPrepared) return
        try {
            mediaPlayer?.let { if (it.isPlaying) it.pause() }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in pauseBGM", e)
        }
    }

    /**
     * Stops the BGM and marks the intent as "not playing".
     *
     * The [MediaPlayer] is paused and rewound rather than released, so the next
     * [playBGM] call can restart silently by reusing the existing player. Releasing
     * and recreating the player causes a static pop from audio hardware re-initialization.
     */
    fun stopBGM() {
        shouldPlayBgm = false
        if (!isPrepared) return
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.pause()
                it.seekTo(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in stopBGM", e)
        }
    }

    /**
     * Plays the tap sound effect.
     *
     * Routed through [SoundPool] for minimal latency.
     */
    fun playTapSound() {
        if (tapSoundId != -1) {
            soundPool.play(tapSoundId, 1f, 1f, 0, 0, 1f)
        }
    }

    /**
     * Releases all audio resources.
     *
     * Should be called from [android.app.Activity.onDestroy] to prevent resource leaks.
     */
    fun release() {
        shouldPlayBgm = false
        isPrepared = false
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in release", e)
        } finally {
            mediaPlayer = null
        }
        soundPool.release()
    }

    companion object {
        private const val TAG = "SoundManager"
    }
}
