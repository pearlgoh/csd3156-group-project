package com.csd3156.game

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier, context: Context, onGameOver: (score: Int) -> Unit = {}) {
    val columnCount = 4

    LaunchedEffect(Unit) {
        viewModel.startGame()
    }

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val screenHeightPx = dpToPx(screenHeightDp.dp)

    val state by viewModel.gameState.collectAsState()

    LaunchedEffect(state.gameOver) {
        if (state.gameOver) {
            onGameOver(state.score)
        }
    }

    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(state.gameOver, boxSize) {
        if (!state.gameOver && boxSize.height > 0) {
            while (true) {
                viewModel.spawnTile()
                viewModel.updateTiles(boxSize.height.toFloat())
                kotlinx.coroutines.delay(16)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().onSizeChanged { size -> boxSize = size }) {
        // Dark gradient background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )

            // Column divider lines
            val columnWidth = size.width / columnCount.toFloat()
            for (i in 1 until columnCount) {
                val x = i * columnWidth
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 3f
                )
            }
        }

        // Tiles
        val columnWidth = boxSize.width.toFloat() / columnCount.toFloat()
        for ((index, tile) in state.tiles.withIndex()) {
            val left = tile.column * columnWidth
            Button(
                onClick = {
                    viewModel.handleTap(tile, index, screenHeightPx)
                },
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .size(pxToDp(columnWidth), pxToDp(300f))
                    .offset(pxToDp(left), pxToDp(tile.y))
            ) { }
        }

        // Score pill overlay
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${state.score}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun pxToDp(px: Float): Dp {
    val density = LocalDensity.current
    return with(density) { px.toDp() }
}

@Composable
fun dpToPx(dp: Dp): Float {
    val density = LocalDensity.current
    return with(density) { dp.toPx() }
}
