package com.csd3156.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Gold = Color(0xFFFFD700)
private val Silver = Color(0xFFB0BEC5)
private val Bronze = Color(0xFFCD7F32)

@Composable
fun ScoreboardScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ScoreboardViewModel = viewModel()
) {
    val localScores by viewModel.topScores.collectAsState()
    val globalScores by viewModel.globalScores.collectAsState()
    val isShowingGlobal by viewModel.isShowingGlobal.collectAsState()

    val scores = if (isShowingGlobal) globalScores else localScores

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scoreboard",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { if (isShowingGlobal) viewModel.toggleScoreboard() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isShowingGlobal)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (!isShowingGlobal)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Local")
            }
            Button(
                onClick = { if (!isShowingGlobal) viewModel.toggleScoreboard() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isShowingGlobal)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isShowingGlobal)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Global")
            }
        }

        if (scores.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isShowingGlobal)
                        "No global scores yet.\nPlay a game and go online!"
                    else
                        "No scores yet.\nPlay a game!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rank",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    text = "Player",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Score",
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(scores) { index, score ->
                    val rank = index + 1
                    val medalText = when (rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "#$rank"
                    }
                    val rankColor = when (rank) {
                        1 -> Gold
                        2 -> Silver
                        3 -> Bronze
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    val cardBg = when (rank) {
                        1 -> Gold.copy(alpha = 0.15f)
                        2 -> Silver.copy(alpha = 0.12f)
                        3 -> Bronze.copy(alpha = 0.12f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = medalText,
                                fontSize = if (rank <= 3) 22.sp else 16.sp,
                                color = rankColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(64.dp)
                            )
                            Text(
                                text = score.playerName,
                                fontSize = if (rank <= 3) 18.sp else 16.sp,
                                fontWeight = if (rank <= 3) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = score.score.toString(),
                                fontSize = if (rank <= 3) 18.sp else 16.sp,
                                fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Normal,
                                color = rankColor
                            )
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Back")
        }
    }
}
