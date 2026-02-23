package com.csd3156.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Main game screen composable.
 *
 * Renders the tile grid and score overlay, and drives the game loop via [LaunchedEffect].
 * Notifies the caller via [onGameOver] once the game ends.
 *
 * @param viewModel The [GameViewModel] driving game state.
 * @param modifier Modifier applied to the root container.
 * @param onGameOver Called with the final score when the game ends.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier,
    onGameOver: (score: Int) -> Unit = {},
) {
    val columnCount = 4

    LaunchedEffect(Unit) {
        viewModel.startGame()
    }

    val state by viewModel.gameState.collectAsState()

    LaunchedEffect(state.gameOver) {
        if (state.gameOver) onGameOver(state.score)
    }

    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    // Game loop: spawn and advance tiles at ~60 fps until game-over.
    LaunchedEffect(state.gameOver, boxSize) {
        if (!state.gameOver && boxSize.height > 0) {
            while (true) {
                viewModel.spawnTile()
                viewModel.updateTiles(boxSize.height.toFloat())
                delay(16)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().onSizeChanged { size -> boxSize = size }) {

        // Dark gradient background with vertical column dividers.
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460),
                    )
                )
            )

            val columnWidth = size.width / columnCount.toFloat()
            for (i in 1 until columnCount) {
                val x = i * columnWidth
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 3f,
                )
            }
        }

        // Tile buttons — each spans one column and scrolls downward.
        val columnWidth = boxSize.width.toFloat() / columnCount.toFloat()
        for ((index, tile) in state.tiles.withIndex()) {
            val left = tile.column * columnWidth
            Button(
                onClick = { viewModel.handleTap(tile, index) },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .size(pxToDp(columnWidth), pxToDp(300f))
                    .offset(pxToDp(left), pxToDp(tile.y)),
            ) {}
        }

        // Score pill shown at the top of the screen.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(horizontal = 24.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${state.score}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

/** Converts a pixel value to [Dp] using the current display density. */
@Composable
private fun pxToDp(px: Float): Dp {
    val density = LocalDensity.current
    return with(density) { px.toDp() }
}
