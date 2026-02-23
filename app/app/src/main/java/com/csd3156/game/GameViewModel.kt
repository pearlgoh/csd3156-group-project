package com.csd3156.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/**
 * ViewModel for the main game screen.
 *
 * Manages tile spawning, movement, scoring, and tap handling. Coordinates audio
 * and vibration feedback through [App.soundManager] and [App.vibrationManager].
 */
class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())

    /** The current state of the game, observed by the UI. */
    val gameState: StateFlow<GameState> = _gameState

    private var tileBuffer = 0f

    /**
     * Spawns a new tile at the top of the screen when the tile buffer allows it.
     *
     * The buffer prevents tiles from spawning too frequently, maintaining a consistent
     * gap between consecutive tiles.
     */
    fun spawnTile() {
        if (tileBuffer > 0f) return
        val state = _gameState.value
        if (state.gameOver || state.isPaused) return

        val column = Random.nextInt(COLUMN_COUNT)
        val newPos = if (gameState.value.tiles.isEmpty()) {
            -TILE_HEIGHT
        } else {
            -TILE_HEIGHT + gameState.value.tiles.last().y
        }

        _gameState.update { state ->
            state.copy(tiles = state.tiles + Tile(column, newPos))
        }
        tileBuffer = TILE_HEIGHT
    }

    /**
     * Advances all tiles downward by the current speed and checks for game-over.
     *
     * Game-over is triggered when any tile falls past the bottom of the play area.
     * Audio and vibration feedback fire once on the transition.
     *
     * @param screenHeight Height of the play area in pixels.
     */
    fun updateTiles(screenHeight: Float) {
        val state = _gameState.value
        if (state.gameOver || state.isPaused) return

        if (tileBuffer > 0f) {
            tileBuffer -= state.speed
        }

        val updatedTiles = state.tiles.map { it.copy(y = it.y + state.speed) }
        val gameOver = updatedTiles.any { it.y > screenHeight }

        if (gameOver) {
            App.soundManager.stopBGM()
            App.vibrationManager.vibrateOnFail()
        }

        _gameState.value = state.copy(
            tiles = updatedTiles,
            speed = state.speed + 0.02f,
            gameOver = gameOver,
        )
    }

    fun pauseGame() {
        val state = _gameState.value
        if (state.gameOver) return
        _gameState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        _gameState.update { it.copy(isPaused = false) }
    }

    fun resetGame() {
        App.soundManager.stopBGM()
        _gameState.value = GameState()
        tileBuffer = 0f
    }

    /**
     * Handles a tap on the given tile.
     *
     * Awards a point when the tapped tile is the correct first tile in the correct column.
     * Triggers game-over with audio and vibration feedback on an incorrect tap.
     *
     * @param tile The tile that was tapped.
     * @param index The position of [tile] within the current tile list.
     */
    fun handleTap(tile: Tile, index: Int) {
        val state = _gameState.value
        if (state.gameOver || state.isPaused ) return

        val firstTile = state.tiles.firstOrNull()

        if (firstTile != null && tile.column == firstTile.column && index == 0) {
            App.soundManager.playTapSound()
            App.vibrationManager.vibrateOnTap()
            _gameState.value = state.copy(
                tiles = state.tiles.drop(1),
                score = state.score + 1,
            )
        } else {
            App.soundManager.stopBGM()
            App.vibrationManager.vibrateOnFail()
            _gameState.value = state.copy(gameOver = true)
        }
    }

    /** Starts the BGM when the game screen is entered. */
    fun startGame() {
        App.soundManager.playBGM()
    }

    companion object {
        private const val COLUMN_COUNT = 4
        private const val TILE_HEIGHT = 300f
    }
}
