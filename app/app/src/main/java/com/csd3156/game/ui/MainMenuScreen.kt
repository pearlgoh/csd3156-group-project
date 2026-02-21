package com.csd3156.game.ui

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import com.csd3156.game.R
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue

@Composable
fun MainMenuScreen(
    onPlay: () -> Unit,
    onScoreboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Game Logo",
            modifier = Modifier
                .size(180.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "TILE TAPPER",
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Tap the black tiles!",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        Button(
            onClick = onPlay,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
        ) {
            Text("Play", fontSize = 20.sp)
        }

        OutlinedButton(
            onClick = onScoreboard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scoreboard", fontSize = 20.sp)
        }
    }
}
