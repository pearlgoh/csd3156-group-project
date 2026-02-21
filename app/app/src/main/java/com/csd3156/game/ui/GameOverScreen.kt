package com.csd3156.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
fun GameOverScreen(
    score: Int,
    onPlayAgain: () -> Unit,
    onViewScoreboard: () -> Unit,
    modifier: Modifier = Modifier,
    scoreboardViewModel: ScoreboardViewModel = viewModel()
) {
    var playerName by remember { mutableStateOf("") }

    var titleVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        titleVisible = true
    }
    val topScores by scoreboardViewModel.topScores.collectAsState()
    val isNewHighScore = topScores.isEmpty() || score > (topScores.firstOrNull()?.score ?: 0)


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        AnimatedVisibility(
            visible = titleVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(600)
            )
        ) {
            Text(
                text = "GAME OVER",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Text(
            text = "Score: $score",
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (isNewHighScore) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(
                        color = Color(0xFFFFD700),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "🏆 New High Score!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        OutlinedTextField(
            value = playerName,
            onValueChange = { playerName = it },
            label = { Text("Enter your name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                scoreboardViewModel.addScore(playerName.trim().ifBlank { "Anonymous" }, score)
                onViewScoreboard()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Save Score & View Scoreboard")
        }

        OutlinedButton(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Play Again")
        }
    }
}
