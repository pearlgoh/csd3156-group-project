package com.csd3156.game

data class GameState(
    val tiles: List<Tile> = emptyList(),
    val score: Int = 0,
    val speed: Float = 10f,
    val gameOver: Boolean = false
)