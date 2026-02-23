package com.csd3156.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import androidx.core.net.toUri

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

    // Set to true by release() so no further audio calls are made on dead resources.
    private var isReleased = false

    private val bgmUri = "android.resource://${context.packageName}/${R.raw.bgm}".toUri()

    // Tracks whether BGM is intended to be playing. Prevents music from auto-starting
    // on screens where it should be silent (e.g. game over).
    private var shouldPlayBgm = false

    // Set while the in-game pause screen is shown. Blocks resumeBGM() from restarting
    // the BGM through the Activity lifecycle (onStart) while the game is still paused.
    private var isGamePaused = false

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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Attaches audio attributes, data source, and listeners to [mp].
     *
     * Extracted so the same setup can be applied both on first creation and after
     * an in-place [MediaPlayer.reset] recovery, avoiding a full teardown/recreate
     * which would cause a static pop from audio hardware re-initialization.
     */
    private fun setupPlayer(mp: MediaPlayer) {
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mp.setDataSource(appContext, bgmUri)
        mp.isLooping = true
        mp.setVolume(0.2f, 0.2f)
        mp.setOnErrorListener { player, what, extra ->
            Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
            isPrepared = false
            // Attempt in-place recovery: reset the existing instance and re-prepare
            // rather than releasing and nulling it. Creating a new MediaPlayer
            // requires a full audio stream initialization which causes static.
            try {
                player.reset()
                setupPlayer(player)
                player.prepareAsync()
            } catch (e: Exception) {
                Log.e(TAG, "In-place recovery failed, releasing player", e)
                player.release()
                mediaPlayer = null
            }
            true
        }
        mp.setOnPreparedListener { player ->
            isPrepared = true
            // Only auto-start if we intend to play AND the game isn't paused.
            // isGamePaused guards against auto-starting during background recovery
            // while the pause screen is still showing.
            if (shouldPlayBgm && !isGamePaused) player.start()
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Explicitly starts the BGM and sets the intent to "playing".
     *
     * Call this when entering a screen where music should play (e.g. main menu, game).
     */
    fun playBGM() {
        if (isReleased) return
        isGamePaused = false
        shouldPlayBgm = true
        resumeBGM()
    }

    /**
     * Resumes the BGM only if it was previously intended to be playing and the game
     * is not currently paused.
     *
     * Used during Activity lifecycle transitions (e.g. [android.app.Activity.onStart])
     * to restore playback after the app returns to the foreground.
     */
    fun resumeBGM() {
        if (isReleased || !shouldPlayBgm || isGamePaused) return

        try {
            if (mediaPlayer == null) {
                // First-time creation (or after an unrecoverable error):
                // audio attributes are set via setupPlayer() BEFORE prepareAsync() so
                // Android can route audio correctly from the start, avoiding startup static.
                isPrepared = false
                val mp = MediaPlayer()
                mediaPlayer = mp
                setupPlayer(mp)
                mp.prepareAsync()
            } else if (isPrepared) {
                // Reuse the existing player — no new stream creation, no static.
                mediaPlayer?.let { if (!it.isPlaying) it.start() }
            }
            // If mediaPlayer != null but isPrepared == false, prepareAsync() is still
            // running; onPreparedListener will call start() when ready.
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
        if (isReleased || !isPrepared) return
        try {
            mediaPlayer?.let { if (it.isPlaying) it.pause() }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in pauseBGM", e)
        }
    }

    /**
     * Pauses the BGM at its current position for the in-game pause screen.
     *
     * Sets [isGamePaused] so that [resumeBGM] called from the Activity lifecycle
     * (e.g. [android.app.Activity.onStart]) cannot restart the BGM while the game
     * is still paused. Call [resumeGameBGM] to undo this.
     */
    fun pauseGameBGM() {
        if (isReleased || !isPrepared) return
        isGamePaused = true
        try {
            mediaPlayer?.let { if (it.isPlaying) it.pause() }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in pauseGameBGM", e)
        }
    }

    /**
     * Resumes the BGM from the position it was paused at by [pauseGameBGM].
     *
     * Clears [isGamePaused] so the Activity lifecycle can manage audio normally again.
     * If the player was recovered in the background while paused, [onPreparedListener]
     * will start it automatically once preparation completes.
     */
    fun resumeGameBGM() {
        isGamePaused = false
        resumeBGM()
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
        isGamePaused = false
        if (isReleased || !isPrepared) return
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
        if (isReleased || tapSoundId == -1) return
        soundPool.play(tapSoundId, 1f, 1f, 0, 0, 1f)
    }

    /**
     * Releases all audio resources.
     *
     * Should be called from [android.app.Activity.onDestroy] to prevent resource leaks.
     */
    fun release() {
        isReleased = true
        shouldPlayBgm = false
        isPrepared = false
        isGamePaused = false
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
