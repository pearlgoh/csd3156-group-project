package com.csd3156.game

import android.R.attr.height
import android.R.attr.width
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
//import android.graphics.*
//import android.view.MotionEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.materialcore.screenHeightDp

@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier, context: Context) {
//    val game = Game(context, viewModel)

//    AndroidView(
//        modifier = modifier,//.fillMaxSize(),
//        factory = { context ->
//            Game(context, viewModel)
//        }
//    )

//    val paint = Paint()
//    val tilePaint = Paint()
//    val textPaint = Paint()
    val textSize = 80f

    val columnCount = 4
    //val columnWidth = width / columnCount.toFloat() //0f

//    tilePaint.color = Color.Black
//    textPaint.color = Color.Black
    //textPaint.textSize = 80f
//    textPaint.isAntiAlias = true
//
//    paint.color = Color.Gray
//    paint.strokeWidth = 5f


    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val screenHeightPx = dpToPx(screenHeightDp.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.,
        modifier = modifier.fillMaxSize()
    ) {
        //Text("Help")
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

                //game.drawGrid()

//            for (tile in state.tiles) {
//                val left = tile.column * columnWidth
                //val right = left + columnWidth
                //val bottom = tile.y + 300f
                //val off = Offset(left, tile.y)
                //val size = Size(columnWidth, 300f)

                //drawRect(color = Color.Black, topLeft = off, size = size)
//            }

                //drawText("Score: ${state.score}", 50f, 100f, textPaint)

                if (!state.gameOver) {
                    viewModel.spawnTile()
                    viewModel.updateTiles(size.height.toFloat())
                    //postInvalidateOnAnimation()
                } else {
                    //canvas.drawText("GAME OVER", width / 4f, height / 2f, textPaint)
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
//            if (state.gameOver) {
//                Text("GAME OVER",
//                    fontSize = 64.sp,
//                    modifier = Modifier.offset(pxToDp(boxSize.width.toFloat() / 2f),
//                        pxToDp(boxSize.height.toFloat() / 2f)))
//            }
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


class Game (//View(
    context: Context,
    private val viewModel: GameViewModel
) /*: View(context)*/ {

//    private val paint = Paint()
//    private val tilePaint = Paint()
//    private val textPaint = Paint()
//    private val textSize = 80f
//
//    private val columnCount = 4
//    private var columnWidth = 0f

//    init {
//        tilePaint.color = Color.Black
//        textPaint.color = Color.Black
//        //textPaint.textSize = 80f
//        textPaint.isAntiAlias = true
//    }

//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        columnWidth = w / columnCount.toFloat()
//        viewModel.spawnTile()
//    }

//    override fun onDraw(canvas: Canvas) {
//        val state = viewModel.gameState.value
//
//        drawGrid(canvas)
//
//        for (tile in state.tiles) {
//            val left = tile.column * columnWidth
//            val right = left + columnWidth
//            val bottom = tile.y + 300f
//            canvas.drawRect(left, tile.y, right, bottom, tilePaint)
//        }
//
//        canvas.drawText("Score: ${state.score}", 50f, 100f, textPaint)
//
//        if (!state.gameOver) {
//            viewModel.updateTiles(height.toFloat())
//            postInvalidateOnAnimation()
//        } else {
//            canvas.drawText("GAME OVER", width / 4f, height / 2f, textPaint)
//        }
//    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_DOWN) {
//            val touchedColumn = (event.x / columnWidth).toInt()
//            viewModel.handleTap(touchedColumn, height.toFloat())
//        }
//        return true
//    }

//    public fun drawGrid(canvas: Canvas) {
//        paint.color = Color.Gray
//        paint.strokeWidth = 5f
//
//        for (i in 1 until columnCount) {
//            val x = i * columnWidth
//            val off1 = Offset(x, 0f)
//            val off2 = Offset(x, height.toFloat())
//            canvas.drawLine(off1, off2, paint)
//        }
//    }
}