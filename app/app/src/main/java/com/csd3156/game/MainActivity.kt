package com.csd3156.game

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.csd3156.game.ui.GameOverScreen
import com.csd3156.game.ui.MainMenuScreen
import com.csd3156.game.ui.ScoreboardScreen
import com.csd3156.game.ui.ScoreboardViewModel
import com.csd3156.game.ui.theme.GameTheme
import androidx.activity.viewModels
import androidx.compose.ui.unit.dp
import com.csd3156.game.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    private val gameView by viewModels<GameViewModel>()
    private val scoreboardView by viewModels<ScoreboardViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameTheme {
                Nav(Modifier.fillMaxSize(), this, gameView, scoreboardView)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        App.soundManager.resumeBGM()
    }

    override fun onStop() {
        super.onStop()
        App.soundManager.pauseBGM()
    }
}

data object MainMenuView
data object GameView
data class GameOverView(val score: Int)
data object ScoreboardView
data object SettingsView

@Composable
fun Nav(mod: Modifier, ctx: Context, gameView: GameViewModel, scoreboardViewModel: ScoreboardViewModel) {
    val viewModelFactory = MainViewModelFactory(/*(application as App).repository*/)

    val backstack = remember { mutableStateListOf<Any>(MainMenuView) }
    val viewModel = viewModel<MainViewModel>(factory = viewModelFactory)

    var currBot by remember { mutableStateOf("") }
    var currBotId by remember { mutableStateOf(0) }

    var chatBotsOrder by remember { mutableStateOf(MutableList(50) {index -> index}) }

    NavDisplay(
        backStack = backstack,
        onBack = {
            backstack.removeLastOrNull()
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = {
                key -> when(key) {
            is MainMenuView -> NavEntry(key) {
                MainMenuScreen(
                    onPlay = {
                        gameView.resetGame()
                        backstack.add(GameView)
                    },
                    onScoreboard = {
                        backstack.add(ScoreboardView)
                    },
                    onSettings = {
                        backstack.add(SettingsView)
                    },
                    modifier = mod
                )
            }

            is GameView -> NavEntry(key) {
                GameScreen(
                    modifier = mod,
                    context = ctx,
                    viewModel = gameView,
                    onGameOver = { score ->
                        backstack.add(GameOverView(score))
                    },
                    onHome = {
                        while (backstack.size > 1) backstack.removeLastOrNull()
                    }
                )
            }

            is GameOverView -> NavEntry(key) {
                GameOverScreen(
                    score = key.score,
                    onPlayAgain = {
                        gameView.resetGame()
                        backstack.removeLastOrNull()
                    },
                    onViewScoreboard = {
                        // Clear down to MainMenuView, then show scoreboard
                        while (backstack.size > 1) backstack.removeLastOrNull()
                        backstack.add(ScoreboardView)
                    },
                    modifier = mod,
                    scoreboardViewModel = scoreboardViewModel
                )
            }

            is ScoreboardView -> NavEntry(key) {
                ScoreboardScreen(
                    onBack = {
                        backstack.removeLastOrNull()
                    },
                    modifier = mod,
                    viewModel = scoreboardViewModel
                )
            }

            is SettingsView -> NavEntry(key) {
                SettingsScreen(
                    onBack = {
                        backstack.removeLastOrNull()
                    },
                    modifier = mod
                )
            }

            else -> NavEntry(key) { Text("") }
        }
        }
    )
}


enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GameTheme {
        Greeting("Android")
    }
}