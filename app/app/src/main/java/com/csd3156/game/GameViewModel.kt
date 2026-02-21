package com.csd3156.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class GameViewModel : ViewModel() {

    private val columnCount = 4
    private val tileHeight = 300f

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState

    private var tileBuffer = 0f

    fun spawnTile() {
        if (tileBuffer > 0f) return

        val column = Random.nextInt(columnCount)
        val newPos = if(gameState.value.tiles.isEmpty()) -tileHeight else -tileHeight + gameState.value.tiles.last().y

        _gameState.update { state ->
            state.copy(
                tiles = state.tiles + Tile(column, newPos)
            )
        }
        tileBuffer = tileHeight
    }

    fun updateTiles(screenHeight: Float) {
        val state = _gameState.value
        if (state.gameOver) return

        if (tileBuffer > 0f) {
            tileBuffer -= state.speed
        }

        val updatedTiles = state.tiles.map {
            it.copy(y = it.y + state.speed)
        }

        val gameOver = updatedTiles.any { it.y > screenHeight }

        _gameState.value = state.copy(
            tiles = updatedTiles,
            speed = state.speed + 0.02f,
            gameOver = gameOver
        )
    }

    fun resetGame() {
        _gameState.value = GameState()
        tileBuffer = 0f
    }

    fun handleTap(tile: Tile/*touchedColumn: Int*/, index: Int, screenHeight: Float) {
        val state = _gameState.value
        if (state.gameOver) return

        val firstTile = state.tiles.firstOrNull()

        if (firstTile != null &&
            tile.column == firstTile.column &&
            index == 0
        ) {
            _gameState.value = state.copy(
                tiles = state.tiles.drop(1),
                score = state.score + 1
            )
        } else {
            _gameState.value = state.copy(gameOver = true)
        }
    }
}