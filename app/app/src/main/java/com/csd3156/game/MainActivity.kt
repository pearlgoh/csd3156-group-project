package com.csd3156.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.csd3156.game.ui.GameOverScreen
import com.csd3156.game.ui.MainMenuScreen
import com.csd3156.game.ui.ScoreboardScreen
import com.csd3156.game.ui.ScoreboardViewModel
import com.csd3156.game.ui.SettingsScreen
import com.csd3156.game.ui.theme.GameTheme

/** Entry point for the application. Hosts the navigation graph and audio lifecycle. */
class MainActivity : ComponentActivity() {

    private val gameView by viewModels<GameViewModel>()
    private val scoreboardView by viewModels<ScoreboardViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameTheme {
                Nav(Modifier.fillMaxSize(), gameView, scoreboardView)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Resume BGM when the app returns to the foreground.
        App.soundManager.resumeBGM()
    }

    override fun onStop() {
        super.onStop()
        // Pause BGM when the app is sent to the background.
        App.soundManager.pauseBGM()
    }

    override fun onDestroy() {
        super.onDestroy()
        App.soundManager.release()
    }
}

// ---------------------------------------------------------------------------
// Navigation destinations
// ---------------------------------------------------------------------------

data object MainMenuView
data object GameView
data class GameOverView(val score: Int)
data object ScoreboardView
data object SettingsView

// ---------------------------------------------------------------------------
// Root composable
// ---------------------------------------------------------------------------

/**
 * Root navigation composable that manages the app's backstack.
 *
 * Each screen is added to the backstack as a typed destination object. Navigation3's
 * [NavDisplay] resolves each key to the correct [NavEntry] composable.
 *
 * @param modifier Modifier forwarded to every screen in the graph.
 * @param gameView ViewModel shared across game and game-over screens.
 * @param scoreboardViewModel ViewModel shared across game-over and scoreboard screens.
 */
@Composable
fun Nav(modifier: Modifier, gameView: GameViewModel, scoreboardViewModel: ScoreboardViewModel) {
    val backstack = remember { mutableStateListOf<Any>(MainMenuView) }

    NavDisplay(
        backStack = backstack,
        onBack = { backstack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = { key ->
            when (key) {
                is MainMenuView -> NavEntry(key) {
                    MainMenuScreen(
                        onPlay = {
                            gameView.resetGame()
                            backstack.add(GameView)
                        },
                        onScoreboard = { backstack.add(ScoreboardView) },
                        onSettings = { backstack.add(SettingsView) },
                        modifier = modifier,
                    )
                }

                is GameView -> NavEntry(key) {
                    GameScreen(
                        modifier = modifier,
                        viewModel = gameView,
                        onGameOver = { score -> backstack.add(GameOverView(score)) },
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
                            // Clear back to MainMenuView, then push the scoreboard.
                            while (backstack.size > 1) backstack.removeLastOrNull()
                            backstack.add(ScoreboardView)
                        },
                        modifier = modifier,
                        scoreboardViewModel = scoreboardViewModel,
                    )
                }

                is ScoreboardView -> NavEntry(key) {
                    ScoreboardScreen(
                        onBack = { backstack.removeLastOrNull() },
                        modifier = modifier,
                        viewModel = scoreboardViewModel,
                    )
                }

                is SettingsView -> NavEntry(key) {
                    SettingsScreen(
                        onBack = { backstack.removeLastOrNull() },
                        modifier = modifier,
                    )
                }

                else -> NavEntry(key) { Text("") }
            }
        },
    )
}
