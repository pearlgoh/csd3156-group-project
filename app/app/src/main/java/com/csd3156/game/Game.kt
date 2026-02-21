package com.csd3156.game

import android.R.attr.height
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign

@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier, context: Context) {
    //val textSize = 80f

    val columnCount = 4

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val screenHeightPx = dpToPx(screenHeightDp.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        val state by viewModel.gameState.collectAsState()

        // draw

        Row(modifier = Modifier.fillMaxWidth().background(color = Color.White)) {
            Text("Score: ${state.score}", fontSize = 32.sp)
        }

        var boxSize by remember { mutableStateOf(IntSize.Zero) }

        Box(
            modifier = Modifier.fillMaxSize().onSizeChanged { size -> boxSize = size }) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val columnWidth = size.width / columnCount.toFloat()
                for (i in 1 until columnCount) {
                    val x = i * columnWidth
                    val off1 = Offset(x, 0f)
                    val off2 = Offset(x, height.toFloat())

                    drawLine(color = Color.Gray, start = off1, end = off2, strokeWidth = 5f)
                }

                if (!state.gameOver) {
                    viewModel.spawnTile()
                    viewModel.updateTiles(size.height.toFloat())
                } else {

                }
            }
            val columnWidth = boxSize.width.toFloat() / columnCount.toFloat()
            for ((index, tile) in state.tiles.withIndex()) {
                val left = tile.column * columnWidth
                Button(
                    onClick = {
                        viewModel.handleTap(tile, index, screenHeightPx)
                    },
                    shape = RoundedCornerShape(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    modifier = Modifier.size(pxToDp(columnWidth), pxToDp(300f))
                        .offset(pxToDp(left), pxToDp(tile.y))
                ) { }
            }

            if (state.gameOver) {
                Text("GAME OVER", fontSize = 48.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.background(color = Color.White).fillMaxWidth().align(Alignment.Center))
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
